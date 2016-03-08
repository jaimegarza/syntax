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
package me.jaimegarza.syntax.model.graph;

import java.util.HashSet;
import java.util.Set;

import me.jaimegarza.syntax.model.graph.symbol.RegexSymbol;

/**
 * Deterministic finite automaton. A directed graph without
 * epsilon transitions, with cycles, and multiple final states
 * @author jgarza
 */
public class Dfa extends DirectedGraph<DfaNode> {

  /**
   * Create a dfa node with the given closure. The closure is usually
   * obtained from a Nfa node's {@link Node#eclosure()} method.
   * @param closure is the closure coming from a NFA
   * @return the new node
   */
  public DfaNode newNode(Set<NfaNode> closure) {
    DfaNode node = new DfaNode(this, closure);
    nodes.add(node);
    return node;
  }
  
  /**
   * Locate a node in the dfa by examining each node's eclosure
   * @param closure is the closure to be checked against all nodes
   * @return the node, or null if not existent.
   */
  protected DfaNode findNodeByClosure(Set<NfaNode> closure) {
    for (DfaNode node: nodes) {
      if (node.eclosure().equals(closure)) {
        return node;
      }
    }
    return null;
  }
  
  /**
   * Is a given closure final? We know that a closure is final if any
   * of the nfa nodes is an accept node
   * @param closure is a closure of nodes to check
   * @return true if any node in the closure is an accepting node
   */
  protected boolean isClosureFinal(Set<NfaNode> closure) {
    for (NfaNode n : closure) {
      if (n.isAccept()) {
        return true;
      }
    }
    return false;
  }
   
  /**
   * Generate a DFA out of a NFA. The steps are as follows:
   * 
   * <ol>
   * <li>Create a new DfaNode from the initial NFA starting state. Compute the closure
   * from the initial state.</li>
   * <li>Iterate all the nodes and all their symbol transitions to create new closures.
   * If the closures already exist in the DFA, use the found state with such closure,
   * otherwise create a new DFA state with the given closure.</li>
   * </ol> 
   * @param graph
   */
  public void generateFromNfa(Nfa graph) {
    // Create initial Dfa state
    for (NfaNode node: graph.getNodes()) {
      if (node.isStarting()) {
        Set<NfaNode> closure = node.eclosure();
        newNode(closure);
        break;
      }
    }
    
    // Create additional states
    for (int i = 0; i < nodes.size(); i++) {
      DfaNode dfaFromNode = nodes.get(i);
      Set<RegexSymbol> symbols = dfaFromNode.getTransitionSymbols();
      for (RegexSymbol symbol : symbols) {
        if (symbol.isEpsilon()) {
          continue;
        }
        Set<NfaNode> toNodes = dfaFromNode.getNfaTransitions(symbol);
        Set<NfaNode> toNodesWithClosure = new HashSet<>();
        for (NfaNode toNode : toNodes) {
          toNodesWithClosure.addAll(toNode.eclosure());
        }
        DfaNode dfaToNode = findNodeByClosure(toNodesWithClosure);
        if (dfaToNode == null) {
          dfaToNode = newNode(toNodesWithClosure);
          dfaToNode.setAccept(isClosureFinal(toNodesWithClosure));
        }
        new Transition(dfaFromNode, dfaToNode, symbol);
      }
    }    
  }
  
}
