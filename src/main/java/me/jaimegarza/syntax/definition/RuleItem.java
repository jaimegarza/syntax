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

/**
 * <i>~pojo class</i><br><br>
 * 
 * During the parsing process, the grammar is represented as a list of {@link Rule}s.
 * Each rule is composed of a list of zero or many RuleItems. This representation
 * is used throughout the multiple processes to generate the structure of the syntax
 * including computing first, follow, lookaheads, states, actions, grammar tables, and 
 * finally parsing tables.
 * 
 *  This class defines a whole rule.
 *  
 * @author jaimegarza@gmail.com
 *
 */public class RuleItem {
  /**
   * The symbol associated to the item.
   */
  Symbol symbol;
  /**
   * The parent rule of the rule item.
   */
  Rule rule;

  /**
   * Construct a rule item with the given symbol
   * @param symbol
   */
  public RuleItem(Symbol symbol) {
    super();
    this.symbol = symbol;
  }

  /**
   * Convenience method to get the id of the symbol in the rule item
   * @return the id of the symbol
   */
  public int getSymbolId() {
    return symbol.getId();
  }

  /**
   * Move to the next item in the rule
   * @return the next item, or null if at the end
   */
  public RuleItem next() {
    for (int i = 0; i < rule.getItems().size(); i++) {
      RuleItem r = rule.getItem(i);
      if (this.equals(r)) {
        return rule.getItem(i + 1);
      }
    }
    return null;
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
   * @return the rule
   */
  public Rule getRule() {
    return rule;
  }

  /**
   * @param rule the rule to set
   */
  public void setRule(Rule rule) {
    this.rule = rule;
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
      return getSymbolId() == ((RuleItem) obj).getSymbolId();
    } catch (NullPointerException unused) {
      return false;
    } catch (ClassCastException unused) {
      return false;
    }
  }

  /**
   * Returns the symbol string value.
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return symbol.toString();
  }

}
