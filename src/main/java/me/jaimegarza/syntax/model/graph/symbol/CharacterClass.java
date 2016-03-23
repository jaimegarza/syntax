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
package me.jaimegarza.syntax.model.graph.symbol;

import java.util.HashSet;
import java.util.Set;

/**
 * A character class symbol. A character class is composed of multiple ranges
 * @author jgarza
 *
 */
public class CharacterClass extends RegexSymbol {
  /** if this is a negate character class */
  private boolean negate;
  /** the ranges in the character class */
  private Set<CharacterRange> ranges = new HashSet<CharacterRange>();
  
  @Override
  public boolean matches(char c) {
    boolean matches = false;
    for (CharacterRange r: ranges) {
      if (r.matches(c)) {
        matches = true;
        break;
      }
    }
    return negate? !matches: matches;
  }
  
  /**
   * Make this character class a negate class
   */
  public void negate() {
    this.negate = true;
  }
  
  /**
   * Add a range to the character class
   * @param r the range
   */
  public void range(CharacterRange r) {
    if (!ranges.contains(r)) {
      ranges.add(r);
    }
  }
  
  /**
   * Add a range to the character class
   * @param from the starting character
   * @param to the ending character
   */
  public void range(char from, char to) {
    CharacterRange r = new CharacterRange(from, to);
    range(r);
  }
  
  /**
   * Add a range to the character class
   * @param c, the from and to character
   */
  public void character(char c) {
    CharacterRange r = new CharacterRange(c);
    range(r);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    try {
      CharacterClass cc = (CharacterClass) o;
      return negate == cc.negate && ranges.equals(cc.ranges);
    } catch (NullPointerException unused) {
      return false;
    } catch (ClassCastException unused) {
      return false;
    }

  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int hash = prime + (negate ? 1 : 0);
    for (CharacterRange r : ranges) {
      hash = hash * prime + r.hashCode();
    }
    return hash;
  }
  
  @Override
  public boolean isEpsilon() {
	  return false;
  }

  @Override
  public String canonical() {
    return "[" + toString() + "]";
  }
  
  @Override
  public int code() {
    return ranges.size();
  }

  @Override
  public int sizeof() {
    return 1 + 2 * ranges.size(); // the code, plus 2 integers per range.
  }

  @Override
  public int[] getCodeArray() {
    int[] rc = new int[ranges.size()*2];
    int i = 0;
    for (CharacterRange range: ranges) {
      rc[i++] = range.getFrom();
      rc[i++] = range.getTo();
    }
    return rc;
  }
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (negate) {
      sb.append("^");
    }
    for (CharacterRange r : ranges) {
      sb.append(r);
    }
    return sb.toString();
  }

  public boolean isNegate() {
    return negate;
  }

}
