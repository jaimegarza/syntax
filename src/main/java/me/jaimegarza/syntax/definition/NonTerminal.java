package me.jaimegarza.syntax.definition;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class NonTerminal extends Symbol {
  // null means not computed.  empty set is computed with no items
  Set<Integer> first = null; 
  //In addition, for LALR follow will remain as null
  Set<Integer> follow = null;
  List<GoTo> gotos = new LinkedList<GoTo>();

  public Set<Integer> getFirst() {
    return first;
  }

  public void setFirst(Set<Integer> first) {
    this.first = first;
  }

  public Set<Integer> getFollow() {
    return follow;
  }

  public void setFollow(Set<Integer> follow) {
    this.follow = follow;
  }

  public List<GoTo> getGotos() {
    return gotos;
  }
  
  public void addGoTo(GoTo goThere) {
    gotos.add(goThere);
  }

  public NonTerminal(String name) {
    super(name);
  }

}
