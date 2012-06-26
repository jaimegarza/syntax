package me.jaimegarza.syntax.generator.parser;

public class Parser {
    public int position;
    public int defa;
    public int elements;
    public int msg;
    
    
    public Parser(int position, int defa, int elements, int msg) {
      super();
      this.position = position;
      this.defa = defa;
      this.elements = elements;
      this.msg = msg;
    }
}
