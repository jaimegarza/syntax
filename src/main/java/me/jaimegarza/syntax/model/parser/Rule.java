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

import java.util.LinkedList;
import java.util.List;

/**
 * <i>~pojo class</i><br><br>
 * 
 * During the parsing process, the grammar is represented as a list of rules.
 * Each rule is composed of a list of zero or many {@link RuleItem}s. This representation
 * is used throughout the multiple processes to generate the structure of the syntax
 * including computing first, follow, lookaheads, states, actions, grammar tables, and 
 * finally parsing tables.
 * 
 *  This class defines a whole rule.
 *  
 * @author jaimegarza@gmail.com
 *
 */
public class Rule {
  /**
   * The sequencial number of a gramatical rule
   */
  int rulenum;
  /**
   * The line in the source file where this rule appears
   */
  int lineNumber;
  /**
   * The computed precedence of the rule, based on its non-terminals and
   * %prec declaration.
   * 
   * Rules get their precedence from the last non-terminal in them.  To further
   * clarify, or to provide a precedence for the rule when no non-terminals are
   * presedence, <b>%prec</b> is used in the grammar.<p>
   * 
   * Precedence and associativity of a symbol are used to resolve LR grammar conflicts
   * as follows:<pre>
   *   if shift-reduce conflict
   *      left associativity implies reduce
   *      right associativity implies shift
   *      non assoc implies error</pre>
   */
  int precedence;
  /**
   * The left hand symbol is the symbol for which a rule is defined.  In<pre>
   * Factor -> identifier</pre>
   * The left hand symbol is the non-terminal called Factor.
   */
  NonTerminal leftHand;
  /**
   * Identifies a list of {@link RuleItem} elements that are part of the rule.  In<pre>
   * Expression -> Expression + Term
   * </pre>
   * The rule items are Expression, +, Term
   */
  List<RuleItem> items = new LinkedList<RuleItem>();

  /**
   * Construct a rule
   * @param rulenum is the rule id
   * @param lineNumber is the line where the rule appears in the
   *        source file 
   * @param precedence is the precedence of the rule
   * @param leftHand is the left hand symbol
   */
  public Rule(int rulenum, int lineNumber, int precedence, NonTerminal leftHand) {
    super();
    this.rulenum = rulenum;
    this.lineNumber = lineNumber;
    this.precedence = precedence;
    this.leftHand = leftHand;
  }
  
  /**
   * @param index is the index of the rule item
   * @return the {@link RuleItem} with index <i>index</i>
   */

  public RuleItem getItem(int index) {
    RuleItem item = null;
    if (items != null && index < items.size()) {
      item = items.get(index);
    }
    return item;
  }

  /**
   * Convenience method to get the id of the left hand non-terminal
   * @return
   */
  public int getLeftHandId() {
    return leftHand.getId();
  }
  
  /* Getters and setters */

  /**
   * @return the rulenum
   */
  public int getRulenum() {
    return rulenum;
  }

  /**
   * @param rulenum the rulenum to set
   */
  public void setRulenum(int rulenum) {
    this.rulenum = rulenum;
  }

  /**
   * @return the lineNumber
   */
  public int getLineNumber() {
    return lineNumber;
  }

  /**
   * @param lineNumber the lineNumber to set
   */
  public void setLineNumber(int lineNumber) {
    this.lineNumber = lineNumber;
  }

  /**
   * @return the precedence
   */
  public int getPrecedence() {
    return precedence;
  }

  /**
   * @param precedence the precedence to set
   */
  public void setPrecedence(int precedence) {
    this.precedence = precedence;
  }

  /**
   * @return the leftHand
   */
  public NonTerminal getLeftHand() {
    return leftHand;
  }

  /**
   * @param leftHand the leftHand to set
   */
  public void setLeftHand(NonTerminal leftHand) {
    this.leftHand = leftHand;
  }

  /**
   * @return the items
   */
  public List<RuleItem> getItems() {
    return items;
  }
  
  public int exactIndexOf(RuleItem item) {
    int index = 0;
    while (index < items.size()) {
      if (items.get(index) == item) {
        break;
      }
      index ++;
    }
    if (index >= items.size()) {
      index = -1;
    }
    return index;
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
      Rule r = (Rule) obj;
      return rulenum == r.rulenum &&
             lineNumber == r.lineNumber &&
               precedence == r.precedence &&
               leftHand.equals(r.leftHand) /*&&
                                           items.equals(r.items);*/;
    } catch (NullPointerException unused) {
      return false;
    } catch (ClassCastException unused) {
      return false;
    }
  }

  /**
   * Returns a graphical directed representation of the left hand symbol
   * and its items.
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return leftHand.toString() + " -> " + items + "\n";
  }

}
