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

import java.util.Random;

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
  private static final double ENERGY_TOLERANCE = 1d;
  private static final double DELTA_TOLERANCE = 0.1d;
  private static final int MAX_ENERGY_ITERATION = 500;
  private static final int MAX_DELTA_ITERATION = 50;
  private static final double SCALE_FOR_MARGIN = 0.9d;
  
  private double K = 1; // Not specified in the paper
  private double L; // L=L0/max_ij, computed from the size of the graph and the max distance
  private double L0;
  private int width;
  private int height;
  private DirectedGraph<T> graph;
  private DijkstraShortestPath<T> shortestPath;
  private double d[][];
  
  public KamadaKawai(DirectedGraph<T> graph, int maxWidth, int maxHeight) {
    this.graph = graph;
    this.width = (int)(maxWidth * SCALE_FOR_MARGIN);
    this.height = (int)(maxHeight * SCALE_FOR_MARGIN);
    L0 = Math.min(this.width, this.height);
  }
  
  public void compute() {
    shortestPath = new DijkstraShortestPath<>(graph);
    L = L0/shortestPath.getMaxDist();
    d = shortestPath.getDistances();
    
    // Temporary for debug
    System.out.println(graph);
    System.out.println(shortestPath);
    
    computeStartingCoordinates();

    double E = E();
    double currentE;
    int energyIteration = 0;

    do {
      currentE = E;
      energyIteration ++;

      // let p_m be the particle satisfying Delta_m = max i Delta i
      double maxDelta_m = 0;
      int p_m = -1; 
      for (int i = 0; i < graph.V(); i++) {
        double Delta_m = Delta_m(i);

        if (maxDelta_m < Delta_m) {
          maxDelta_m = Delta_m;
          p_m = i;
        }
      }

      int iter = 0;
      while (maxDelta_m > DELTA_TOLERANCE && iter < MAX_DELTA_ITERATION) {
        Node v_p_m = graph.getNode(p_m);
        Delta delta_m = DxDy(p_m);
        v_p_m.setX((int)(v_p_m.getX() + delta_m.dx));
        v_p_m.setY((int)(v_p_m.getY() + delta_m.dy));

        maxDelta_m = Delta_m(p_m);
        iter++;
      }
      E = E();
    } while (Math.abs(currentE-E) > ENERGY_TOLERANCE && energyIteration < MAX_ENERGY_ITERATION);
  }

  /**
   * Assign a starting location to each node at random
   */
  private void computeStartingCoordinates() {
    // Assign random positions in the graph to the nodes
    Random random = new Random(System.currentTimeMillis());
    for (int i = 0; i < graph.V(); i++) {
      Node n_i = graph.getNode(i);
      n_i.setX(random.nextInt(width));
      n_i.setY(random.nextInt(height));
    }
  }
  
  /**
   * Kawai Kamada eq (5)
   * 
   * @return the energy of the model
   */
  private double E() {
    double e = 0;
    for (int i = 0; i < graph.V() - 1; i++) {
      Node v_i = graph.getNode(i);
      for (int j = i + 1; j < graph.V(); j++) {
        Node v_j = graph.getNode(j);
        double l_ij = l_ij(i, j);
        double k_ij = k_ij(i, j);
        double dx_ij = v_i.getX() - v_j.getX();
        double dy_ij = v_i.getY() - v_j.getY();

        e += k_ij/2 * (dx_ij*dx_ij + dy_ij*dy_ij + l_ij*l_ij - 2*l_ij * Math.sqrt(dx_ij*dx_ij + dy_ij*dy_ij));
      }
    }
    return e;
  }
  
  /**
   * Kamada Kaway eq (7), (8), (9).
   * 
   * @param m is the vertex index
   * @return the Delta_m
   */
  private double Delta_m(int m) {
    double deriv_E_over_dx_m = 0;
    double deriv_E_over_dy_m = 0;
    Node v_m = graph.getNode(m);
    for (int i = 0; i < graph.V(); i++) {
      if (i != m) {
        Node v_i = graph.getNode(i);
        double l_mi = l_ij(m, i);
        double k_mi = k_ij(m, i);
        double dx_mi = v_m.getX() - v_i.getX();
        double dy_mi = v_m.getY() - v_i.getY();
        double dist = Math.sqrt(dx_mi*dx_mi + dy_mi*dy_mi);
        deriv_E_over_dx_m += k_mi * (dx_mi - (l_mi * dx_mi)/dist); // (7)
        deriv_E_over_dy_m += k_mi * (dy_mi - (l_mi * dy_mi)/dist); // (8)
      }
    }
    return Math.sqrt(deriv_E_over_dx_m*deriv_E_over_dx_m + deriv_E_over_dy_m*deriv_E_over_dy_m); // eq (9)
  }

  /**
   * Kamada Kawai eq (13) through (16)
   * @param m
   * @return
   */
  private Delta DxDy(int m) {
    double dE_dx_m = 0;
    double dE_dy_m = 0;
    double d2E_d2x_m = 0;
    double d2E_dx_m_dy_m = 0;
    double d2E_dy_m_dx_m = 0;
    double d2E_d2y_m = 0;
    Node v_m = graph.getNode(m);

    for (int i = 0; i < graph.V(); i++) {
      if (i != m) {
        Node v_i = graph.getNode(i);
        double l_mi = l_ij(m, i);
        double k_mi = k_ij(m, i);
        double dx = v_m.getX() - v_i.getX();
        double dy = v_m.getY() - v_i.getY();
        double d = Math.sqrt(dx*dx + dy*dy);
        double dCube = d*d*d;

        dE_dx_m += k_mi * (1 - l_mi / d) * dx;
        dE_dy_m += k_mi * (1 - l_mi / d) * dy;
        d2E_d2x_m += k_mi * (1 - l_mi * dy * dy / dCube);
        d2E_dx_m_dy_m += k_mi * l_mi * dx * dy / dCube;
        d2E_d2y_m += k_mi * (1 - l_mi * dx * dx / dCube);
      }
    }
    d2E_dy_m_dx_m = d2E_dx_m_dy_m;

    double delta_x = (d2E_dx_m_dy_m * dE_dy_m - d2E_d2y_m * dE_dx_m) / d2E_d2x_m * d2E_d2y_m - d2E_dx_m_dy_m * d2E_dy_m_dx_m;
    double delta_y = (d2E_dy_m_dx_m * dE_dx_m - d2E_d2x_m * dE_dy_m) / d2E_d2x_m * d2E_d2y_m - d2E_dx_m_dy_m * d2E_dy_m_dx_m;
    return new Delta(delta_x, delta_y);
  }

  private double l_ij(int i, int j) {
    double dist = d[i][j];
    
    if (dist == Double.POSITIVE_INFINITY) {
      dist = shortestPath.getMaxDist();
    }
    return L * dist;
  }

  private double k_ij(int i, int j) {
    double dist = d[i][j];
    
    if (dist == Double.POSITIVE_INFINITY) {
      dist = shortestPath.getMaxDist();
    }
    return K / (dist*dist);
  }

  private static class Delta {
    protected double dx;
    protected double dy;
    
    public Delta(double dx, double dy) {
      super();
      this.dx = dx;
      this.dy = dy;
    }
  }
}
