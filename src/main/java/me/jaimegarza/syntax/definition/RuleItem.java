package me.jaimegarza.syntax.definition;

public class RuleItem {
  Symbol symbol;
  Rule rule;

  public RuleItem(Symbol symbol) {
    super();
    this.symbol = symbol;
  }

  public Symbol getSymbol() {
    return symbol;
  }

  public void setSymbol(Symbol symbol) {
    this.symbol = symbol;
  }

  public Rule getRule() {
    return rule;
  }

  public void setRule(Rule rule) {
    this.rule = rule;
  }

  public int getSymbolId() {
    return symbol.getId();
  }
  
  public RuleItem next() {
    for (int i = 0; i < rule.getItems().size(); i++) {
      RuleItem r = rule.getItem(i);
      if (this.equals(r)) {
        return rule.getItem(i+1);
      }
    }
    return null;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    
    try{
      return getSymbolId() == ((RuleItem) obj).getSymbolId();
    } catch (NullPointerException unused) {
      return false;
    } catch (ClassCastException unused) {
      return false;
    }
  }

  @Override
  public String toString() {
    return symbol.toString();
  }

}
