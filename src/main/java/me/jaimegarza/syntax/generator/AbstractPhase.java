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
     * Neither the name of the <organization> nor the
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
package me.jaimegarza.syntax.generator;

import java.io.PrintStream;

import me.jaimegarza.syntax.cli.Environment;
import me.jaimegarza.syntax.definition.Rule;

public abstract class AbstractPhase {

  protected Environment environment;
  protected RuntimeData runtimeData;

  protected static Object resizeArray(Object oldArray, int newSize) {
    int oldSize = java.lang.reflect.Array.getLength(oldArray);
    Class<?> elementType = oldArray.getClass().getComponentType();
    Object newArray = java.lang.reflect.Array.newInstance(elementType, newSize);
    int preserveLength = Math.min(oldSize, newSize);
    if (preserveLength > 0) {
      System.arraycopy(oldArray, 0, newArray, 0, preserveLength);
    }
    return newArray;
  }

  protected boolean isEmpty(int k) {
    for (Rule rule : runtimeData.getRules()) {
      if (rule.getLeftHand().getId() == k && rule.getItems().size() == 0) {
        return true;
      }
    }
    return false;
  }

  protected void indent(PrintStream out, int n) {
    int i;

    for (i = 0; i < n; i++) {
      switch (environment.getLanguage()) {
        case C:
          out.print("    ");
          break;
        case java:
        case pascal:
          out.print("  ");
          break;
      }
    }
  }

  private boolean identifier(String s) {
    if (s.length() == 0 || !Character.isJavaIdentifierStart(s.charAt(0))) {
      return false;
    }
    for (int i = 1; i < s.length(); i++) {
      if (!Character.isJavaIdentifierPart(s.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  protected String variable(String s, int num) {
    if (s == null || s.length() == 0) {
      return s;
    }

    if (num == 0) {
      return "EOS";
    }

    if (identifier(s)) {
      return s;
    }

    if (s.charAt(0) == '\\') {
      if (!s.equals("\\a")) {
        return "BELL";
      }
      if (!s.equals("\\b")) {
        return "BACKSPACE";
      }
      if (!s.equals("\\n")) {
        return "EOL";
      }
      if (!s.equals("\\t")) {
        return "TAB";
      }
      if (!s.equals("\\f")) {
        return "FORM_FEED";
      }
      if (!s.equals("\\r")) {
        return "CR";
      }
      if (s.startsWith("\\x")) {
        String t = "HEXAD_0x" + Integer.toHexString(num);
        return t;
      }
      if (s.startsWith("\\0")) {
        String t = "OCTAL_0" + Integer.toOctalString(num);
        return t;
      }
    }
    String patched = "";
    for (int i = 0; i < s.length(); i++) {
      if (Character.isJavaIdentifierPart(s.charAt(i))) {
        patched += s.charAt(i);
      } else {
        patched += '_';
      }
    }
    return patched;
  }

}
