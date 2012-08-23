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
package me.jaimegarza.syntax.algorithm;

import java.util.List;
import java.util.Set;

import me.jaimegarza.syntax.definition.Dot;
import me.jaimegarza.syntax.definition.LookAhead;
import me.jaimegarza.syntax.definition.NonTerminal;
import me.jaimegarza.syntax.definition.Rule;
import me.jaimegarza.syntax.definition.RuleItem;
import me.jaimegarza.syntax.definition.State;
import me.jaimegarza.syntax.definition.Symbol;
import me.jaimegarza.syntax.definition.Terminal;
import me.jaimegarza.syntax.env.Environment;

public class LalrAlgorithmicSupport extends BaseAlgorithmicSupport {

  /**
   * Construct the supporting utility algorithm class for LALR
   * 
   * @param environment is the calling environment
   */
  public LalrAlgorithmicSupport(Environment environment) {
    super(environment);
  }

  @Override
  public boolean addLookaheadsToState(State I[], int state, List<Dot> dots) {
    Dot i, k;
    
    i = I[state].getDot(0);
    k = dots.get(0);

    // are all there?
    if (i.getLookahead() != null && k.getLookahead() != null && i.getLookahead().containsAll(k.getLookahead())) {
      return false;
    }

    while (i != null && k != null && Dot.equals(i,  k)) {
      i.addAllLookaheads(k.getLookahead());
      i = i.next();
      k = k.next();
    }
    return true;
  }
  
  @Override
  public LookAhead computeLookAhead(Rule rule, RuleItem item) {
    LookAhead l = new LookAhead();
    l.setCarry(true);
    
    if (item == null) {
      return l;
    }

    int index = rule.getItems().indexOf(item);
    if (index == -1 || index >= rule.getItems().size() - 1) {
      return l;
    }

    index++;
    while (index < rule.getItems().size()) {
      item = rule.getItem(index);
      if (item.getSymbol() instanceof Terminal) {
        l.getSymbolIds().add(item.getSymbolId());
        l.setCarry(false);
        break;
      } else {
        l.getSymbolIds().addAll(((NonTerminal) item.getSymbol()).getFirst());
        if (!runtimeData.symbolCanBeEmpty(item.getSymbolId())) {
          l.setCarry(false);
          break;
        }
      }
      index++;
    }
    return l;
  }  
  
  @Override
  public void mergeLookaheads(Dot marker, Dot auxiliary) {
    LookAhead l = computeLookAhead(marker.getRule(), marker.getItem());
    auxiliary.getLookahead().addAll(l.getSymbolIds());
    if (l.isCarry()) {
      auxiliary.getLookahead().addAll(marker.getLookahead());
    }
  }
  
  @Override
  public void addAllLookaheads(Dot marker, Dot auxiliary) {
    marker.addAllLookaheads(auxiliary.getLookahead());
  }

  @Override
  public void printLookahead(Dot dot) {
    Set<Integer> lookAhead = dot.getLookahead();
    if (lookAhead == null) {
      return;
    }

    environment.report.print("     { ");
    for (Symbol tkn : runtimeData.getTerminals()) {
      if (lookAhead.contains(tkn.getId())) {
        environment.report.print(tkn.getName() + " ");
      }
    }
    environment.report.print("}");
  }

  @Override
  public boolean dotContains(Dot dot, int terminalId) {
    return  dot.getLookahead().contains(terminalId);
  }

  @Override
  public boolean isMultiPass() {
    return true;
  }

  @Override
  public void initializeDot(Dot dot) {
    dot.addLookahead(0); // empty set
  }

  @Override
  public boolean hasFollows() {
    return false;
  }

}
