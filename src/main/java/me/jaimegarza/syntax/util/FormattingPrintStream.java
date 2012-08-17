/*
 ===============================================================================
 Copyright (c) 1985, 2012, Jaime Garza
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
     * Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.
     * Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.
     * Neither the name of Jaime Garza nor the
       names of its contributors may be used to endorse or promote products
       derived from this software without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ===============================================================================
*/
package me.jaimegarza.syntax.util;

import java.io.OutputStream;
import java.io.PrintStream;

import me.jaimegarza.syntax.cli.Environment;

public class FormattingPrintStream extends PrintStream {

  private Environment environment;
  
  public void printFragment(String key, Object... objects) {
    String fragment = environment.formatFragment(key, objects);
    print(fragment);
  }

  public void printlnFragment(String key, Object... objects) {
    printFragment(key, objects);
    println();
  }

  public void printIndentedFragment(String key, int indent, Object... objects) {
    Object newObjects[] = new Object[objects.length+1];
    for (int i = 0; i < objects.length; i++) {
      newObjects[i+1] = objects[i];
    }
    newObjects[0] = environment.language.indent(indent);
    printFragment(key, newObjects);
  }

  public void printlnIndentedFragment(String key, int indent, Object... objects) {
    printIndentedFragment(key, indent, objects);
    println();
  }

  /**
   * Construct a formatting print stream from an output stream
   * @param out the output stream
   */
  public FormattingPrintStream(Environment environment, OutputStream out) {
    super(out);
    this.environment = environment;
  }

  /**
   * @return the environment
   */
  public Environment getEnvironment() {
    return environment;
  }

  /**
   * @param environment
   *          the environment to set
   */
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

}
