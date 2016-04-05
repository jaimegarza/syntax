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
package me.jaimegarza.syntax.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.jaimegarza.syntax.model.graph.DirectedGraph;
import me.jaimegarza.syntax.model.graph.Node;
import me.jaimegarza.syntax.model.graph.Transition;

public class SvgRenderer {

  public String render(DirectedGraph<? extends Node> graph, int width, int height) {
    SvgCanvas canvas = new SvgCanvas(width, height);
    
    for (Node node : graph.getNodes()) {
      // Do straight arrows first to compute best arc in the reentrant nodes
      for (Transition transition: node.getTransitions()) {
        if (transition.getTo() != node) {
          Connection c = canvas.transitionNodeToNode(transition);
          registerAngle(node, c.getFromAngle());
          registerAngle(transition.getTo(), c.getToAngle());
        }
      }
      // Now do arcs in best posible way
      for (Transition transition: node.getTransitions()) {
        if (transition.getTo() == node) {
          double angle = computeBestAngle(node);
          canvas.transitionToSelf(transition, angle);
        }
      }
      canvas.node(node);
    }
    return canvas.getGraph();
  }
  
  private Map<Node, List<Double>> angles = new HashMap<>();
  
  private List<Double> getNodeAngles(Node n) {
    List<Double> dl = angles.get(n);
    if (dl == null) {
      dl = new ArrayList<>();
      angles.put(n, dl);
    }
    return dl;
  }
  
  private void registerAngle(Node n, double angle) {
    while (angle > 2 * Math.PI) {
      angle -= 2 * Math.PI;
    }
    while (angle < 0) {
      angle += 2 * Math.PI;
    }
    List<Double> dl = getNodeAngles(n);
    dl.add(angle);
  }
  
  private double computeBestAngle(Node n) {
    List<Double> dl = getNodeAngles(n);
    
    if (dl.size() == 0) {
      return Math.random() * 2 * Math.PI;
    }
    
    Collections.sort(dl);
    double maxRange = 0;
    int index = -1;
    
    for (int i = 0; i < dl.size() - 1; i++) {
      double x = dl.get(i);
      double y = dl.get(i+1);
      double delta = y - x;
      if (delta > maxRange) {
        maxRange = delta;
        index = i;
      }
    }
    
    double x = dl.get(dl.size()-1);
    double y = dl.get(0) + 2 * Math.PI;
    double delta = y - x;
    if (delta > maxRange) {
      maxRange = delta;
      index = dl.size()-1;
    }
    
    if (index == -1) {
      return Math.PI / 2;
    }
    
    double openAngle = dl.get(index) + maxRange/2;
    if (openAngle > 2 * Math.PI) {
      openAngle -= 2 * Math.PI;
    }
    
    return openAngle;
  }
}
