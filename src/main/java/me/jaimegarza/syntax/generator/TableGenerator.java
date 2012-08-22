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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import me.jaimegarza.syntax.cli.Environment;
import me.jaimegarza.syntax.definition.Action;
import me.jaimegarza.syntax.definition.Associativity;
import me.jaimegarza.syntax.definition.Dot;
import me.jaimegarza.syntax.definition.GoTo;
import me.jaimegarza.syntax.definition.NonTerminal;
import me.jaimegarza.syntax.definition.Rule;
import me.jaimegarza.syntax.definition.RuleItem;
import me.jaimegarza.syntax.definition.State;
import me.jaimegarza.syntax.definition.Symbol;

/**
 * Phases:
 * 
 * <ol>
 *   <li>Code Parser
 *   <li>Structural Analysis
 *   <li><b>Table Generation</b> (This Phase)
 *   <li>Writing Code
 * </ol>
 * Table Generation is the phase that computes:
 * <ul>
 *   <li> All states
 *   <li> Parsing table
 * </ul>
 *
 * @author jaimegarza@gmail.com
 *
 */
public class TableGenerator extends AbstractPhase {
  private static final int MIN_STATE_ARRAY_LENGTH = 100;
  private static final int STATE_INCR_SIZE = 50;
  private static final int ACCEPT = Integer.MAX_VALUE;

  private State I[] = new State[MIN_STATE_ARRAY_LENGTH];
  private int finalState = 0;
  private int actionNumber = 0;
  private int numberOfGotos;
  private List<String> errorMessages = new LinkedList<String>();

  /**
   * Construct a TableGenerator for an environment
   * @param environment
   */
  public TableGenerator(Environment environment) {
    super(environment);
  }

  /**
   * Locate a dot in the list, by rule and rule item.
   * 
   * @param dots the list of dots
   * @param rule the rule of the dot
   * @param item the rule item of the dot
   * @return a dot in the list, or null
   */
  private Dot findDot( List<Dot> dots, Rule rule, RuleItem item) {
    for (Dot i : dots) {
      if (i.getRule() == rule && i.getItem() == item) {
        return i;
      }
    }
    return null;
  }

  /**
   * Locate a state with the given dots.  Kernel dots
   * fully identify a state
   * @param markers is the set of dots in the state
   * @return a stateIndex, or finalState + 1 (meaning, a new state) 
   */
  private int findStateWithDots(List<Dot> markers) {
    int stateNumber;

    for (stateNumber = 1; stateNumber <= finalState; stateNumber++) {
      List<Dot> originalMarkers = I[stateNumber].getKernelDots();
      // using containsAll as a protective measure since equals implies order as well.
      if (markers.containsAll(originalMarkers)) { //if (markers.equals(originalMarkers)) {
        return stateNumber;
      }
    }
    return finalState + 1;
  }

  /**
   * Compute the closure of a state.  From {@link State}:<p>
   * ...the closure of a dot -- where &chi; &epsilon; <b>N</b> &cup; <b>T</b> and &beta; 
   * &epsilon; (<b>N</b> &cup;
   * <b>T</b>)* -- is defined as <pre>
   * <i>closure</i>(<b>n</b>&#7522; &rarr; &alpha; . &chi;&beta;) =
   * 
   *   1. &empty; if &chi; &epsilon; T or &not;&exist;&chi;&beta;; or
   *   2. {&chi; &rarr; . &delta;; &forall; &chi; &epsilon; N} | &chi; &rarr; <b>&delta;</b> &epsilon; <b>R</b>
   * </pre>
   * <p>
   * @param state is the state whose closure is being computed
   */
  private void closure(State state) {
    for (Dot marker = state.getDot(0); marker != null; marker = marker.next()) {
      if (marker.getItem() != null && marker.getItem().getSymbol() instanceof NonTerminal) {
        for (Rule rule : runtimeData.getRules()) {
          if (rule.getLeftHandId() == marker.getItem().getSymbolId()) {
            Dot auxiliary = findDot(state.getKernelDots(), rule, rule.getItem(0));
            if (auxiliary != null) {
              environment.algorithm.mergeLookaheads(marker, auxiliary);
            } else {
              auxiliary = new Dot(state, rule, rule.getItem(0));
              environment.algorithm.mergeLookaheads(marker, auxiliary);
              state.addClosureDot(auxiliary);
            }
          }
        }
      }
    }
  }

  /**
   * Given a state, write it to the literal report
   * @param stateNum is the desired state
   */
  private void printStateReport(int stateNum) {
    environment.report.println();
    environment.report.printf("State #%3d", stateNum);
    if (I[stateNum].getFrom() >= 0) {
      environment.report.printf(" Goto from state %d with symbol %s\n", I[stateNum].getFrom(), I[stateNum].getSymbol()
          .getName());
    } else {
      environment.report.println(" - Root");
    }
    for (Dot dot : I[stateNum].getKernelDots()) {
      printDotReport(dot);
    }
    if (I[stateNum].getClosureDots().size() > 0) {
      environment.report.println("    ---------------------------------------------------------");
    }
    for (Dot dot : I[stateNum].getClosureDots()) {
      printDotReport(dot);
    }
  }

  /**
   * Given a dot, report it to the textual stream
   * 
   * @param dot is the dot to report
   */
  private void printDotReport(Dot dot) {
    environment.report.printf("%3d ", dot.getRule().getRulenum());
    environment.report.printf("%s -> ", dot.getRule().getLeftHand().getName());
    RuleItem item = dot.getRule().getItem(0);
    if (item == null) {
      environment.report.print(".");
    }
    int i = 0;
    while (item != null) {
      if (dot.getItem() != null && dot.getItem() == item) {
        environment.report.print(". ");
      }
      environment.report.printf("%s ", item.getSymbol().getName());
      item = dot.getRule().getItem(++i);
      if (item == null && dot.getItem() == null) {
        environment.report.print(".");
      }
    }
    environment.algorithm.printLookahead(dot);
    //dot = dot.next();
    environment.report.println();
    //return dot;
  }

  /**
   * given a parser line with transitions, find the one with the highest 
   * frequency such that it can be assumed to be the default.  Usually
   * only terminals are considered in the computation
   * 
   * @param parserLine is the parsing line with state transitions
   * @param numberOfElements restricts the number of elements in the parsing table
   *    to consider
   * @return the default, or 0 if it is best to assume error
   */
  private int computeDefaultAction(int parserLine[], int numberOfElements) {
    int defaultValue = 0, defaultCount = 0;
    int i, j, count;

    for (i = 0; i < numberOfElements; i++) {
      if (parserLine[i] < 0 && parserLine[i] != defaultValue) {
        // Cuenta ocurrencias
        for (j = count = 0; j < numberOfElements; j++) {
          if (parserLine[j] == parserLine[i]) {
            count++;
          }
        }
        if (count > defaultCount) {
          defaultCount = count;
          defaultValue = parserLine[i];
        }
      }
    }

    return defaultValue;
  }

  /**
   * A parsing line is a sparsely populated array with multiple zeros.  This
   * method returns a list of those values that are non zero AND
   * are not the default
   * 
   * @param parserLine is the parser line with state transitions
   * @param defaultValue is the default value to exclude
   * @return the list of {@link Action}s
   */
  private List<Action> packActions(int parserLine[], int defaultValue) {
    List<Action> actions = new LinkedList<Action>();

    // loop parsing row on terminals only
    // remember that parsing tables are indexed by the symbol number, hence i is symbol number
    for (int i = 0; i < runtimeData.getTerminals().size(); i++) {
      if (parserLine[i] != 0 && parserLine[i] != defaultValue) {
        // a shift action with symbol number = i
        for (Symbol symbol : runtimeData.getTerminals()) {
          if (symbol.getId() == i) {
            actions.add(new Action(symbol, parserLine[i]));
            break;
          }
        }
      }
    }
    return actions;
  }

  /**
   * Locate a state with identical actions.  This way they can share them
   * @param stateNumber is the currentState.  Actions will be searched
   * on states prior to this.
   * 
   * @param actions is the list of actions to search
   * @return gthe stateNumber, or -1 if not found
   */
  private int findActions(int stateNumber, List<Action> actions) {
    for (int state = 0; state < stateNumber; state++) {
      if (I[state].getActions().equals(actions)) {
        return state;
      }
    }
    return -1;
  }

  /**
   * Add a non-terminal state transition
   * @param id is the non terminal for the transition in the origin state
   * @param origin the origin state where transition happens
   * @param destination the destination state of the transition 
   */
  private void addGoto(NonTerminal id, int origin, int destination) {
    GoTo goTo = new GoTo(origin, destination);

    id.addGoTo(goTo);
    numberOfGotos++;
  }

  /**
   * An error message was detected.  Add it to the list of messages
   * @param message is the error message.
   * @return the index in the table, which will be used as a pointer in
   *       the parsing table.
   */
  private int addErrorMessage(String message) {
    int index = errorMessages.indexOf(message);
    if (index != -1) {
      return index;
    }
    errorMessages.add(message);
    return errorMessages.size() - 1;
  }

  /**
   * Compress a state's parser line, adding the actions to the packed
   * parser table.
   * 
   * @param parserLine is the parsing table's line
   * @param stateNumber is the current state
   */
  private void packState(int parserLine[], int stateNumber) {
    if (environment.isPacked() == false) {
      return;
    }

    int defaultAction = computeDefaultAction(parserLine, runtimeData.getTerminals().size());
    List<Action> actions = packActions(parserLine, defaultAction);
    int existingState = findActions(stateNumber, actions);
    if (existingState >= 0) {
      actions = I[existingState].getActions();
      I[stateNumber].setPosition(I[existingState].getPosition());
      environment.report.printf("\nActions (same as state %d)\n------------------------------\n", existingState);
    } else {
      I[stateNumber].setPosition(actionNumber);
      environment.report.printf("\nActions\n--------\n");
      actionNumber += actions.size();
    }
    I[stateNumber].setDefaultValue(defaultAction);
    I[stateNumber].setActions(actions);
    int tokenCount = 0;
    Action errorToken = null;
    for (Action action : actions) {
      environment.report.printf("    With %s ", action.getSymbol().getName());
      if (action.getStateNumber() < 0) {
        environment.report.printf("Reduce by rule %d\n", -action.getStateNumber());
      } else if (action.getStateNumber() == ACCEPT) {
        environment.report.printf("Accept\n");
      } else {
        environment.report.printf("Shift to state %d\n", action.getStateNumber());
        errorToken = action;
        tokenCount++;
      }
    }
    // compute and emit GOTO's
    int symbolCount = 0;

    int terminals = runtimeData.getTerminals().size();
    NonTerminal errorSymbol = null;
    for (int i = 0; i < runtimeData.getNonTerminals().size(); i++) {
      if (parserLine[terminals + i] != 0) {
        NonTerminal symbol = null;
        for (NonTerminal id : runtimeData.getNonTerminals()) {// = pNTtable;
                                                              // id;
                                                              // id=id.next) {
          if (id.getId() == i + terminals) {
            symbol = id;
            break;
          }
        }
        if (symbol == null) {
          continue;
        }
        environment.report.printf("    With %s Goto %d\n", symbol.getName(), parserLine[i + terminals]);
        symbolCount++;
        errorSymbol = symbol;
        addGoto(symbol, stateNumber, parserLine[i + terminals]);
      }
    }
    // imprime default
    environment.report.printf("    Default: ");
    if (defaultAction < 0) {
      environment.report.printf("Reduce by rule %d\n", -defaultAction);
    } else {
      environment.report.printf("Error\n");
    }
    
    environment.report.printf("\nErrors\n-------\n");
    if (tokenCount == 1) {
      String message = "";
      message = errorToken.getSymbol().getFullName() + " expected";
      environment.report.println("    " + message);
      I[stateNumber].setMessage(addErrorMessage(message));
    } else if (symbolCount == 1) {
      String message = "Expecting " + errorSymbol.getFullName();
      environment.report.println("    " + message);
      I[stateNumber].setMessage(addErrorMessage(message));
    } else if (tokenCount != 0 && (tokenCount < symbolCount || symbolCount == 0)) {
      StringBuilder messageBuffer = new StringBuilder();
      int i = 0;
      for (Action action : actions) {
        if (action.getStateNumber() > 0 && action.getStateNumber() != ACCEPT) {
          if (i > 0) {
            if (i == tokenCount - 1) {
              messageBuffer.append(" or ");
            } else {
              messageBuffer.append(", ");
            }
          }
          messageBuffer.append(action.getSymbol().getFullName());
          i++;
        }
      }
      messageBuffer.append(" expected");
      environment.report.println("    " + messageBuffer.toString());
      I[stateNumber].setMessage(addErrorMessage(messageBuffer.toString()));
    } else if (symbolCount != 0) {
      StringBuilder messageBuffer = new StringBuilder("Expecting ");
      int j = 0;
      for (int i = 0; i < runtimeData.getNonTerminals().size(); i++) {
        if (parserLine[terminals + i] != 0) {
          for (NonTerminal nonTerminal : runtimeData.getNonTerminals()) {
            if (nonTerminal.getId() == i + terminals) {
              if (j > 0) {
                if (j == symbolCount - 1) {
                  messageBuffer.append(" or ");
                } else {
                  messageBuffer.append(", ");
                }
              }
              messageBuffer.append(nonTerminal.getFullName());
              break;
            }
          }
          j++;
          /*
           * putting this break for now because in the example I was trying just
           * putting the first one seems OK
           */
          break;
        }
      }
      environment.report.println("    " + messageBuffer.toString());
      I[stateNumber].setMessage(addErrorMessage(messageBuffer.toString()));
    } else {
      I[stateNumber].setMessage(-1);
    }
  }

  /**
   * Given the associativity and order of rules find IF a shift/reduce 
   * can be resolved to either shift, or reduce.
   * <p>
   * Precedence conflicts occur in ambiguous grammars where if is both possible 
   * to have a shift or a reduce.  Note should be made that sometimes is both
   * convenient for readability and parser size. 
   * <p>
   * To declare precedence, bot rules and tokens can be assigned
   * a precedence and associativity.  For tokens the precedence gets
   * declared with <b>%left</b>, <b>%right</b> or <b>%binary</b>
   * 
   * Consider the following example with an ambiguous non-LR grammar, (or non LL
   * top down for that matter)<pre><code>
   * 
   * E -> E + E
   * E -> E - E
   * E -> E * E
   * E -> E / E
   * E -> - E
   * E -> number
   * </code></pre>
   * 
   * To solve it, the first step is to declare the tokens precedence<pre><code>
   * %left +, -  <--- sets the associativity, and by virtue of being above, lower precedence.
   * %left *, /  <--- sets the associativity, abd by being second higher precedence
   * </code></pre>
   * 
   * There is still a second conflict caused by E -> -E.  We know that unary minus is right associative
   * so we declare a new token (new since - is already used)<pre><code>
   * %right UMINUS
   * </code></pre>
   * and then assign the precedence <b>directly</b> to the rule.  This is a contextual precedence<pre><code>
   * E -> -E %prec UMINUS  <--- gives the rule higher precedence
   * </code></pre>
   * Rules in general get their precedence from the last non-terminal in the rule, or by %prec<p>
   * Precedence gets resolved in the generation as follows:
   * <ol>
   *   <li>if the non-terminal precedence is higher than that of the rule, make it a shift conflict is higher, take the shift
   *   <li>if the non terminal precedence is lower than that of the rule, make it a reduce.
   *   <li>if the non-terminal and the rule have the same precedence, use associativity:
   *     <ul>
   *       <li>left associativity implies <b>reduce</b>
   *       <li>right associativity implies <b>shift</b>
   *       <li>binary associativity implies <b>error</b>
   *     </ul>
   * </ol>
   * in cases when the shift/reduce conflict has not been solved, shift is taken, and a warning is printed.<p>
   * @param parserLine the parser line contents
   * @param tkn the token causing the conflict
   * @param rule the rule involved in the conflict
   * @return true if properly resolved
   */
  private boolean resolveShiftReduceConflict(int parserLine[], Symbol tkn, Rule rule) {
    Associativity association;

    if (tkn.getPrecedence() == 0 || rule.getPrecedence() == 0) {
      return false;
    }

    if (tkn.getPrecedence() == rule.getPrecedence()) {
      association = tkn.getAssociativity();
    } else if (tkn.getPrecedence() > rule.getPrecedence()) {
      association = Associativity.RIGHT;
    } else {
      association = Associativity.LEFT;
    }

    switch (association) {
      case NONE:
      case BINARY:
        return false;

      case LEFT:
        parserLine[tkn.getId()] = -rule.getRulenum();
        environment.report.printf("Conflict with %s resolved by Reduce\n", tkn.getName());
        break;

      case RIGHT:
        environment.report.printf("Conflict with %s resolved by Shift\n", tkn.getName());
        break;
    }

    return true;
  }

  /**
   * Complete the reduces of a parser line in the given state
   * @param parserLine the state's parser line
   * @param stateNumber the state number
   */
  private void computeReduce(int parserLine[], int stateNumber) {
    for (Dot dot : I[stateNumber].getAllDots()) {
      if (dot.getItem() == null) { // I like dots at the end of rules
        if (dot.getRule().getLeftHand().equals(runtimeData.getRoot())) {
          environment.report.println("ACCEPT BY " + -dot.getRule().getRulenum());
          parserLine[0] = ACCEPT;
        } else {
          environment.report.printf("REDUCE BY RULE %d\n", dot.getRule().getRulenum());
          for (Symbol tkn : runtimeData.getTerminals()) {
            boolean containsToken = environment.algorithm.dotContains(dot, tkn.getId());
            if (containsToken) {
              if (parserLine[tkn.getId()] > 0) {
                if (!resolveShiftReduceConflict(parserLine, tkn, dot.getRule())) {
                  environment.error(dot.getRule().getLineNumber(),
                      "Warning: Shift/Reduce conflict on state %d[%s Shift:%d Reduce:%d].", stateNumber, tkn.getName(),
                      parserLine[tkn.getId()], dot.getRule().getRulenum());
                }
              } else if (parserLine[tkn.getId()] < 0) {
                environment.error(dot.getRule().getLineNumber(),
                    "Warning: Reduce/Reduce conflict on state %d[%s Reduce:%d Reduce:%d].", stateNumber, tkn.getName(),
                    -parserLine[tkn.getId()], dot.getRule().getRulenum());
                parserLine[tkn.getId()] = Math.max(-dot.getRule().getRulenum(), parserLine[tkn.getId()]);
              } else {
                parserLine[tkn.getId()] = -dot.getRule().getRulenum(); // reduce
              }
            }
          }
        }
      }
    }
  }

  /**
   * Compact the go to table
   */
  private void compactGotos() {
    int defaultValue;
    int numElems, position;

    position = 0;
    for (NonTerminal id : runtimeData.getNonTerminals()) {
      defaultValue = id.getDefaultGoto();
      if (defaultValue != 0) {
        numElems = id.removeGoto(defaultValue);
        numberOfGotos--;
        id.setToken(position);
        id.appendGoto(-1, defaultValue);
        numberOfGotos++;
        position += numElems + 1;
      }
    }
  }

  /**
   * Get the starting symbol, and put a dot on each rule that 
   * starts with it.
   * 
   * @return the list of dots
   */
  private List<Dot> computeStartingDots() {
    List<Dot>initialDots = new LinkedList<Dot>();
    for (Rule rule : runtimeData.getRules()) {
      if (rule.getLeftHand().equals(runtimeData.getRoot())) {
        Dot dot = new Dot(I[0], rule, rule.getItem(0));
        environment.algorithm.initializeDot(dot);
        initialDots.add(dot);
      }
    }
    return initialDots;
  }

  /**
   * Create a new state, with its originating state and symbol transition, 
   * adding the dots for it.
   * 
   * @param fromState the state where the state transition originated
   * @param symbol is the transition symbol
   * @param dots is the elements of the state
   */
  private void createNewState(int stateNumber, int fromState, Symbol symbol, List<Dot> dots) {
    if (stateNumber >= I.length) { // resize if needed
      I = Arrays.copyOf(I, I.length + STATE_INCR_SIZE);
    }
    I[stateNumber] = new State(stateNumber, fromState, symbol);
    for (Dot m : dots) {
      m.setState(I[stateNumber]);
    }
    I[stateNumber].addAllKernelDots(dots);
    closure(I[stateNumber]);
    if (environment.isDebug()) {
      System.out.println("Created new state " + stateNumber + ":\n" + I[stateNumber]);
    }
  }

  /**
   * Moves a given dot to the right.
   * Check each dot after the given dot and check to see if it points to the
   * same symbol.  if it does create a new dot pointing to after the symbol.<p>
   * 
   * This operation may result in more than one dot.
   *   
   * @param state being reviewed
   * @param dot the dot being checked
   * @return
   */
  private List<Dot> moveDotToTheRight(State state, Dot dot) {
    List<Dot> auxiliaryMarkers = new LinkedList<Dot>();
    for (Dot lookaheadMarker = dot; lookaheadMarker != null; lookaheadMarker = lookaheadMarker.next()) {
      if (lookaheadMarker.getItem() != null &&
          lookaheadMarker.getItem().getSymbol().equals(dot.getItem().getSymbol())) {
        Dot auxiliary = new Dot(state, lookaheadMarker.getRule(), lookaheadMarker.nextItem());
        environment.algorithm.addAllLookaheads(auxiliary, lookaheadMarker);
        auxiliaryMarkers.add(auxiliary);
      }
    }
    return auxiliaryMarkers;
  }

  /**
   * Go dot by dot computing state transitions for each of the pointed symbols.
   * 
   * @param parserLine is the current parser table's line
   * @param stateIndex is the current state
   * @return
   */
  private boolean computeStateTransitions(int[] parserLine, int stateIndex) {
    boolean affected = false;
    
    Arrays.fill(parserLine, 0);

    for (Dot marker = I[stateIndex].getDot(0); marker != null; marker = marker.next()) {
      if (marker.getItem() != null && parserLine[marker.getItem().getSymbolId()] == 0) {
        List<Dot> auxiliaryMarkers = moveDotToTheRight(I[stateIndex], marker);
        int gotoState = findStateWithDots(auxiliaryMarkers);
        if (gotoState > finalState) { // meaning, this is a new state
          createNewState(++finalState, stateIndex, marker.getItem().getSymbol(), auxiliaryMarkers);
          gotoState = finalState;
        } else {
          // Merge lookaheads
          if (environment.algorithm.addLookaheadsToState(I, gotoState, auxiliaryMarkers)) {
            I[gotoState].setReview(true);
            affected = true;
          }
        }
        if (environment.isDebug()) {
          System.out.println("On state " + stateIndex + " with " + marker.getItem().getSymbol() + " go to " + gotoState);
        }
        parserLine[marker.getItem().getSymbolId()] = gotoState;
      }
    }
    return affected;
  }

  /**
   * finalize a state
   * @param parserLine is the parsing table's current row
   * @param stateNumber is the current state
   */
  private void completeState(int[] parserLine, int stateNumber) {
    environment.report.println("    ---------------------------------------------------------");
    for (int i = 0; i < parserLine.length; i++) {
      if (parserLine[i] > 0) {
        Symbol s = runtimeData.findTerminalById(i);
        if (s == null) {
          s = runtimeData.findNonTerminalById(i);
        }
        environment.report.printf("SHIFT ON %s TO STATE %d\n", s.getName(), parserLine[i]);
      }
    }
    computeReduce(parserLine, stateNumber);
    I[stateNumber].setMessage(-1);
    I[stateNumber].setRow(parserLine);
    packState(parserLine, stateNumber);
  }

  /**
   * Set data to runtimeData and make sure the state
   * array is the proper size.
   */
  private void completeGeneration() {
    compactGotos();

    // reduce size to proper length
    I = Arrays.copyOf(I, finalState + 1);

    runtimeData.setStates(I);
    runtimeData.setNumberOfActions(actionNumber);
    runtimeData.setNumberOfGoTos(numberOfGotos);
    runtimeData.setErrorMessages(errorMessages);
  }
  /**
   * Generates the parsing table by computing the states of the grammar.
   */
  public void execute() {
    int parserLine[] = new int[runtimeData.getTerminals().size() + runtimeData.getNonTerminals().size()];
    List<Dot> initialDots = computeStartingDots();
    createNewState(0, -1, null, initialDots);

    // make sure that for multi-pass algorithms we do not initially print.
    boolean finalPhase = environment.algorithm.isMultiPass() == false;
    boolean completed = false;
    finalState = 0;
    // generate till the end. Loop multiple times until all resolved and all printed.
    while (!completed) {
      if (environment.isVerbose()) {
        System.out.println("\n\nStarting a new phase with " + finalPhase + " and " + finalState + " states");
      }
      int affected = 0;
      for (int stateIndex = 0;stateIndex <= finalState; stateIndex++) {
        //skip state if not to be reviewed (unless we want to print it)
        if (I[stateIndex].isReview() == false && finalPhase == false) {
          continue;
        }
        if (environment.isVerbose()) {
          System.out.printf("Reviewing state %d of %d\n", stateIndex, finalState);
        }
        if (finalPhase) {
          printStateReport(stateIndex);
        }

        if (computeStateTransitions(parserLine, stateIndex)) {
          affected++;
        }
        
        if (finalPhase) {
          completeState(parserLine, stateIndex);
        }
        I[stateIndex].setReview(false);
      }
      
      if (finalPhase) {
        completed = true;
      } else if (affected == 0) {
        finalPhase = true;
      }
    }
    completeGeneration();
  }
}
