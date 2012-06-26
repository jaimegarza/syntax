package me.jaimegarza.syntax;

public class OutputException extends Exception {

  private static final long serialVersionUID = 3894726711472166627L;

  public OutputException(String message, Throwable cause) {
    super("parsing error:" + message, cause);
  }

  public OutputException(String message) {
    super(message);
  }

}
