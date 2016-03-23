/*
Syntax is distributed under the Revised, or 3-clause BSD license
===============================================================================
Copyright (c) 1985, 2012, 2016, Jaime Garza
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the copyright holder nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
===============================================================================
*/
package me.jaimegarza.syntax.model.graph;

import me.jaimegarza.syntax.model.graph.symbol.AnyCharacter;
import me.jaimegarza.syntax.model.graph.symbol.RegexSymbol;

/**
 * A transition (edge) maps a node to another node. These are
 * directed transitions
 * @author jgarza
 *
 */
public class Transition {
  /** The origin node */
  private Node from;
  /** The destination node */
  private Node to;
  /** The transition's symbol */
  private RegexSymbol symbol;

  /**
   * The default constructor. Transitions also get added to the origin node
   * @param from the node from which the transition happens
   * @param to the destination node
   * @param symbol the symbol causing the transition
   */
  public Transition(Node from, Node to, RegexSymbol symbol) {
    if (from == null || to == null) {
      throw new IllegalArgumentException("transition's from and to have to be non null");
    }
    this.from = from;
    this.to = to;
    this.symbol = symbol;
    
    // update from node's transitions
    from.addTransition(this);
  }

  /**
   * @return the from
   */
  public Node getFrom() {
    return from;
  }

  /**
   * @return the to
   */
  public Node getTo() {
    return to;
  }
  
  /**
   * @return the symbol
   */
  public RegexSymbol getSymbol() {
    return symbol;
  }

  /**
   * @return true if it is an &epsilon; transition
   */
  public boolean isEpsilon() {
    return symbol.isEpsilon();
  }
  
  /**
   * @return true if the transition is of <ANY> type
   */
  public boolean isAny() {
    return symbol instanceof AnyCharacter;
  }

  /**
   * Return a canonical representation of the transition. The
   * current implementation displays a symbols canonical representation
   * @return the canonical representation of the transition
   */
  public String canonical() {
    return symbol.canonical();
  }

  /**
   * Does a character match an input symbol?
   * @param c the character to check
   * @return true if it matches
   */
  public boolean matches(char c) {
    return symbol.matches(c);
  }
  
  /**
   * Determine the code for a given transition.
   * The code is determined by the symbol
   */
  public int code() {
    return symbol.code();
  }
  
  /**
   * What is the size of this transition in the table. Uses
   * the symbol + 1 for the node id
   */
  public  int sizeof() {
    return symbol.sizeof() + 1; // 1 destination node;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    
    try {
      Transition t = (Transition) o;
      return from.equals(t.from) && 
             to.equals(t.to) &&
             symbol.equals(t.symbol);
    } catch (NullPointerException unused) {
      return false;
    } catch (ClassCastException unused) {
      return false;
    }
  }
  
  @Override
  public int hashCode() {
    int hash = 1;
    hash = hash * 31 + (from == null ? 0: from.hashCode());
    hash = hash * 31 + (to == null ? 0: to.hashCode());
    hash = hash * 31 + (symbol == null ? 0: symbol.hashCode());
    return hash;
  }
  
  @Override
  public String toString() {
    return from.getId() + "(" + symbol.canonical() + ")->" + to.getId();
  }

  public int[] getCodeArray() {
    return symbol.getCodeArray();
  }

}
