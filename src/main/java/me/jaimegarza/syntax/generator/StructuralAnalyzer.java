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
package me.jaimegarza.syntax.generator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.jaimegarza.syntax.AnalysisException;
import me.jaimegarza.syntax.algorithm.Algorithm;
import me.jaimegarza.syntax.cli.Environment;
import me.jaimegarza.syntax.definition.NonTerminal;
import me.jaimegarza.syntax.definition.Rule;
import me.jaimegarza.syntax.definition.RuleItem;
import me.jaimegarza.syntax.definition.Terminal;

/**
 * Phases:
 * 
 * <ol>
 *   <li>Code Parser
 *   <li><b>Structural Analysis</b> (This Phase)
 *   <li>Table Generation
 *   <li>Writing Code
 * </ol>
 * This phase computes the first & follow sets for symbols
 * @author jaimegarza@gmail.com
 *
 */
public class StructuralAnalyzer extends AbstractPhase {
  private Set<Integer> searchItems = new HashSet<Integer>();

  /**
   * Construct an analizer given an environment
   * 
   * @param environment the {@link Environment}, shared between phases.
   */
  public StructuralAnalyzer(Environment environment) {
    super(environment);
  }

  /**
   * The first is the set of terminal symbols that a given non-terminal can
   * start with.
   * <p>
   * Look at the left side of all rules and, if the rule left hand matches the
   * desired symbol, find the non-terminals that the symbol can start with. This
   * is recursive. In addition, empty non-terminals appearing at the beginning
   * of the rule require the review of the second one, etc.<p>
   * given<pre><code>
   * r<sub>i</sub>: &alpha; &rarr; &beta; 
   * </code></pre>
   * First(&alpha;) = &cup; First(&beta;)<br>
   * &nbsp;&nbsp;&nbsp;&forall; &alpha; &isin; N &and; r<sub>i</sub> &isin; (<b>R</b> &isin; <b>G</b>)<p>
   * In addition, for &beta; = &lambda;&delta;<p>
   * First (&beta;):
   * <ol>
   *   <li> if &lambda; &isin; <b>N</b> then First (&beta;) = First(&lambda;)<p>
   *        if &exist; r<sub>j</sub>: &lambda; &rarr; &empty; then 
   *          First(&beta;) = First(&lambda;) &cup; First(&delta;)
   *   <li> if &lambda; &isin; T then First(&lambda;) = { &lambda; }
   * </ol>
   * @param nonTerminalId is the non terminal to compute
   * @return the set of first for a given symbol, traversing all rules left hand symbol.
   */
  public Set<Integer> getFirst(int nonTerminalId) {
    Set<Integer> first = new HashSet<Integer>();

    for (Rule rule : runtimeData.getRules()) {
      getFirstForAllRules(nonTerminalId, first, rule);
    }
    return first;
  }

  /**
   * The follow is the set of terminal symbols that can appear after a given non-terminal.  
   * This is useful to simplify grammar production (via SLR) which generalizes the symbols
   * that can appear to the right of a symbol.
   * 
   * given<pre><code>
   * r<sub>i</sub>: &alpha; &rarr; &mu; &beta; &gamma;
   * </code></pre>
   * Follow(&beta;) = First(&gamma;)<br>&nbsp;&nbsp;&nbsp; &forall; &beta; &isin; <b>N</b> &and; r<sub>i</sub> &isin; (<b>R</b> &isin; <b>G</b>)<p>
   * In addition, <i>if</i> &exist; r<sub>j</sub>: &gamma; &rarr; &empty;; &gamma; &isin; <b>N</b> &and; &forall; r<sub>j</sub> &isin; (<b>R</b> &isin; <b>G</b>) then<p>
   * Follow(&beta;) = First(&gamma;) &cup; Follow(&alpha;) <p>
   * Please note that to compute follows we analyze each non terminal in the <b>right
   * side</b> context of all rules.
   * 
   * @param nonTerminalId is the non terminal to compute (id of &beta;)
   * @return
   */
  Set<Integer> getFollow(int nonTerminalId) {
    Set<Integer> follow = new HashSet<Integer>();

    // the follow of the root is always the empty terminal
    if (nonTerminalId == runtimeData.getRoot().getId()) {
      follow.add(0);
      return follow;
    }

    for (Rule rule : runtimeData.getRules()) {
      getFollowFromARule(nonTerminalId, follow, rule);
    }
    return follow;
  }

  /**
   * Compute the follow of a rule<p>
   * see {@link #getFollow(int)} for additional description on what a follow is<p>
   * This routine finds all &beta; elements from a rule r<sub>i</sub>: &alpha; &rarr; &mu; &beta; &gamma;
   * @param nonTerminalId is the id of &beta;
   * @param follow is where to keep adding the follow
   * @param rule is the {@link Rule} to analyze
   */
  private void getFollowFromARule(int nonTerminalId, Set<Integer> follow, Rule rule) {
    List<RuleItem> items = rule.getItems();
    int itemCount = items.size();
    for (int itemIndex = 0; itemIndex < itemCount; itemIndex++) {
      RuleItem item = items.get(itemIndex);
      if (item.getSymbolId() == nonTerminalId) {
        getFollowInRuleContext(nonTerminalId, follow, rule, items, itemCount, itemIndex);
      }
    }
  }

  /**
   * Get the follows for a given item.
   * see {@link #getFollow(int)} for additional description on what a follow is<p>
   * 
   * 
   * @param nonTerminalId is the id of &beta;
   * @param follow is the set where to add all non terminals that constitute the follow
   * @param rule the {@link Rule} where &beta; can appear
   * @param items is the contex of {@link RuleItem}s that can be around &beta;
   * @param itemCount number of items in the rule r<subi</sub>
   * @param itemIndex the index in the items where &beta; appears
   */
  private void getFollowInRuleContext(int nonTerminalId, Set<Integer> follow, Rule rule, List<RuleItem> items, int itemCount,
      int itemIndex) {
    for (int j = itemIndex; j < itemCount; j++) {
      if (j == itemCount - 1) { // is the index pointing to the last item?
        if (rule.getLeftHandId() != nonTerminalId) {
          if (rule.getLeftHand().getFollow() == null) {
            if (!searchItems.contains(nonTerminalId)) {
              searchItems.add(nonTerminalId);
              Set<Integer> faux = getFollow(rule.getLeftHandId());
              searchItems.remove(nonTerminalId);
              follow.addAll(faux);
            }
          } else {
            follow.addAll(rule.getLeftHand().getFollow());
          }
        }
      } else {
        if (!getFollowForItem(j + 1, follow, items)) {
          break;
        }
      }
    }
  }

  /**
   * Get the follow for item<p>
   * see {@link #getFollow(int)} for additional description on what a follow is<p>
   * 
   * @param nextItemIndex is the index in the rules items for &gamma;
   * @param follow is the set where to add all non terminals that constitute the follow
   * @param items is the contex of {@link RuleItem}s that can be around &beta;
   * @return true if symbol &gamma
   */
  private boolean getFollowForItem(int nextItemIndex, Set<Integer> follow, List<RuleItem> items) {
    if (items.get(nextItemIndex).getSymbol() instanceof NonTerminal) {
      NonTerminal nonTerminal = (NonTerminal) items.get(nextItemIndex).getSymbol();
      follow.addAll(nonTerminal.getFirst());
      if (runtimeData.symbolCanBeEmpty(nonTerminal.getId()) == false) {
        return false;
      } else {
        return true;
      }
    } else {
      follow.add(items.get(nextItemIndex).getSymbol().getId());
      return false;
    }
  }

  /**
   * Look at the rule items and get the first of the element. The first is the
   * set of terminal symbols that a given non-terminal can start with.
   * 
   * <ul>
   * <li>For non-empty non-terminals, it will get the non-terminal data</li>
   * <li>For empty non-terminals it will get the non-terminal data and move to
   * the next</li>
   * <li>For terminals, just add the symbol id to the set and finish</li>
   * </ul>
   * 
   * @param ntId
   *          the id of the non-terminal
   * @param first
   *          the set where to collect the terminals
   * @param rule
   *          the rule to be investigated
   */
  private void getFirstForAllRules(int ntId, Set<Integer> first, Rule rule) {
    if (rule.getLeftHand().getId() != ntId) {
      return; // rule left hand is not the desired id
    }

    for (RuleItem item : rule.getItems()) {
      if (item.getSymbol() instanceof NonTerminal) {
        getFirstForNonTerminal(ntId, first, item);
        if (runtimeData.symbolCanBeEmpty(item.getSymbol().getId()) == false) {
          break; // non propagating first. Stop.
        }
      } else {
        first.add(item.getSymbolId());
      }
    }
  }

  /**
   * The first is the set of terminal symbols that a given non-terminal can
   * start with.
   * 
   * @param ntId
   *          the id of the non-terminal
   * @param first
   *          the set where to collect the terminals
   * @param item
   *          the item in the rule to be investigated
   */
  private void getFirstForNonTerminal(int ntId, Set<Integer> first, RuleItem item) {
    if (item.getSymbol().getId() == ntId) {
      return; // if left hand is the same as the desired number, skip
    }

    if (!(item.getSymbol() instanceof NonTerminal)) {
      return;
    }

    NonTerminal nt = (NonTerminal) item.getSymbol();
    if (nt.getFirst() == null) { // not yet computed. Go get it recursivelly
      // This is a controlled recursion. I have a set of non-terminals being
      // analized recursivelly.
      // This is done such that I do not do infinite recursion
      if (!searchItems.contains(ntId)) {
        // control non infinite recursion
        searchItems.add(ntId);
        Set<Integer> faux = getFirst(item.getSymbol().getId());
        searchItems.remove(ntId);
        first.addAll(faux);
      }
    } else {
      first.addAll(nt.getFirst());
    }
  }

  /**
   * Print first and follow to the report.
   */
  private void print() {
    for (NonTerminal itm : runtimeData.getNonTerminals()) {
      environment.report.println();
      environment.report.println("First of " + itm.getName());
      for (Terminal tkn : runtimeData.getTerminals()) {
        if (itm.getFirst().contains(tkn.getId())) {
          environment.report.printf("%s%3d%s%s", "   ", tkn.getId(), ". ", tkn.getName());
          environment.report.println();
        }
      }
    }

    if (environment.getAlgorithmType() == Algorithm.LALR) {
      return;
    }

    for (NonTerminal itm : runtimeData.getNonTerminals()) {
      environment.report.println();
      environment.report.println("Follow of " + itm.getName());
      for (Terminal tkn : runtimeData.getTerminals()) {
        if (itm.getFollow().contains(tkn.getId())) {
          environment.report.printf("%s%3d%s%s", "   ", tkn.getId(), ". ", tkn.getName());
          environment.report.println();
        }
      }
    }
  }

  /*
   * Execute this phase
   */
  public void execute() throws AnalysisException {
    if (environment.isVerbose()) {
      System.out.println("First & Follow");
    }
    for (NonTerminal symbol : runtimeData.getNonTerminals()) {
      searchItems.clear();
      if (environment.isVerbose()) {
        System.out.printf("First of %d. %-40.40s", symbol.getId(), symbol.getName());
        System.out.println();
      }
      Set<Integer> first = getFirst(symbol.getId());
      if (first == null) {
        throw new AnalysisException("Internal Error computing first set.");
      }
      symbol.setFirst(first);
    }

    // LALR does not compute follows. Only SLR. LALR does it by going case by
    // case in the table
    // generation to compute contextual follows.
    if (environment.getAlgorithmType() == Algorithm.LALR) {
      print();
      return;
    }

    for (NonTerminal symbol : runtimeData.getNonTerminals()) {
      searchItems.clear();
      if (environment.isVerbose()) {
        System.out.printf("Follow of %d. %-40.40s", symbol.getId(), symbol.getName());
        System.out.println();
      }
      Set<Integer> follow = getFollow(symbol.getId());
      if (follow == null) {
        throw new AnalysisException("Internal Error computing follow set.");
      }
      symbol.setFollow(follow);
    }
    if (environment.isVerbose()) {
      System.out.println("First & Follow OK");
    }
    print();
  }
}
