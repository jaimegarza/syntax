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

public class Rule {
  int rulenum;
  int lineNumber;
  int precedence;
  NonTerminal leftHand;
  List<RuleItem> items = new LinkedList<RuleItem>();

  public Rule(int rulenum, int lineNumber, int precedence, NonTerminal leftHand) {
    super();
    this.rulenum = rulenum;
    this.lineNumber = lineNumber;
    this.precedence = precedence;
    this.leftHand = leftHand;
  }

  public int getRulenum() {
    return rulenum;
  }

  public void setRulenum(int rulenum) {
    this.rulenum = rulenum;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public void setLineNumber(int lineNumber) {
    this.lineNumber = lineNumber;
  }

  public int getPrecedence() {
    return precedence;
  }

  public void setPrecedence(int precedence) {
    this.precedence = precedence;
  }

  public NonTerminal getLeftHand() {
    return leftHand;
  }

  public void setLeftHand(NonTerminal leftHand) {
    this.leftHand = leftHand;
  }

  public List<RuleItem> getItems() {
    return items;
  }

  public RuleItem getItem(int i) {
    RuleItem item = null;
    if (items != null && i < items.size()) {
      item = items.get(i);
    }
    return item;
  }

  public int getLeftHandId() {
    return leftHand.getId();
  }

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

  @Override
  public String toString() {
    return leftHand.toString() + " -> " + items + "\n";
  }

}
