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

/**
 * A range of characters
 * @author jgarza
 *
 */
public class CharacterRange {
  private char from;
  private char to;
  
  /**
   * Default constructor
   * @param from the starting character
   * @param to the ending character
   */
  public CharacterRange(char from, char to) {
    this.from = from;
    this.to = to;
  }
  
  /**
   * Constructor for a single char
   * @param c a character
   */
  public CharacterRange(char c) {
    this.from = this.to = c;
  }

  /**
   * @return the from
   */
  public char getFrom() {
    return from;
  }

  /**
   * @return the to
   */
  public char getTo() {
    return to;
  }

  @Override
  public String toString() {
    if (from == to) {
      return "" + from;
    }
    else {
      return "" + from + "-" + to;
    }
  }
  
  /**
   * Does a character match this range?
   * @param c the character
   * @return true if it matches
   */
  public boolean matches(char c) {
    return c >= from && c <= to;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    try {
      CharacterRange cr = (CharacterRange) obj;
      return from == cr.from && to == cr.to;
    } catch (NullPointerException unused) {
      return false;
    } catch (ClassCastException unused) {
      return false;
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int hash = prime + from;
    hash = prime * hash + to;
    return hash;
  }
  

}
