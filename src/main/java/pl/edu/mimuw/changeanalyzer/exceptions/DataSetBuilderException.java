package pl.edu.mimuw.changeanalyzer.exceptions;


public class DataSetBuilderException extends ChangeAnalyzerException {

	private static final long serialVersionUID = -6665034329577589499L;

	public DataSetBuilderException() {}

	public DataSetBuilderException(String arg0) {
		super(arg0);
	}

	public DataSetBuilderException(Throwable arg0) {
		super(arg0);
	}

	public DataSetBuilderException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public DataSetBuilderException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

}
