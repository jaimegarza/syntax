package me.jaimegarza.syntax;

public class ParsingException extends Exception {

  private static final long serialVersionUID = 4142027349626749000L;

  public ParsingException(String message, Throwable cause) {
    super("parsing error:" + message, cause);
  }

  public ParsingException(String message) {
    super(message);
  }

}
