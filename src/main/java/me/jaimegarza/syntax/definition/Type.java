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
package me.jaimegarza.syntax.definition;

import java.util.LinkedList;
import java.util.List;

/**
 * <i>~pojo class</i><br><br>
 * 
 * defines the convenience type used for $$, $1, $2 etc to be used in code
 * generation rules.  This applies to both terminals and non terminals.
 * 
 * @author jaimegarza@gmail.com
 *
 */public class Type {
   /**
    * The type name.  
    * It is up to the user to make sure that it is syntactically valid in 
    * the target language.
    */
  String name;
  List<Symbol> usedBy = new LinkedList<Symbol>();

  public void addUsage(Symbol symbol) {
    if (!usedBy.contains(symbol)) {
      usedBy.add(symbol);
    }
  }
  
  /** 
   * Construct a type
   * @param name is the type name
   */
  public Type(String name) {
    super();
    this.name = name;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    try {
      Type t = (Type) obj;
      return name.equals(t.name);
    } catch (NullPointerException unused) {
      return false;
    } catch (ClassCastException unused) {
      return false;
    }
  }

  /**
   * Returns the name of the type
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String s = name;
    if (usedBy.size() > 0) {
      s += "\n    Used by:";
      for (Symbol symbol : usedBy) {
        s += "\n    +-- " + symbol;
      }
    }
    return s;
  }

}
