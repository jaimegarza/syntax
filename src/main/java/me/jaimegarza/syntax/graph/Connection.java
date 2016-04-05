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

/**
 * Representation of a source and origin point
 * @author jgarza
 *
 */
public class Connection {
  
  private Point from;
  private double fromAngle;
  private Point to;
  private double toAngle;

  public Connection(Point from, double fromAngle, Point to, double toAngle) {
    this.from = from;
    this.fromAngle = fromAngle;
    this.to = to;
    this.toAngle = toAngle;
  }

  /**
   * @return the x
   */
  public Point getFrom() {
    return from;
  }

  /**
   * @return the y
   */
  public Point getTo() {
    return to;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    try {
      Connection c = (Connection) obj;
      return from.equals(c.from) && to.equals(c.to);
    } catch (NullPointerException unused) {
      return false;
    } catch (ClassCastException unused) {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return 31 + from.hashCode() + to.hashCode();
  }

  @Override
  public String toString() {
    return "{" + from + "->" + to + "}";
  }

  /**
   * @return the fromAngle
   */
  public double getFromAngle() {
    return fromAngle;
  }

  /**
   * @return the toAngle
   */
  public double getToAngle() {
    return toAngle;
  }

}

