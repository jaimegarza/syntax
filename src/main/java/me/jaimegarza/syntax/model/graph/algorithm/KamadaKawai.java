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
package me.jaimegarza.syntax.model.graph.algorithm;

import me.jaimegarza.syntax.model.graph.DirectedGraph;
import me.jaimegarza.syntax.model.graph.Node;

/**
 * KamadaKawai algorithm for laying up a graph. It is done in iterations
 * until a max number, or until a negligible change in E.
 * <p>
 * Based on the paper by Kamada and Kawai of 1988, "An alorithm for Drawing
 * General Unidirected Graphs".
 * <p>
 * Although they specify a newton-raphson, I decided to iterate by observing E
 * changes.
 * 
 * @author jgarza
 *
 * @param <T> is the type of node that this graph supports.
 */
public class KamadaKawai<T extends Node> {
  private double K = 1; // Not specified in the paper
  private double L; // L=L0/max_ij, computed from the size of the graph and the max distance

  public KamadaKawai(DirectedGraph<T> graph, int maxWidth, int maxHeight) {
    DijkstraShortestPath<T> shortestPath = new DijkstraShortestPath<>(graph);
    int L0 = Math.min(maxWidth, maxHeight);
    L = L0/shortestPath.getMaxDist();
    
    // Now I have L and K
  }
  
  
}
