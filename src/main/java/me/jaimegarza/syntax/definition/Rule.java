package me.jaimegarza.syntax.definition;

import java.util.LinkedList;
import java.util.List;

public class Rule {
  int rulenum;
  int lineNumber;
  int precedence;
  NonTerminal leftHand;
  List<RuleItem> items = new LinkedList<RuleItem>();

  public Rule(int rulenum, int lineNumber, int precedence, NonTerminal leftHand) {
    super();
    this.rulenum = rulenum;
    this.lineNumber = lineNumber;
    this.precedence = precedence;
    this.leftHand = leftHand;
  }

  public int getRulenum() {
    return rulenum;
  }

  public void setRulenum(int rulenum) {
    this.rulenum = rulenum;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public void setLineNumber(int lineNumber) {
    this.lineNumber = lineNumber;
  }

  public int getPrecedence() {
    return precedence;
  }

  public void setPrecedence(int precedence) {
    this.precedence = precedence;
  }

  public NonTerminal getLeftHand() {
    return leftHand;
  }

  public void setLeftHand(NonTerminal leftHand) {
    this.leftHand = leftHand;
  }

  public List<RuleItem> getItems() {
    return items;
  }

  public RuleItem getItem(int i) {
    RuleItem item = null;
    if (items != null && i < items.size()) {
      item = items.get(i);
    }
    return item;
  }

  public int getLeftHandId() {
    return leftHand.getId();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    
    try {
      Rule r = (Rule) obj;
      return rulenum == r.rulenum &&
        lineNumber == r.lineNumber &&
        precedence == r.precedence &&
        leftHand.equals(r.leftHand) /*&&
        items.equals(r.items);*/;
    } catch (NullPointerException unused) {
      return false;
    } catch (ClassCastException unused) {
      return false;
    }
  }

  @Override
  public String toString() {
    return leftHand.toString() + " -> " + items + "\n";
  }

}
