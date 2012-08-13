package me.jaimegarza.syntax.algorithm;

import java.util.List;
import java.util.Set;

import me.jaimegarza.syntax.cli.Environment;
import me.jaimegarza.syntax.definition.Dot;
import me.jaimegarza.syntax.definition.LookAhead;
import me.jaimegarza.syntax.definition.NonTerminal;
import me.jaimegarza.syntax.definition.Rule;
import me.jaimegarza.syntax.definition.RuleItem;
import me.jaimegarza.syntax.definition.State;
import me.jaimegarza.syntax.definition.Symbol;
import me.jaimegarza.syntax.definition.Terminal;

public class LalrAlgorithmicSupport extends BaseAlgorithmicSupport {

  /**
   * Construct the supporting utility algorithm class for LALR
   * 
   * @param environment is the calling environment
   */
  public LalrAlgorithmicSupport(Environment environment) {
    super(environment);
  }

  /*
   * (non-Javadoc)
   * @see me.jaimegarza.syntax.algorithm.AlgorithmicSupport#mergeLookAheads(me.jaimegarza.syntax.definition.State[], int, java.util.List)
   */
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
  
  /*
   * (non-Javadoc)
   * @see me.jaimegarza.syntax.algorithm.AlgorithmicSupport#computeLookAhead(me.jaimegarza.syntax.definition.Rule, me.jaimegarza.syntax.definition.RuleItem)
   */
  @Override
  public LookAhead computeLookAhead(Rule rule, RuleItem item) {
    LookAhead l = new LookAhead();
    if (item == null) {
      return l;
    }

    int index = rule.getItems().indexOf(item);
    if (index == -1) {
      return l;
    }

    l.setCarry(true);

    if (index >= rule.getItems().size() - 1) {
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
  

  /*
   * (non-Javadoc)
   * @see me.jaimegarza.syntax.algorithm.AlgorithmicSupport#mergeLookaheads(me.jaimegarza.syntax.definition.Dot, me.jaimegarza.syntax.definition.Dot)
   */
  @Override
  public void mergeLookaheads(Dot marker, Dot auxiliary) {
    LookAhead l = computeLookAhead(marker.getRule(), marker.getItem());
    auxiliary.getLookahead().addAll(l.getSymbolIds());
    if (l.isCarry()) {
      marker.getLookahead().addAll(auxiliary.getLookahead());
    }
  }
  
  /*
   * (non-Javadoc)
   * @see me.jaimegarza.syntax.algorithm.AlgorithmicSupport#addAllLookaheads(me.jaimegarza.syntax.definition.Dot, me.jaimegarza.syntax.definition.Dot)
   */
  @Override
  public void addAllLookaheads(Dot marker, Dot auxiliary) {
    marker.addAllLookaheads(auxiliary.getLookahead());
  }

  /*
   * (non-Javadoc)
   * @see me.jaimegarza.syntax.algorithm.AlgorithmicSupport#printLookahead(me.jaimegarza.syntax.definition.Dot)
   */
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

  /*
   * (non-Javadoc)
   * @see me.jaimegarza.syntax.algorithm.AlgorithmicSupport#dotContains(int)
   */
  @Override
  public boolean dotContains(Dot dot, int terminalId) {
    return  dot.getLookahead().contains(terminalId);
  }

  /*
   * (non-Javadoc)
   * @see me.jaimegarza.syntax.algorithm.AlgorithmicSupport#isMultiPass()
   */
  @Override
  public boolean isMultiPass() {
    return true;
  }

  /*
   * (non-Javadoc)
   * @see me.jaimegarza.syntax.algorithm.AlgorithmicSupport#initializeDot()
   */
  @Override
  public void initializeDot(Dot dot) {
    dot.addLookahead(0); // empty set
  }

  /*
   * (non-Javadoc)
   * @see me.jaimegarza.syntax.algorithm.AlgorithmicSupport#hasFollows()
   */
  @Override
  public boolean hasFollows() {
    return false;
  }

}
