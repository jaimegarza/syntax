/*
Syntax is distibuted under the Revised, or 3-clause BSD license
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
package me.jaimegarza.syntax.model.nfa;

import java.util.HashSet;
import java.util.Set;

public class CharacterClass {
  
  private boolean negate;
  private Set<CharacterRange> ranges = new HashSet<>();
  
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
  
  public void addRange(char from, char to) {
    CharacterRange r = new CharacterRange(from, to);
    addRange(r);
  }
  
  public void addRange(char c) {
    CharacterRange r = new CharacterRange(c);
    addRange(r);
  }
  
  public void addRange(CharacterRange r) {
    if (!ranges.contains(r)) {
      ranges.add(r);
    }
  }
  
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
  public String toString() {
    return "CharacterClass [negate=" + negate + ", ranges=" + ranges + "]";
  }

}
