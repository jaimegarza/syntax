package me.jaimegarza.syntax.definition;

public class Terminal extends Symbol {

  private String variable;
  
  public Terminal(String name) {
    super(name);
  }
  
  public Terminal(NonTerminal nonTerminal) {
    super(nonTerminal.name);
    this.count = nonTerminal.count;
    this.associativity = nonTerminal.associativity;
    this.fullName = nonTerminal.fullName;
    this.id = nonTerminal.id;
    this.precedence = nonTerminal.precedence;
    this.token = nonTerminal.token;
    this.type = nonTerminal.type;
  }

  public String getVariable() {
    return variable;
  }

  public void setVariable(String variable) {
    this.variable = variable;
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj);
  }

}
