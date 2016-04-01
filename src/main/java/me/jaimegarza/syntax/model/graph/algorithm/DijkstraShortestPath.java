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

import java.util.PriorityQueue;

import me.jaimegarza.syntax.model.graph.DirectedGraph;
import me.jaimegarza.syntax.model.graph.Node;
import me.jaimegarza.syntax.model.graph.Transition;

/**
 * Implementation of the Dijkstra Shortest Path algorithm with the
 * addition that it computes distances for all source nodes. We also
 * consider all paths to have a weight=1 since we are using it for
 * layout algorithms.
 * <p>
 * For this matter, <tt>distTo</tt> is an array of arrays whose first index is <tt>s</tt>.
 * <p>
 * I have based my algorithm on Algorithms, 4e, Sedgewick and Wayne, plus notes
 * from other lectures.
 * 
 * @param <T> is the type of node
 * 
 * @author jgarza
 *
 */
public class DijkstraShortestPath<T extends Node> {

  private DirectedGraph<T> graph;
  private double distTo[][];
  private Transition edgeTo[][];
  private Node pred[][];
  private double maxDist = 0.0;
  
  // working elements
  private PriorityQueue<PQ> pq;

  /**
   * Construct AND compute the shortest paths for a given graph.
   * 
   * @param graph is the graph in question
   */
  public DijkstraShortestPath(DirectedGraph<T> graph) {
    this.graph = graph;
    this.distTo = new double[graph.V()][];
    this.edgeTo = new Transition[graph.V()][];
    this.pred = new Node[graph.V()][];
    for (int i = 0; i < graph.V(); i++) {
      distTo[i] = new double[graph.V()];
      edgeTo[i] = new Transition[graph.V()];
      pred[i] = new Node[graph.V()];
      for (int j = 0; j < graph.V(); j++) {
        distTo[i][j] = i == j ? 0 : Double.POSITIVE_INFINITY;
        edgeTo[i][j] = null;
        pred[i][j] = null;
      }
      compute(i);
    }
    
    // make sure the distances are also true in a reverse order
    for (int i = 0; i < graph.V(); i++) {
      for (int j = 0; j < graph.V(); j++) {
        distTo[i][j] = Math.min(distTo[i][j], distTo[j][i]);
        distTo[j][i] = distTo[i][j];
        if (distTo[i][j] > maxDist && distTo[i][j] < Double.POSITIVE_INFINITY) {
          maxDist = distTo[i][j];
        }
      }
    }
  }
  
  public double[][] getDistances() {
    return distTo;
  }
  
  protected void compute(int s) {
    // Create a priority queue that returns in lowest to highest distance
    pq = new PriorityQueue<>((a, b) -> (int)(a.dist - b.dist));
    pq.offer(new PQ(s, 0.0));
    while (!pq.isEmpty()) {
      relax(s, pq.poll());
    }
  }
  
  protected void relax(int s, PQ elem) {
    int v = elem.v;
    Node n = graph.getNode(v);
    
    for (Transition e: n.getTransitions()) {
      int w = e.getTo().getIndex();
      
      if (distTo[s][w] > distTo[s][v] + 1) { // weight is hardcoded to one
        distTo[s][w] = distTo[s][v] + 1;
        edgeTo[s][w] = e;
        PQ elem2 = getPQ(w);
        if (elem2 != null) {
          pq.remove(elem2);
          elem2.dist = distTo[s][w];
          pq.offer(elem2);
        } else {
          pq.offer(new PQ(w, distTo[s][w]));
        }
      }
    }
  }
  
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(graph).append('\n');
    for (int i = 0; i < graph.V(); i++) {
      builder.append('[');
      for (int j = 0; j < graph.V(); j++) {
        builder.append(distTo[i][j] < Double.POSITIVE_INFINITY ? String.format("%5.0f ", distTo[i][j]) : "  INF ");
      }
      builder.append("]");
      if (i != graph.V()-1) {
        builder.append("\n");
      }
    }
    return builder.toString();
  }
  
  private PQ getPQ(int w) {
    for (PQ e : pq) {
      if (e.v == w) {
        return e;
      }
    }
    return null;
  }
  
  /**
   * Elements in the priority queue are of this type
   * @author jgarza
   *
   */
  private static class PQ {
    public int v;
    public double dist;
    
    public PQ(int v, double dist) {
      this.v = v;
      this.dist = dist;
    }
    
    @Override
    public String toString() {
      return "v=" + v + ",dist=" + dist;
    }
  }
  
  public static void main(String args[]) {
    PriorityQueue<PQ> pq = new PriorityQueue<>((a, b) -> a.dist == b.dist ? a.v -b.v : (int)(a.dist - b.dist));
    pq.offer(new PQ(9, 3.0));
    pq.offer(new PQ(10, 0.0));
    pq.offer(new PQ(7, 1.0));
    pq.offer(new PQ(5, 2.0));
    pq.offer(new PQ(4, 4.0));
    pq.offer(new PQ(8, 3.0));
    pq.offer(new PQ(7, 30.0));
    
    PQ x = null;
    for (PQ p: pq) {
      if (p.v == 7 && p.dist == 30.0) {
        x = p;
      }
    }

    pq.remove(x);
    x.dist = 0.0;
    pq.offer(x);
    
    System.out.println("pq:" + pq);
    System.out.println("Size:" + pq.size());
    System.out.println("Peek:" + pq.peek());
    System.out.println("Size:" + pq.size());
    System.out.println("Poll:" + pq.poll());
    System.out.println("pq:" + pq);
    System.out.println("Size:" + pq.size());
    System.out.println("Poll:" + pq.poll());
    System.out.println("pq:" + pq);
    System.out.println("Size:" + pq.size());
    System.out.println("Poll:" + pq.poll());
    System.out.println("pq:" + pq);
    System.out.println("Size:" + pq.size());
    System.out.println("Pool:" + pq.poll());
    System.out.println("pq:" + pq);
    System.out.println("Size:" + pq.size());
    System.out.println("Pool:" + pq.poll());
    System.out.println("pq:" + pq);
    System.out.println("Size:" + pq.size());
    System.out.println("Pool:" + pq.poll());
    System.out.println("pq:" + pq);
    System.out.println("Size:" + pq.size());
    System.out.println("Pool:" + pq.poll());
    System.out.println("pq:" + pq);
    System.out.println("Size:" + pq.size());
    System.out.println("Pool:" + pq.poll());
    System.out.println("pq:" + pq);
  }

  /**
   * @return the distTo
   */
  public double[][] getDistTo() {
    return distTo;
  }

  /**
   * @return the edgeTo
   */
  public Transition[][] getEdgeTo() {
    return edgeTo;
  }

  /**
   * @return the pred
   */
  public Node[][] getPred() {
    return pred;
  }

  /**
   * @return the maxDist
   */
  public double getMaxDist() {
    return maxDist;
  }
}
