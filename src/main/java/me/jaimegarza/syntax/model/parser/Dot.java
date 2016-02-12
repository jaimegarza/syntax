/*
 ===============================================================================
 Copyright (c) 1985, 2012, Jaime Garza
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
     * Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.
     * Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.
     * Neither the name of Jaime Garza nor the
       names of its contributors may be used to endorse or promote products
       derived from this software without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ===============================================================================
*/
package me.jaimegarza.syntax.model.parser;

import java.util.HashSet;
import java.util.Set;

/**
 * <i>~pojo class</i><br><br>
 * 
 * During the table creation process an algorithm is performed to obtain a
 * state machine (turing machine) with the shifts and reduces needed at all
 * states of the system.  To do that, we analyze every possible state of the 
 * grammar by the using the concept of a dot.<p>
 * 
 * The algorithm is as follows:
 * <ol>
 * <li> Locate the initial symbol, and place a dot at the first element
 *    of the rules where the initial symbol is on the left hand. For example:
 *    <pre>  S -> . E</pre>
 * 
 * <li> Compute the closure of a state on every non terminal symbol.  
 *    The closure is the set of rules that have the non terminal in the
 *    left hand side, for each non terminal with a dot at its left.
 *    A dot is assigned at the first position on the closure rules.
 *    For example:
 *    <pre>  S -> .E
 *  <i>closure:</i>
 *  E -> .E + T
 *  E -> .E - T</pre>
 *   Closure rules are identical when the two rules are the same, and the 
 *   dot positions are also the same.<br><br>
 *   
 * <li> After the state (set of rules and dots) is known, it is checked for 
 *    uniqueness.<br><br>
 *    
 * <li> We iterate through each of the states moving dots to the right of non
 *    terminals and terminals, and finding the destination state.  
 *    For instance:
 *    <pre>  <i>state 0</i>
 *  S -> .E
 *  <i>closure:</i>
 *  E -> .E + T   closure on E
 *  E -> .E - T   closure on E
 *  E -> .T       closure on E
 *  T -> .T * F   closure on T
 *  T -> .T / F   closure on T
 *  T -> .F       closure on T
 *  F -> .number  closure on F
 *  F -> .- F     closure on F
 *  F -> .( F )   closure on F
 * 
 *  <i>state 1 (with its closure, transition of state 0 with E)</i>
 *  S -> E .
 *  E -> E .+ T
 *  E -> E .- T
 *  
 *  <i>state 2 (with its closure, transition of state 1 with +)</i>
 *  E -> E + .T
 *  <i>closure:</i>
 *  T -> .T * F   closure on T
 *  T -> .T / F   closure on T
 *  T -> .F       closure on T
 *  F -> .number  closure on F
 *  F -> .- F     closure on F
 *  F -> .( F )   closure on F
 *  
 *  <i>etc...</i></pre>
 * </ol>
 * 
 * Out of this dot traversal, all states are known, and all its transition to
 * other states are also known.<p>
 * 
 * As such, a dot is a positional element that defines a rule, and the position
 * the observed rule item.
 *
 * @author jaimegarza@gmail.com
 *
 */
public class Dot {
  /** 
   * The state to which this dot belongs.  It must not be null.
   */
  State state;
  /**
   * the rule item that this dot points to. It can be null, which means that 
   * it is marking the end of a rule.  Another way to say this is that the dot
   * is at the right most position of the rule.l
   */
  private RuleItem item;
  /**
   * The rule to which this dot belongs.  This rule is the parent of all rule 
   * items.  It must not be null.
   */
  private Rule rule;
  /**
   * When computing LALR (as opposed to SLR) the lookahead is the set of all non
   * terminals that can be reached from this symbol.  in the case of the dot: 
   * <pre>  F -> .number
   * the lookaheads for the dot are {number} 
   */
  private Set<Integer> lookahead = new HashSet<Integer>();

  /**
   * Construct a dot
   * @param state is the vertical parent {@link State} of this dot
   * @param rule is the {@link Rule} where this dot points.
   * @param item is the {@link RuleItem} to the right of the dot.  It can be null.
   * 
   */
  public Dot(State state, Rule rule, RuleItem item) {
    super();
    this.state = state;
    this.item = item;
    this.rule = rule;
  }
  
  /**
   * Given a dot, move the dot to the right of the pointed symbol<br>
   * It does this by going to the rule and finding the rule item i+1 
   * from the rule item i.
   * 
   * @return the next rule item, or null
   */
  public RuleItem nextItem() {
    int index = 0;
    for (RuleItem item : rule.getItems()) {
      if (this.item == item) {
        return rule.getItem(index+1);
      }
      index++;
    }
    return null;
  }

  /**
   * State dots are linked.  Get the next dot in the state.
   * @return the next dot, or null.
   * 
   * @see {@link State#dots} for a "state view" of its dots.
   */
  public Dot next() {
    int index = 0;
    for (Dot aMarker : state.getAllDots()) {
      if (aMarker == this) {
        return state.getDot(index + 1);
      }
      index++;
    }
    return null;
  }

  /**
   * Adds a non terminal to the dot
   * @param symbolId is the id of the non terminal
   */
  public void addLookahead(int symbolId) {
    lookahead.add(symbolId);
  }

  /**
   * merge lookaheads into this dot
   * @param symbolIds
   */
  public void addAllLookaheads(Set<Integer> symbolIds) {
    lookahead.addAll(symbolIds);
  }

  /* Getters and setters */

  /**
   * @return the lookahead
   */
  public Set<Integer> getLookahead() {
    return lookahead;
  }

  /**
   * @return the state
   */
  public State getState() {
    return state;
  }

  /**
   * @param state the state to set
   */
  public void setState(State state) {
    this.state = state;
  }

  /**
   * @return the item
   */
  public RuleItem getItem() {
    return item;
  }

  /**
   * @param item the item to set
   */
  public void setItem(RuleItem item) {
    this.item = item;
  }

  /**
   * @return the rule
   */
  public Rule getRule() {
    return rule;
  }

  /**
   * @param rule the rule to set
   */
  public void setRule(Rule rule) {
    this.rule = rule;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    try {
      Dot rm = (Dot) obj;
      return RuleItem.equals(item,  rm.item) && rule.equals(rm.rule);
    } catch (NullPointerException unused) {
      return false;
    } catch (ClassCastException unused) {
      return false;
    }
  }

  /**
   * Utility method to compare two dots, accounting for nulls
   * @param a the first element.  Can be null.
   * @param b the second element.  Can be null.
   * @return true if both are null, otherwise the result of a.equals(b)
   */
  public static boolean equals(Dot a, Dot b) {
    if (a == null) return b == null; // now a is NOT null
    if (b == null) return false; // now none is null
    return a.equals(b);
  }
  
  /**
   * Returns a "dotted" representation of the rule items in the rule
   * @see java.lang.Object#toString()
   */
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
