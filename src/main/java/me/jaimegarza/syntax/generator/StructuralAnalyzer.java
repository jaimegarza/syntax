package me.jaimegarza.syntax.generator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.jaimegarza.syntax.AnalysisException;
import me.jaimegarza.syntax.cli.Algorithm;
import me.jaimegarza.syntax.cli.Environment;
import me.jaimegarza.syntax.definition.NonTerminal;
import me.jaimegarza.syntax.definition.Rule;
import me.jaimegarza.syntax.definition.RuleItem;
import me.jaimegarza.syntax.definition.Terminal;

public class StructuralAnalyzer extends AbstractPhase {
  private Set<Integer> searchItems = new HashSet<Integer>();

  public StructuralAnalyzer(Environment environment, RuntimeData runtimeData) {
    super();
    this.environment = environment;
    this.runtimeData = runtimeData;
  }

  /**
   * The first is the set of terminal symbols that a given non-terminal can
   * start with.
   * <p>
   * Look at the left side of all rules and, if the rule left hand matches the
   * desired symbol, find the non-terminals that the symbol can start with. This
   * is recursive. In addition, empty non-terminals appearing at the beginning
   * of the rule require the review of the second one, etc.
   */
  public Set<Integer> getFirst(int ntId) {
    Set<Integer> first = new HashSet<Integer>();

    for (Rule rule : runtimeData.getRules()) {
      getFirstForAllRules(ntId, first, rule);
    }
    return first;
  }

  Set<Integer> getFollow(int ntId) {
    Set<Integer> follow = new HashSet<Integer>();

    // the follow of the root is always the empty terminal
    if (ntId == runtimeData.getRoot().getId()) {
      follow.add(0);
      return follow;
    }

    for (Rule rule : runtimeData.getRules()) {
      getFollowFromARule(ntId, follow, rule);
    }
    return follow;
  }

  private void getFollowFromARule(int ntId, Set<Integer> follow, Rule rule) {
    List<RuleItem> items = rule.getItems();
    int itemCount = items.size();
    for (int itemIndex = 0; itemIndex < itemCount; itemIndex++) {
      RuleItem item = items.get(itemIndex);
      if (item.getSymbolId() == ntId) {
        getFollowFromAnItem(ntId, follow, rule, items, itemCount, itemIndex);
      }
    }
  }

  private void getFollowFromAnItem(int ntId, Set<Integer> follow, Rule rule, List<RuleItem> items, int itemCount,
      int itemIndex) {
    for (int j = itemIndex; j < itemCount; j++) {
      if (j == itemCount - 1) { // is the index pointing to the last item?
        if (rule.getLeftHandId() != ntId) {
          if (rule.getLeftHand().getFollow() == null) {
            if (!searchItems.contains(ntId)) {
              searchItems.add(ntId);
              Set<Integer> faux = getFollow(rule.getLeftHandId());
              searchItems.remove(ntId);
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

  private boolean getFollowForItem(int nextItemIndex, Set<Integer> follow, List<RuleItem> items) {
    if (items.get(nextItemIndex).getSymbol() instanceof NonTerminal) {
      NonTerminal nonTerminal = (NonTerminal) items.get(nextItemIndex).getSymbol();
      follow.addAll(nonTerminal.getFirst());
      if (!isEmpty(nonTerminal.getId())) {
        return false;
      }
    } else {
      follow.add(items.get(nextItemIndex).getSymbol().getId());
      return false;
    }
    return true;
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
        if (!isEmpty(item.getSymbol().getId())) {
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

  private void print() {
    for (NonTerminal itm : runtimeData.getNonTerminals()) {
      environment.report.printf("\n");
      environment.report.printf("First of %s\n", itm.getName());
      for (Terminal tkn : runtimeData.getTerminals()) {
        if (itm.getFirst().contains(tkn.getId())) {
          environment.report.printf("%s%3d%s%s\n", "   ", tkn.getId(), ". ", tkn.getName());
        }
      }
    }

    if (environment.getAlgorithm() == Algorithm.LALR) {
      return;
    }

    for (NonTerminal itm : runtimeData.getNonTerminals()) {
      environment.report.printf("\n");
      environment.report.printf("Follow of %s\n", itm.getName());
      for (Terminal tkn : runtimeData.getTerminals()) {
        if (itm.getFollow().contains(tkn.getId())) {
          environment.report.printf("%s%3d%s%s\n", "   ", tkn.getId(), ". ", tkn.getName());
        }
      }
    }
  }

  public void execute() throws AnalysisException {
    if (environment.isVerbose()) {
      System.out.println("First & Follow");
    }
    for (NonTerminal symbol : runtimeData.getNonTerminals()) {
      searchItems.clear();
      if (environment.isVerbose()) {
        System.out.printf("First of %d. %-40.40s\n", symbol.getId(), symbol.getName());
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
    if (environment.getAlgorithm() == Algorithm.LALR) {
      print();
      return;
    }

    for (NonTerminal symbol : runtimeData.getNonTerminals()) {
      searchItems.clear();
      if (environment.isVerbose()) {
        System.out.printf("Follow of %d. %-40.40s\n", symbol.getId(), symbol.getName());
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
