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

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.jaimegarza.syntax.util.CollectionUtils;

/**
 * Abstract class denoting a node in a graph. A graph node is also known in
 * state diagram theory as a vertex. It contains an id and a set of
 * transition (or edges.)
 * 
 * @author jgarza
 *
 */
public abstract class Node {
  protected int id;
  protected DirectedGraph<? extends Node> graph;
  protected Set<Transition> transitions = new HashSet<Transition>();
  protected boolean accept = false;
  protected boolean starting = false;
  
  /**
   * Default constructor
   * @param graph the graph to which this node belongs
   * @param id the identifier for the node
   */
  public Node(DirectedGraph<? extends Node> graph, int id) {
    this.id = id;
    this.graph = graph;
  }
  
  /**
   * &epsilon;-closure is defined as the node, plus all other nodes that are
   * reachable from this node with an &epsilon; transition, recursively.
   * @return the e-closure
   */
  public abstract Set<NfaNode> eclosure();
  
  /**
   * @return the accept
   */
  public boolean isAccept() {
    return accept;
  }

  /**
   * @param accept the accept to set
   */
  public void setAccept(boolean accept) {
    this.accept = accept;
  }

  /**
   * @return the starting
   */
  public boolean isStarting() {
    return starting;
  }

  /**
   * @param starting the starting to set
   */
  public void setStarting(boolean starting) {
    this.starting = starting;
  }

  /**
   * Add one transition (edge) to this node
   * @param transition is the node transition
   */
  public void addTransition(Transition transition) {
    transitions.add(transition);
  }
  
  /**
   * Remove a transition from this node
   * @param transition is the node transition
   */
  public void removeTransition(Transition transition) {
    transitions.remove(transition);
  }
  
  /**
   * Obtain a list of transitions for writing to the generated code
   */
  public List<Transition> getCodeTransitions() {
    // decided using java 7. Java 8 below
    /*
    List<Transition> codeTransitions = transitions.stream().sorted((a,b) -> a.code() - b.code()).collect(Collectors.toList());
    boolean any = transitions.stream().filter(t->t.isAny()).count() > 0;
    if (any) {
      codeTransitions = codeTransitions.stream().filter(t->t.isAny()).limit(1).collect(Collectors.toList());
    }
    */
    
    List<Transition> codeTransitions = CollectionUtils.asSortedList(transitions, new Comparator<Transition>() {

      @Override
      public int compare(Transition a, Transition b) {
        return a.code() - b.code();
      }
      
    });
    
    boolean hasAny = false;
    for (Transition t: transitions) {
      if (t.isAny()) {
        hasAny = true;
        break;
      }
    }
    
    if (hasAny) {
      boolean firstAny = true;
      for (int i = codeTransitions.size() - 1; i>=0; i--) {
        Transition t = codeTransitions.get(i);
        if (t.isAny()) {
          if (firstAny) {
            firstAny = false;
          }
          else {
            codeTransitions.remove(i);
          }
        } else {
          codeTransitions.remove(i);
        }
      }
    }
    return codeTransitions;
  }

  /**
   * @return the transitions
   */
  public Set<Transition> getTransitions() {
    return transitions;
  }

  /**
   * @return the id
   */
  public int getId() {
    return id;
  }

  /**
   * @return the graph
   */
  public DirectedGraph<? extends Node> getGraph() {
    return graph;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append('{');
    if (starting) {
      sb.append("*");
    }
    if (accept) {
      sb.append("(");
    }
    sb.append(id);
    if (accept) {
      sb.append(")");
    }
    for (Transition t: transitions) {
      sb.append(' ').append(t.canonical()).append("->").append(t.getTo().getId());
    }
    sb.append("}");
    return sb.toString();
  }
}
