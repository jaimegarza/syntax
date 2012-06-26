package me.jaimegarza.syntax.generator.parser;

public class Grammar {
  public int symbol;
  public int reductions;

  public Grammar(int symbol, int reductions) {
    super();
    this.symbol = symbol;
    this.reductions = reductions;
  }

}
