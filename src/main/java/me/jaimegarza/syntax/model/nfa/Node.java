/*
Syntax is distibuted under the Revised, or 3-clause BSD license
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
package me.jaimegarza.syntax.model.nfa;

import java.util.HashSet;
import java.util.Set;

public class Node {
  private static int sequence = 0;
  
  private int id;
  private DirectedGraph graph;
  private Set<Transition> transitions = new HashSet<>();
  private boolean accept = false;
  private boolean starting = false;
  
  public Node(DirectedGraph graph) {
    id = ++sequence;
    this.graph = graph;
  }
  
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

  public void addTransition(Transition transition) {
    transitions.add(transition);
  }
  
  public void removeTransition(Transition transition) {
    transitions.remove(transition);
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
  public DirectedGraph getGraph() {
    return graph;
  }

  @Override
  public String toString() {
    return "Node [id=" + id + ", transitions=" + transitions + ", accept=" + accept + ", starting=" + starting + "]";
  }
}
