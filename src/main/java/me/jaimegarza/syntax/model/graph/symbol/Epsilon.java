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
package me.jaimegarza.syntax.model.graph.symbol;

/**
 * An &epsilon; symbol
 * @author jgarza
 *
 */
public class Epsilon extends RegexSymbol {

  @Override
  public boolean isEpsilon() {
    return true;
  }

  @Override
  public String canonical() {
    return "ε";
  }

  @Override
  public boolean matches(char c) {
    return false;
  }

  @Override
  public int code() {
    return EPSILON_CODE;
  }

  @Override
  public int sizeof() {
    return 0; // It should not be in the tables
  }

  @Override
  public int[] getCodeArray() {
    return new int[0];
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    try {
      return o instanceof Epsilon;
    } catch (NullPointerException unused) {
      return false;
    }
  }
  
  @Override
  public int hashCode() {
    final int prime = 17;
    int hash = prime + HASH_EPSILON;
    return hash;
  }
  
  @Override
  public String toString() {
    return canonical();
  }

}
