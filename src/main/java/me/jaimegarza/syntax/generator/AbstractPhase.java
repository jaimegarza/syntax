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
package me.jaimegarza.syntax.generator;

import me.jaimegarza.syntax.env.Environment;
import me.jaimegarza.syntax.env.RuntimeData;
import me.jaimegarza.syntax.util.FormattingPrintStream;
import me.jaimegarza.syntax.util.HtmlWriter;
import me.jaimegarza.syntax.util.HtmlWriter.HtmlValue;

/**
 * Base clase for all phases.  Common routines will be placed here
 *
 * @author jaimegarza@gmail.com
 *
 */
public abstract class AbstractPhase {
  /**
   * Every phase needs to have the environment defined
   */
  protected Environment environment;
  /**
   * Every phase needs to have the runtime defined.
   */
  protected RuntimeData runtimeData;
  
  public AbstractPhase(Environment environment) {
    this.environment = environment;
    this.runtimeData = environment.getRuntimeData();
  }

  /**
   * Outputs the proper number of spaces, as needed by param n.<p>
   * An indentation is four spaces for C, and 2 for java and pascal
   * 
   * @param out is the output stream to be written to
   * @param n is the number of indentations.
   */
  protected void indent(FormattingPrintStream out, int n) {
    environment.language.indent(out, n);
  }
  
  /**
   * Format a report entry for left alignment
   * @param o is the object to left align
   * @return a representation of the left-aligned value
   */
  protected HtmlValue left(Object o) {
    return HtmlWriter.left(o);
  }

  /**
   * Format a report entry for right alignment
   * @param o is the object to right align
   * @return a representation of the right-aligned value
   */
  protected HtmlValue right(Object o) {
    return HtmlWriter.right(o);
  }
  
  /**
   * Produce one span
   * @param s is the span contents
   * @param className is the optional CSS class name, can be null
   * @return the span
   */
  protected String span(String s, String className) {
    return "<span" + (className != null ? " class=\"" + className + "\"": "") + ">" + s + "</span>";
  }

  /**
   * Produce one span
   * @param s is the span contents
   * @return the span
   */
  protected String span(String s) {
    return span(s, null);
  }
}
