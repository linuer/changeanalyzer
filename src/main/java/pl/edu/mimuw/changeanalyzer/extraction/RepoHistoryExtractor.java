package pl.edu.mimuw.changeanalyzer.extraction;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

import pl.edu.mimuw.changeanalyzer.exceptions.ChangeAnalyzerException;
import pl.edu.mimuw.changeanalyzer.exceptions.ExtractionException;
import pl.edu.mimuw.changeanalyzer.util.LazyList;
import ch.uzh.ifi.seal.changedistiller.model.entities.ClassHistory;


/**
 * Master class of the extraction package. It extracts all class histories
 * and all commits from a repository.
 * 
 * @author Adam Wierzbicki
 */
public class RepoHistoryExtractor {
	
	private Repository repository;
	private Git git;
	private ClassHistoryExtractor extractor;
	
	/**
	 * Construct a new RepoHistoryExtractor.
	 * 
	 * @param repository Repository to extract history
	 */
	public RepoHistoryExtractor (Repository repository) {
		this.repository = repository;
		this.git = new Git(repository);
		this.extractor = new ClassHistoryExtractor(repository);
	}
	
	/**
	 * Construct a new RepoHistoryExtractor.
	 * 
	 * @param repoDir Directory with a repository to extract history
	 * @throws IOException When the given directory doesn't contain a proper repository
	 */
	public RepoHistoryExtractor (File repoDir) throws IOException {
		FileRepositoryBuilder repoBuilder = new FileRepositoryBuilder();
		this.repository = repoBuilder.setWorkTree(repoDir).build();
		this.git = new Git(this.repository);
		this.extractor = new ClassHistoryExtractor(this.repository);
	}
	
	/**
	 * Construct a new RepoHistoryExtractor.
	 * 
	 * @param repoPath Path to a repository to extract history
	 * @throws IOException When the given path doesn't point to a proper repository
	 */
	public RepoHistoryExtractor (String repoPath) throws IOException {
		this(new File(repoPath));
	}
	
	/**
	 * Extract histories of all clases (that is all .java files) in the repository.
	 * 
	 * @return Mapping from file paths to class histories
	 * @throws IOException
	 * @throws ChangeAnalyzerException
	 */
	public Map<String, ClassHistory> extractClassHistories() throws IOException, ChangeAnalyzerException {
		Map<String, ClassHistory> map = new HashMap<String, ClassHistory>();
		
		ObjectId headId = ExtractionUtils.getHead(this.repository);
		RevWalk revWalk = new RevWalk(this.repository);
		RevCommit headCommit = revWalk.parseCommit(headId);
		RevTree revTree = headCommit.getTree();
		
		TreeWalk treeWalk = new TreeWalk(this.repository);
		treeWalk.addTree(revTree);
		treeWalk.setRecursive(true);
		TreeFilter filter = PathSuffixFilter.create(".java");
		treeWalk.setFilter(filter);
		
		while (treeWalk.next()) {
			String path = treeWalk.getPathString();
			ClassHistory history = this.extractor.extractClassHistory(path);
			map.put(path, history);
		}
		
		return map;
	}
	
	/**
	 * Extract all commits which can be achieved from the repostory HEAD.
	 * 
	 * @return Extracted commits
	 * @throws IOException
	 * @throws ExtractionException
	 */
	public Iterable<RevCommit> extractCommits() throws IOException, ExtractionException {
		ObjectId head = ExtractionUtils.getHead(this.repository);
		LogCommand logCommand = this.git.log().add(head);
		try {
			return new LazyList<RevCommit>(logCommand.call());
		} catch (GitAPIException e) {
			throw new ExtractionException("Failed to execute LOG command", e);
		}
	}

}
