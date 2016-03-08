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
package me.jaimegarza.syntax.model.parser;

import java.util.HashSet;
import java.util.Set;

/**
 * <i>~pojo class</i><br><br>
 *
 * When the parser table is generated with a LALR parser, a set of
 * lookaheads is computed.  This is similar to a follow set except
 * that the lookahead is more fine grained than the coarse follow set.
 * 
 * Lookaheads are associated to dots, not non-terminals.
 * 
 * A lookahead is computed on a per state basis.  A state is observed 
 * and the set of follow-like terminals is obtained, only on the c
 * context of that state.
 * 
 * @author jaimegarza@gmail.com
 *
 */
public class LookAhead {
  /**
   * The set of symbol ids that make a lookahead
   */
  Set<Integer> symbolIds = new HashSet<Integer>();
  /**
   * A carry is obtained when a rule is at the end, and the follow
   * may require additional computations.
   */
  boolean carry = true;

  /**
   * @return the symbolIds
   */
  public Set<Integer> getSymbolIds() {
    return symbolIds;
  }

  /**
   * @param symbolIds the symbolIds to set
   */
  public void setSymbolIds(Set<Integer> symbolIds) {
    this.symbolIds = symbolIds;
  }

  /**
   * @return the carry
   */
  public boolean isCarry() {
    return carry;
  }

  /**
   * @param carry the carry to set
   */
  public void setCarry(boolean carry) {
    this.carry = carry;
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
      LookAhead la = (LookAhead) obj;
      return carry == la.carry && symbolIds.equals(la.symbolIds);
    } catch (NullPointerException unused) {
      return false;
    } catch (ClassCastException unused) {
      return false;
    }
  }

}
