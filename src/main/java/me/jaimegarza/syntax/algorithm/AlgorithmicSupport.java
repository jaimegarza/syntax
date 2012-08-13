package me.jaimegarza.syntax.algorithm;

import java.util.List;

import me.jaimegarza.syntax.definition.Dot;
import me.jaimegarza.syntax.definition.LookAhead;
import me.jaimegarza.syntax.definition.Rule;
import me.jaimegarza.syntax.definition.RuleItem;
import me.jaimegarza.syntax.definition.State;

public interface AlgorithmicSupport {
  /**
   * For algorithms that support look ahead per state, this method will
   * add all the lookaheads of the passed dots into the state's dots.
   * 
   * @param I is the array of states
   * @param state is the state number
   * @param dots is the list of marker {@link Dot}s
   * @return
   */
  boolean addLookaheadsToState(State I[], int state, List<Dot> dots);

  /**
   * Merge one marker dot's lookaheads into a destination.
   * 
   * @param marker is the source of the lookaheads
   * @param auxiliary is the second auxiliary dot
   */
  void mergeLookaheads(Dot marker, Dot auxiliary);

  /**
   * Simply put lookaheads of auxiliary into marker
   * @param marker is the receiving marker
   * @param auxiliary is the originating set of lookaheads
   */
  void addAllLookaheads(Dot marker, Dot auxiliary);

  /**
   * Compute the lookahead set of a rule on a specific item
   * 
   * @param rule is the rule where the lookahead begins
   * @param item is the dot position to start lookahead computations.
   * @return the new Lookahead, or null
   */
  LookAhead computeLookAhead(Rule rule, RuleItem item);
  
  /**
   * Print the lookaheads in a dot
   * @param dot is the dot to print
   */
  void printLookahead(Dot dot);
  
  /**
   * Checks a dot to see if it contains the symbol
   * 
   * @param dot the dot to check
   * @param terminalId is the symbol
   * @return true or false
   */
  boolean dotContains(Dot dot, int terminalId);
  
  /**
   * @return true if the algorithm needs multiple passes
   */
  boolean isMultiPass();

  /**
   * This dot is brand new, and used for a state.  Initialize it
   * @param dot is the dot to initialize
   */
  void initializeDot(Dot dot);

  /**
   * Does this algorithm support follow sets?
   * @return
   */
  boolean hasFollows();


}
