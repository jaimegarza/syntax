package me.jaimegarza.syntax.generator.parser;


public class ParserAction {
  public int symbol;
  public int state;

  public ParserAction(int symbol, int state) {
    super();
    this.symbol = symbol;
    this.state = state;
  }

}
