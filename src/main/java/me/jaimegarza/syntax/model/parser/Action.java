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
package me.jaimegarza.syntax.model.parser;

/**
 * <i>~pojo class</i><br><br>
 * 
 * For packed parsers, the parsing table is divided in a list of
 * {@link Action} and {@link GoTo}.  The parsing table just points at the 
 * entry point of the actions.
 * 
 * This class describes a transition from one state to another by 
 * specifying the symbol and the destination.  It is a mapping for a set of states
 * over a symbol.
 *
 * @author jaimegarza@gmail.com
 *
 */
public class Action {
  /** The {@link Symbol} with wich the transition is to happen */
  Symbol symbol;
  /** The new state to which to move to */
  int stateNumber;

  /**
   * Construct an action
   * @param symbol the symbol that will cause the action
   * @param stateNumber the destination state
   */
  public Action(Symbol symbol, int stateNumber) {
    super();
    this.symbol = symbol;
    this.stateNumber = stateNumber;
  }
  
  /* Getters and setters */

  /**
   * @return the symbol
   */
  public Symbol getSymbol() {
    return symbol;
  }

  /**
   * @param symbol the symbol to set
   */
  public void setSymbol(Symbol symbol) {
    this.symbol = symbol;
  }

  /**
   * @return the stateNumber
   */
  public int getStateNumber() {
    return stateNumber;
  }

  /**
   * @param stateNumber the stateNumber to set
   */
  public void setStateNumber(int stateNumber) {
    this.stateNumber = stateNumber;
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
      Action a = (Action) obj;
      return symbol.equals(a.symbol) && stateNumber == a.stateNumber;
    } catch (NullPointerException unused) {
      return false;
    } catch (ClassCastException unused) {
      return false;
    }
  }

  /**
   * Returns a phrase with the symbol and its destination
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "with " + symbol + " goto " + stateNumber;
  }
}
