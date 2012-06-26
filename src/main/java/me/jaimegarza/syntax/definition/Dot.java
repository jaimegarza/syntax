package me.jaimegarza.syntax.definition;

import java.util.HashSet;
import java.util.Set;

public class Dot {
  State state;
  private RuleItem item; // can be null, which means I am marking the end of a set of rule entries
  private Rule rule; // must not be null
  private Set<Integer> lookahead = new HashSet<Integer>();

  public Dot(State state, Rule rule, RuleItem item) {
    super();
    this.state = state;
    this.item = item;
    this.rule = rule;
  }

  public State getState() {
    return state;
  }

  public void setState(State state) {
    this.state = state;
  }

  public RuleItem getItem() {
    return item;
  }

  public void setItem(RuleItem item) {
    this.item = item;
  }

  public Rule getRule() {
    return rule;
  }

  public void setRule(Rule rule) {
    this.rule = rule;
  }

  public Set<Integer> getLookahead() {
    return lookahead;
  }

  public RuleItem nextRuleEntry() {
    RuleItem entry = null;
    if (item != null) {
      int index = rule.getItems().indexOf(item);
      if (index >= -1) {
        entry = rule.getItem(index+1);
      }
    }
    return entry;
  }

  public Dot next() {
    int index = 0;
    for (Dot aMarker : state.getMarkers()) {
      if (aMarker == this) {
        return state.getMarker(index + 1);
      }
      index++;
    }
    return null;
  }
  
  public void addLookahead(int symbolId) {
    lookahead.add(symbolId);
  }
  
  public void addAllLookaheads(Set<Integer> symbolIds) {
    lookahead.addAll(symbolIds);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;

    try {
      Dot rm = (Dot) obj;
      return item.equals(rm.item) && rule.equals(rm.rule);
    } catch (NullPointerException unused) {
      return false;
    } catch (ClassCastException unused) {
      return false;
    }
  }

  @Override
  public String toString() {
    String s = "";
    boolean dotted = false;
    for (RuleItem anItem : rule.getItems()) {
      if (item == anItem) {
        dotted = true;
        s += " . ";
      }
      s += " " + anItem;
    }
    if (!dotted) {
      s += " . ";
    }
    return s + "\n";
  }
}
