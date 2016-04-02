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

import java.util.LinkedList;
import java.util.List;

import me.jaimegarza.syntax.graph.KamadaKawai;

/**
 * Abstract class defining a graph with directed transitions
 * @author jgarza
 *
 * @param <T> is the type of node
 */
public abstract class DirectedGraph<T extends Node> {
  protected final List<T> nodes = new LinkedList<T>();
  protected String regex = null;
  
  /**
   * Default empty constructor
   */
  public DirectedGraph() {
  }
  
  /**
   * @return the nodes
   */
  public List<T> getNodes() {
    return nodes;
  }

  /**
   * Retrieve a node given its index
   * @param index is the index of the node
   * @return the node at the given index
   */
  public T getNode(int index) {
    return nodes.get(index);
  }
  
  /**
   * @return the number of nodes
   */
  public int V() {
    return nodes.size();
  }
  
  public void layout(int width, int height) {
    KamadaKawai<T> layoutAlgorithm = new KamadaKawai<>(this, width, height);
    layoutAlgorithm.compute();
    
    System.out.println(layoutAlgorithm);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[nodes=" + nodes + "]";
  }

  /**
   * @return the regex
   */
  public String getRegex() {
    return regex;
  }

  /**
   * @param regex the regex to set
   */
  public void setRegex(String regex) {
    this.regex = regex;
  }

}
