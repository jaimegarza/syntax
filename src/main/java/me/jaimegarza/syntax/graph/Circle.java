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

public class Circle {
  double x;
  double y;
  double left;
  double r;

  /**
   * Construct a circle with center in x,y and radius r
   * @param x the x coordinate
   * @param y the y coordinate
   * @param r the radius
   */
  public Circle(double x, double y, double r) {
    this.x = x;
    this.y = y;
    this.r = r;
    this.left = x - r;
  }
  
  /**
   * Compute the intersection points of two touching circles
   * @param c is the other circle
   * @return two points
   */
  public Pair<Point> intersect(Circle c) {
    double d;
    double a;
    double h;
    double x3;
    double y3;
    double x4;
    double y4;

    Point P0 = new Point(x, y);
    Point P1 = new Point(c.x, c.y);
    
    d = P0.distance(P1);
    a = (r*r - c.r*c.r + d*d)/(2*d);
    h = Math.sqrt(r*r - a*a);
    Point P2 = P1.subtract(P0).scale(a/d).add(P0);
    x3 = P2.getX() + h*(P1.getY() - P0.getY())/d;
    y3 = P2.getY() - h*(P1.getX() - P0.getX())/d;
    x4 = P2.getX() - h*(P1.getY() - P0.getY())/d;
    y4 = P2.getY() + h*(P1.getX() - P0.getX())/d;

    return new Pair<Point>(new Point(x3, y3), new Point(x4, y4));
  }

  /**
   * @return the x
   */
  public double getX() {
    return x;
  }

  /**
   * @return the y
   */
  public double getY() {
    return y;
  }

  /**
   * @return the r
   */
  public double getR() {
    return r;
  }
}
