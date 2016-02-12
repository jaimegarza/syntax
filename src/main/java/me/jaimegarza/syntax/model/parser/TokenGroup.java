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
package me.jaimegarza.syntax.model.parser;

import java.util.List;

public class TokenGroup {
  /**
   * The name of the group
   */
  String name;
  /**
   * The display name for error messages
   */
  String displayName;
  /**
   * The tokens that make the group
   */
  List<Terminal> tokens;
  
  /**
   * Construct a fully defined group
   * @param tokens the list of tokens
   * @param name the name of the group
   * @param displayName the display name in error messages for the group.
   */
  public TokenGroup(List<Terminal> tokens, String name, String displayName) {
    this.tokens = tokens;
    this.name = name;
    this.displayName = displayName;
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
   * @return the displayName
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * @param displayName the displayName to set
   */
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   * @return the tokens
   */
  public List<Terminal> getTokens() {
    return tokens;
  }

  /**
   * @param tokens the tokens to set
   */
  public void setTokens(List<Terminal> tokens) {
    this.tokens = tokens;
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
      TokenGroup t = (TokenGroup) obj;
      return name.equals(t.name);
    } catch (NullPointerException unused) {
      return false;
    } catch (ClassCastException unused) {
      return false;
    }
  }
  /**
   * Returns the name of the group, and its tokens
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String s = name + ":\"" + displayName + "\"";
    if (tokens.size() > 0) {
      s += "\n    Tokens:";
      for (Terminal symbol : tokens) {
        s += "\n    +-- " + symbol.toString();
      }
    }
    return s;
  }


}
