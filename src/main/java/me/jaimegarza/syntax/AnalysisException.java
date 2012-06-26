package me.jaimegarza.syntax;

public class AnalysisException extends Exception {

  private static final long serialVersionUID = -6530606290254900119L;

  public AnalysisException(String message, Throwable cause) {
    super("analysis error:" + message, cause);
  }

  public AnalysisException(String message) {
    super(message);
  }

}
