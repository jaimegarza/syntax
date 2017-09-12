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

import me.jaimegarza.syntax.env.Environment;
import me.jaimegarza.syntax.model.graph.Node;
import me.jaimegarza.syntax.model.graph.Transition;

/**
 * Allows drawing of SVG in a simulated canvas. Outputs SVG
 * @author jgarza
 *
 */
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
  private static final double SELF_LOOP_TEXT_OFFSET = 29;
  private static final double SELF_LOOP_ARROW_LENGTH = 8;
  private static final int EXTRA_WIDTH = 25;
  private static final int EXTRA_HEIGHT = 25;
  
  public SvgCanvas(int width, int height) {
    this.width = width;
    this.height = height;
  }
  
  /**
   * Produce the graph for a node
   * @param n is the node
   */
  public void node(Node n) {
    circle(n.isStarting() ? "g-starting-node" : "g-node", n.getX(), n.getY(), NODE_RADIUS);
    if (n.isAccept()) {
      circle("g-accept", n.getX(), n.getY(), ACCEPT_RADIUS);
    }
    text("g-node-text", new Point(n.getX() + NODE_TEXT_OFFSET_X, n.getY() + NODE_TEXT_OFFSET_Y), "" + n.getId(), "middle", "middle");
  }
  
  /**
   * Arrow between two nodes
   * @param t the transition
   * @return how they connect in a connection object
   */
  public Connection transitionNodeToNode(Transition t) {
    Point pFrom = new Point(t.getFrom().getX(), t.getFrom().getY());
    Point pTo = new Point(t.getTo().getX(), t.getTo().getY());
    
    LineData coords = nodeToNodeArrow("g-transition", pFrom, pTo, NODE_RADIUS);
    double angle = coords.d;
    
    if (Environment.getCurrentEnvironment().isDebug()) {
      System.out.println("From " + t.getFrom().getId() + pFrom + " to " + t.getTo().getId() + pTo + " angle " + angle);
    }
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
    text("g-transition-text", new Point(textX, textY), t.getSymbol().toHtmlString(), textAnchor, "middle");
    
    double angle2 = angle + Math.PI;
    if (angle2 > 2 * Math.PI) {
      angle2 -= 2 * Math.PI;
    }
    return new Connection(coords.p1, angle, coords.p2, angle2);
  }
  
  /**
   * Self loop. Connects with an excentric circle and computes the arrow
   * @param t is the transition.
   * @param angle the best angle from the node to the excentric circle
   */
  public void transitionToSelf(Transition t, double angle) {
    Point loopCenter = new Point(SELF_LOOP_OFFSET * Math.cos(angle) + t.getFrom().getX(),
        SELF_LOOP_OFFSET * Math.sin(angle) + t.getFrom().getY());
    
    Circle c_n = new Circle(t.getFrom().getX(), t.getFrom().getY(), NODE_RADIUS);
    Circle c_l = new Circle(loopCenter.getX(), loopCenter.getY(), SELF_LOOP_RADIUS);
    Circle c_a = new Circle(t.getFrom().getX(), t.getFrom().getY(), NODE_RADIUS + SELF_LOOP_ARROW_LENGTH);
    Pair<Point> pair_n = c_n.intersect(c_l);
    Point p_n = pair_n.getFirst();
    Pair<Point> pair_a = c_a.intersect(c_l);
    Point p_a = pair_a.getFirst();
    
    circle("g-self-loop", loopCenter.getX(), loopCenter.getY(), SELF_LOOP_RADIUS);

    double arrowAngle = Math.atan2(- (p_a.getX() - p_n.getX()), p_a.getY() - p_n.getY());
    Point p_w1 = new Point(Math.cos(arrowAngle) * SELF_LOOP_ARROW_LENGTH / 2 + p_a.getX(), Math.sin(arrowAngle) * SELF_LOOP_ARROW_LENGTH / 2 + p_a.getY());
    Point p_w2 = new Point(-Math.cos(arrowAngle) * SELF_LOOP_ARROW_LENGTH / 2 + p_a.getX(), -Math.sin(arrowAngle) * SELF_LOOP_ARROW_LENGTH / 2 + p_a.getY());
    
    arrowHead("g-self-loop", p_w1, p_n, p_w2);

    Point txt = new Point(SELF_LOOP_TEXT_OFFSET * Math.cos(angle) + t.getFrom().getX(),
        SELF_LOOP_TEXT_OFFSET * Math.sin(angle) + t.getFrom().getY());
    
    String textAlignment;
    String alignmentBaseline;
    
    if (angle < 0) {
      angle += 2 * Math.PI;
    }
    if (angle > 0 && angle < Math.PI/2) {
      textAlignment = "start";
      alignmentBaseline = "middle";
    } else if (angle >= Math.PI/2 && angle < Math.PI) {
      textAlignment = "end";
      alignmentBaseline = "middle";
    } else if (angle >= Math.PI && angle < 3*Math.PI/2) {
      textAlignment = "end";
      alignmentBaseline = "middle";
    } else {
      textAlignment = "start";
      alignmentBaseline = "middle";
    }
    text("g-transition-text", txt, t.getSymbol().toHtmlString(), textAlignment, alignmentBaseline);
  }
  
  /**
   * Return the whole graph as SVG
   * @return SVG
   */
  public String getGraph() {
    return "<svg width=\"" + (width + EXTRA_WIDTH) + "\" height=\"" + (height + EXTRA_HEIGHT) + "\">\n"
        + instructions
        + "</svg>\n";
  }
  
  private LineData nodeToNodeArrow(String className, Point p1, Point p2, double r) {
    double angle = Math.atan2(p2.getY()-p1.getY(), p2.getX()-p1.getX());
    
    // first circle
    Point c1_1 = new Point(r * Math.cos(angle) + p1.getX(), r * Math.sin(angle) + p1.getY());
    Point c1_2 = new Point(r * Math.cos(angle+Math.PI) + p1.getX(), r * Math.sin(angle+Math.PI) + p1.getY());

    // second circle
    Point c2_1 = new Point(r * Math.cos(angle) + p2.getX(), r * Math.sin(angle) + p2.getY());
    Point c2_2 = new Point(r * Math.cos(angle+Math.PI) + p2.getX(), r * Math.sin(angle+Math.PI) + p2.getY());
    
    LineData ld = new LineData(c1_1, c2_1, c1_1.distance(c2_1));
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

  //----------------------------------------------------------
  //                     PRIMITIVES
  //----------------------------------------------------------
  private void circle(String className, double x, double y, double radius) {
    instructions +=
      String.format("  <circle class=\"%s\" cx=\"%.2f\" cy=\"%.2f\" r=\"%f\"/>\n", className, x + LEFT_MARGIN, y + TOP_MARGIN, radius);
  }

  private void text(String className, Point p, String text, String textAnchor, String alignmentBaseline) {
    instructions +=
      String.format("  <text class=\"%s\" x=\"%.2f\" y=\"%.2f\" alignment-baseline=\"%s\" text-anchor=\"%s\">%s</text>\n", className, p.getX() + LEFT_MARGIN, p.getY() + TOP_MARGIN, alignmentBaseline, textAnchor, text);
  }
  
  private void arrowHead(String className, Point p2, double angle, Point c2) {
    double alphaAngle1 = angle + Math.PI - ARROWHEAD_ANGLE;
    double alphaAngle2 = angle + Math.PI + ARROWHEAD_ANGLE;
    
    Point arrowEdge1 = new Point(ARROWHEAD_LENGTH * Math.cos(alphaAngle1) + p2.getX(),
        ARROWHEAD_LENGTH * Math.sin(alphaAngle1) + p2.getY());
        
    Point arrowEdge2 = new Point(ARROWHEAD_LENGTH * Math.cos(alphaAngle2) + p2.getX(),
        ARROWHEAD_LENGTH * Math.sin(alphaAngle2) + p2.getY());
    
    arrowHead(className, arrowEdge1, c2, arrowEdge2);
  }
  
  private void arrowHead(String className, Point p1, Point p2, Point p3) {
    instructions += 
        String.format("  <polygon class=\"%s-head\" points=\"%.2f,%.2f %.2f,%.2f %.2f,%.2f\"/>\n", 
            className, p1.getX() + LEFT_MARGIN, p1.getY() + TOP_MARGIN, 
            p2.getX() + LEFT_MARGIN, p2.getY() + TOP_MARGIN, 
            p3.getX() + LEFT_MARGIN, p3.getY() + TOP_MARGIN);
  }
  
  private LineData computeMin(LineData ld, Point p1, Point p2) {
    double d = p1.distance(p2);
    if (d < ld.d) {
      return new LineData(p1, p2, d);
    } else {
      return ld;
    }
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
