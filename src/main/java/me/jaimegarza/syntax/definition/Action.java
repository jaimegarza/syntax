package me.jaimegarza.syntax.definition;

public class Action {
  Symbol symbol;
  int stateNumber;

  public Action(Symbol symbol, int stateNumber) {
    super();
    this.symbol = symbol;
    this.stateNumber = stateNumber;
  }
  
  public Symbol getSymbol() {
    return symbol;
  }

  public void setSymbol(Symbol symbol) {
    this.symbol = symbol;
  }

  public int getStateNumber() {
    return stateNumber;
  }

  public void setStateNumber(int stateNumber) {
    this.stateNumber = stateNumber;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    
    try {
      Action a = (Action) obj;
      return symbol.equals(a.symbol) && stateNumber == a.stateNumber;
    } catch (NullPointerException unused) {
      return false;
    } catch (ClassCastException unused) {
      return false;
    }
  }
  
  public String toString() {
    return "with " + symbol + " goto " + stateNumber;
  }
}
