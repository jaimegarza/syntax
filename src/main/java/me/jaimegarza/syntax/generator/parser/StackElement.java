package me.jaimegarza.syntax.generator.parser;

public class StackElement {
  public int estado;
  public int value;
  public boolean mustClose;
  public String id;
  public String regex;

  public StackElement(int estado, int value, boolean mustClose, String id, String regex) {
    super();
    this.estado = estado;
    this.value = value;
    this.mustClose = mustClose;
    this.id = id;
    this.regex = regex;
  }

  @Override
  public String toString() {
    return "state:" + estado + ", value:" + value + ", mustClose:" + mustClose + ", id:" + id;
  }

}
