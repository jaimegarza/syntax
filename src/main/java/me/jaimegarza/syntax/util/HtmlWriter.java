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
package me.jaimegarza.syntax.util;

/**
 * Class used to write HTML out for the report.
 * <p>
 * It allows the constructions of tables and other reporting structures
 * 
 * @author jgarza
 *
 */
public class HtmlWriter {

  private FormattingPrintStream out;
  private int currentNumberOfColumns = 0;

  /**
   * Class utilized to hold a value with a CSS class
   * @author jgarza
   *
   */
  public static class HtmlValue {
    public Object value;
    public String className;
    
    public HtmlValue(Object value, String className) {
      super();
      this.value = value;
      this.className = className;
    }
    
  }
  
  /**
   * Construct a writer to output to the out stream
   * @param out is the stream to write to
   */
  public HtmlWriter(FormattingPrintStream out) {
    this.out = out;
  }
  
  /**
   * Write the heading of a column to HTML
   * @param type is the CSS classname of the table
   * @param columnHeadersAndClasses is the column descriptors
   */
  public void tableHead(String type, HtmlValue... columnHeadersAndClasses) {
    out.println("  <table class=\"" + type + "\">");
    
    tableElement("th", columnHeadersAndClasses);
  }

  /**
   * Write the contents of one row of data as per the values
   * @param valuesAndClasses is the array of values with their CSS classes
   */
  public void tableRow(HtmlValue... valuesAndClasses) {
    tableElement("td", valuesAndClasses);
  }
  
  public void tableOneCellRow(String value, String className) {
    out.print("    <tr><td");
    if (className != null) {
      out.print(" class=\"" + className + "\"");
    }
    out.println(" colspan=\"" + currentNumberOfColumns + "\">" + value + "</td></tr>");
  }
  
  public void tableEnd() {
    out.println("  </table>");
    out.println();
  }
  
  public void heading(String text) {
    out.println("<div class=\"heading\">" + text + "</div>");
  }

  public void subHeading(String text) {
    out.println("<div class=\"subheading\">" + text + "</div>");
  }

  public void title(String text) {
    out.println("<div class=\"title\">" + text + "</div>");
  }

  public void subTitle(String text) {
    out.println("<div class=\"subtitle\">" + text + "</div>");
  }

  public void item(String text) {
    out.println("<div class=\"item\">" + text + "</div>");
  }

  public void item(String text, String subtext) {
    out.println("<div class=\"item\">" + text + "<span class=\"subitem\">" + subtext + "</span>"+ "</div>");
  }
  
  public static HtmlValue left(Object o) {
    return new HtmlValue(o, "left");
  }

  public static HtmlValue right(Object o) {
    return new HtmlValue(o, "right");
  }

  private void tableElement(String cellType, HtmlValue... valuesAndClasses) {
    out.print("    <tr>");
    currentNumberOfColumns = 0;
    
    for (HtmlValue valueAndClass : valuesAndClasses) {
      currentNumberOfColumns++;
      out.print("<" + cellType);
      if (valueAndClass.className != null) {
        out.print(" class=\"" + valueAndClass.className + "\"");
      }
      out.print(">" + valueAndClass.value.toString() + "</" + cellType + ">");
    }
    out.println("</tr>");
  }
}
