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

import me.jaimegarza.syntax.model.graph.Node;
import me.jaimegarza.syntax.model.graph.Transition;

public class SvgCanvas {
  private int width;
  private int height;
  private String instructions = "";
  
  private static final double NODE_RADIUS = 8;
  private static final double ACCEPT_RADIUS = 6;
  private static final double NODE_TEXT_OFFSET_Y = 14;
  private static final double NODE_TEXT_OFFSET_X = 0;
  private static final int LEFT_MARGIN = 25;
  private static final int TOP_MARGIN=25;
  private static final double TRANSITION_TEXT_OFFSET_Y = 10;
  private static final double TRANSITION_TEXT_OFFSET_X = 0;
  private static final double ARROWHEAD_ANGLE = 10 * Math.PI / 180;
  private static final double ARROWHEAD_LENGTH = 20;
  private static final double SELF_LOOP_OFFSET = 12;
  private static final double SELF_LOOP_RADIUS = 12;
  
  public SvgCanvas(int width, int height) {
    this.width = width;
    this.height = height;
  }
  
  public void node(Node n) {
    circle(n.isStarting() ? "g-starting-node" : "g-node", n.getX(), n.getY(), NODE_RADIUS);
    if (n.isAccept()) {
      circle("g-accept", n.getX(), n.getY(), ACCEPT_RADIUS);
    }
    text("g-node-text", new Point(n.getX() + NODE_TEXT_OFFSET_X, n.getY() + NODE_TEXT_OFFSET_Y), "" + n.getId(), "middle");
  }
  
  public Connection transitionNodeToNode(Transition t) {
    Point pFrom = new Point(t.getFrom().getX(), t.getFrom().getY());
    Point pTo = new Point(t.getTo().getX(), t.getTo().getY());
    
    LineData coords = nodeToNodeArrow("g-transition", pFrom, pTo, NODE_RADIUS);
    double angle = coords.d;
    
    System.out.println("From " + t.getFrom().getId() + pFrom + " to " + t.getTo().getId() + pTo + " angle " + angle);
    double textX = (pTo.getX()+pFrom.getX())/2;
    double textY = (pTo.getY()+pFrom.getY())/2;
    String textAnchor;
    
    if (angle > Math.PI) {
      textY -= TRANSITION_TEXT_OFFSET_Y;
    } else {
      textY += TRANSITION_TEXT_OFFSET_Y;
    }

    if (angle < Math.PI /2 || angle >= 3*Math.PI/2) {
      textX += TRANSITION_TEXT_OFFSET_X;
      textAnchor = "end";
    } else {
      textX -= TRANSITION_TEXT_OFFSET_X;
      textAnchor = "start";
    }
    text("g-transition-text", new Point(textX, textY), t.getSymbol().toHtmlString(), textAnchor);
    
    double angle2 = angle + Math.PI;
    if (angle2 > 2 * Math.PI) {
      angle2 -= 2 * Math.PI;
    }
    return new Connection(coords.p1, angle, coords.p2, angle2);
  }
  
  public void transitionToSelf(Transition t, double angle) {
    double x = SELF_LOOP_OFFSET * Math.cos(angle) + t.getFrom().getX();
    double y = SELF_LOOP_OFFSET * Math.sin(angle) + t.getFrom().getY();
    
    circle("g-self-loop", x, y, SELF_LOOP_RADIUS);
  }
  
  public String getGraph() {
    return "<svg width=\"" + width + "\" height=\"" + height + "\">\n"
        + instructions
        + "</svg>\n";
  }
  
  private void circle(String className, double x, double y, double radius) {
    instructions +=
      String.format("  <circle class=\"%s\" cx=\"%.2f\" cy=\"%.2f\" r=\"%f\"/>\n", className, x + LEFT_MARGIN, y + TOP_MARGIN, radius);
  }

  private void text(String className, Point p, String text, String textAnchor) {
    instructions +=
      String.format("  <text class=\"%s\" x=\"%.2f\" y=\"%.2f\" alignment-baseline=\"middle\" text-anchor=\"%s\">%s</text>\n", className, p.getX() + LEFT_MARGIN, p.getY() + TOP_MARGIN, textAnchor, text);
  }
  
  private LineData nodeToNodeArrow(String className, Point p1, Point p2, double r) {
    double angle = Math.atan2(p2.getY()-p1.getY(), p2.getX()-p1.getX());
    
    // first circle
    Point c1_1 = new Point(r * Math.cos(angle) + p1.getX(), r * Math.sin(angle) + p1.getY());
    Point c1_2 = new Point(r * Math.cos(angle+Math.PI) + p1.getX(), r * Math.sin(angle+Math.PI) + p1.getY());

    // second circle
    Point c2_1 = new Point(r * Math.cos(angle) + p2.getX(), r * Math.sin(angle) + p2.getY());
    Point c2_2 = new Point(r * Math.cos(angle+Math.PI) + p2.getX(), r * Math.sin(angle+Math.PI) + p2.getY());
    
    LineData ld = new LineData(c1_1, c2_1, distance(c1_1, c2_1));
    ld = computeMin(ld, c1_2, c2_1);
    ld = computeMin(ld, c1_1, c2_2);
    ld = computeMin(ld, c1_2, c2_2);
    
    Point c1 = ld.p1;
    Point c2 = ld.p2;
    
    //circle("xxx", c1.x, c1.y,3);
    //circle("xxx", c2.x, c2.y,3);
    instructions +=
        String.format("  <path class=\"%s\" d=\"M%.2f %.2f L%.2f %2f\"/>\n", className, 
            c1.getX() + LEFT_MARGIN, c1.getY() + TOP_MARGIN, c2.getX() + LEFT_MARGIN, c2.getY() + TOP_MARGIN);
    
    arrowHead(className, p2, angle, c2);
    return new LineData(c1, c2, angle);
  }

  private void arrowHead(String className, Point p2, double angle, Point c2) {
    double alphaAngle1 = angle + Math.PI - ARROWHEAD_ANGLE;
    double alphaAngle2 = angle + Math.PI + ARROWHEAD_ANGLE;
    
    Point arrowEdge1 = new Point(ARROWHEAD_LENGTH * Math.cos(alphaAngle1) + p2.getX(),
        ARROWHEAD_LENGTH * Math.sin(alphaAngle1) + p2.getY());
        
    Point arrowEdge2 = new Point(ARROWHEAD_LENGTH * Math.cos(alphaAngle2) + p2.getX(),
        ARROWHEAD_LENGTH * Math.sin(alphaAngle2) + p2.getY());

    instructions += 
        String.format("  <polygon class=\"%s-head\" points=\"%.2f,%.2f %.2f,%.2f %.2f,%.2f\"/>", 
            className, arrowEdge1.getX() + LEFT_MARGIN, arrowEdge1.getY() + TOP_MARGIN, 
            c2.getX() + LEFT_MARGIN, c2.getY() + TOP_MARGIN, 
            arrowEdge2.getX() + LEFT_MARGIN, arrowEdge2.getY() + TOP_MARGIN);
  }
  
  private LineData computeMin(LineData ld, Point p1, Point p2) {
    double d = distance(p1, p2);
    if (d < ld.d) {
      return new LineData(p1, p2, d);
    } else {
      return ld;
    }
  }
  
  private double distance(Point p1, Point p2) {
    return Math.sqrt((p2.getX()-p1.getX())*(p2.getX()-p1.getX()) + (p2.getY()-p1.getY())*(p2.getY()-p1.getY()));
  }
  
  private static class LineData {
    Point p1;
    Point p2;
    double d;
    
    public LineData(Point p1, Point p2, double d) {
      this.p1 = p1;
      this.p2 = p2;
      this.d = d;
    }
  }
  
}
