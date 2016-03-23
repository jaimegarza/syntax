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

import java.util.HashSet;
import java.util.Set;

import me.jaimegarza.syntax.model.graph.symbol.RegexSymbol;

/**
 * A node for a dfa
 * @author jgarza
 *
 */
public class DfaNode extends Node {
  private static int sequence = 0;
  
  /**
   * &epsilon;-closure coming from nfa nodes
   */
  private Set<NfaNode> closure;

  /**
   * When writing to the output code, where in the edge
   * table does this node transitions start.
   */
  private int edgeIndex;
  
  /**
   * Constructor including precomputed &epsilon;-closure
   * @param graph the source graph
   * @param closure the Nfa closure
   */
  public DfaNode(Dfa graph, Set<NfaNode> closure) {
    super(graph, sequence++);
    this.closure = closure;
  }
  
  @Override
  public Set<NfaNode> eclosure() {
    return closure;
  }
  
  /**
   * Get all symbols transitioning out given the &epsilon;-closure
   * @return the set of symbols
   */

  public Set<RegexSymbol> getTransitionSymbols() {
    Set<RegexSymbol> set = new HashSet<RegexSymbol>();
    for (NfaNode nfaNode : closure) {
      for (Transition t : nfaNode.getTransitions()) {
        RegexSymbol symbol = t.getSymbol();
        if (!set.contains(symbol)) {
          set.add(symbol);
        }
      }
    }
    return set;
  }

  /**
   * Given a symbol, what nfa nodes are being transitioned to
   * @param symbol is the symbol being checked
   * @return the set of nfa nodes
   */
  public Set<NfaNode> getNfaTransitions(RegexSymbol symbol) {
    Set<NfaNode> set = new HashSet<NfaNode>();
    for (NfaNode nfaNode : closure) {
      for (Transition t : nfaNode.getTransitions()) {
        RegexSymbol transitionSymbol = t.getSymbol();
        if (transitionSymbol.equals(symbol)) {
          set.add((NfaNode) t.getTo());
        }
      }
    }
    return set;
  }
  
  /**
   * Return the size of this node in the table
   */
  public int sizeof() {
    int tableSize = 0;
    for (Transition t: getCodeTransitions()) {
      tableSize += t.sizeof();
    }
    return tableSize + 1; // one count number
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    
    try {
      DfaNode d = (DfaNode) o;
      return d.id == id;
    } catch (ClassCastException unused) {
      return false;
    } catch (NullPointerException unused) {
      return false;
    }
  }
  
  @Override
  public int hashCode() {
    return id;
  }

  /**
   * Setter for the edge index
   * @param edgeIndex is the index of the edge
   */
  public void setEdgeIndex(int edgeIndex) {
    this.edgeIndex = edgeIndex;
  }

  /**
   * @return the edgeIndex
   */
  public int getEdgeIndex() {
    return edgeIndex;
  }
  
  /**
   * Needed for test cases
   */
  public static void reset() {
    sequence = 0;
  }
}
