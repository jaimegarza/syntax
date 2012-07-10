package me.jaimegarza.syntax.algorithm;

import java.util.List;

import me.jaimegarza.syntax.cli.Environment;
import me.jaimegarza.syntax.definition.Dot;
import me.jaimegarza.syntax.definition.LookAhead;
import me.jaimegarza.syntax.definition.Rule;
import me.jaimegarza.syntax.definition.RuleItem;
import me.jaimegarza.syntax.definition.State;

public class SlrAlgorithmicSupport extends BaseAlgorithmicSupport{

  /**
   * Construct the supporting utility algorithm class for LALR
   * 
   * @param environment is the calling environment
   */
  public SlrAlgorithmicSupport(Environment environment) {
    super(environment);
  }

  /*
   * (non-Javadoc)
   * @see me.jaimegarza.syntax.algorithm.AlgorithmicSupport#mergeLookAheads(me.jaimegarza.syntax.definition.State[], int, java.util.List)
   */
  @Override
  public boolean addLookaheadsToState(State[] I, int state, List<Dot> dots) {
    return false;
  }

  /*
   * (non-Javadoc)
   * @see me.jaimegarza.syntax.algorithm.AlgorithmicSupport#mergeLookaheads(me.jaimegarza.syntax.definition.Dot, me.jaimegarza.syntax.definition.Dot)
   */
  @Override
  public void mergeLookaheads(Dot marker, Dot auxiliary) {
    
  }

  /*
   * (non-Javadoc)
   * @see me.jaimegarza.syntax.algorithm.AlgorithmicSupport#computeLookAhead(me.jaimegarza.syntax.definition.Rule, me.jaimegarza.syntax.definition.RuleItem)
   */
  @Override
  public LookAhead computeLookAhead(Rule rule, RuleItem item) {
    return null;
  }

  /*
   * (non-Javadoc)
   * @see me.jaimegarza.syntax.algorithm.AlgorithmicSupport#printLookahead(me.jaimegarza.syntax.definition.Dot)
   */
  @Override
  public void printLookahead(Dot dot) {
  }

  /*
   * (non-Javadoc)
   * @see me.jaimegarza.syntax.algorithm.AlgorithmicSupport#dotContains(me.jaimegarza.syntax.definition.Dot, int)
   */
  @Override
  public boolean dotContains(Dot dot, int terminalId) {
    return dot.getRule().getLeftHand().getFollow().contains(terminalId);
  }
  
  /*
   * (non-Javadoc)
   * @see me.jaimegarza.syntax.algorithm.AlgorithmicSupport#isMultiPass()
   */
  @Override
  public boolean isMultiPass() {
    return false;
  }

  /*
   * (non-Javadoc)
   * @see me.jaimegarza.syntax.algorithm.AlgorithmicSupport#initializeDot(me.jaimegarza.syntax.definition.Dot)
   */
  @Override
  public void initializeDot(Dot dot) {
  }

  /*
   * (non-Javadoc)
   * @see me.jaimegarza.syntax.algorithm.AlgorithmicSupport#addAllLookaheads(me.jaimegarza.syntax.definition.Dot, me.jaimegarza.syntax.definition.Dot)
   */
  @Override
  public void addAllLookaheads(Dot marker, Dot auxiliary) {
    
  }

}
