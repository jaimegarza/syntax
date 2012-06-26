package me.jaimegarza.syntax.generator;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import me.jaimegarza.syntax.cli.Algorithm;
import me.jaimegarza.syntax.cli.Environment;
import me.jaimegarza.syntax.definition.Action;
import me.jaimegarza.syntax.definition.Associativity;
import me.jaimegarza.syntax.definition.Dot;
import me.jaimegarza.syntax.definition.GoTo;
import me.jaimegarza.syntax.definition.LookAhead;
import me.jaimegarza.syntax.definition.NonTerminal;
import me.jaimegarza.syntax.definition.Rule;
import me.jaimegarza.syntax.definition.RuleItem;
import me.jaimegarza.syntax.definition.State;
import me.jaimegarza.syntax.definition.Symbol;
import me.jaimegarza.syntax.definition.Terminal;

public class TableGenerator extends AbstractPhase {
  private static final int MIN_STATE_ARRAY_LENGTH = 100;
  private static final int STATE_INCR_SIZE = 50;
  private static final int ACCEPT = Integer.MAX_VALUE;

  private State I[] = new State[MIN_STATE_ARRAY_LENGTH];
  private int finalState = 0;
  private int actionNumber = 0;
  private int numberOfGotos;
  private List<String> errorMessages = new LinkedList<String>();

  public TableGenerator(Environment environment, RuntimeData runtimeData) {
    super();
    this.environment = environment;
    this.runtimeData = runtimeData;
  }

  Dot isThere(Rule rule, RuleItem item, List<Dot> markers) {
    for (Dot i : markers) {
      if (i.getRule() == rule && i.getItem() == item) {
        return i;
      }
    }
    return null;
    // return rules.contains(i) ? i : null;
  }

  LookAhead getLookAhead(Rule rule, RuleItem itm) {
    LookAhead l = new LookAhead();
    if (itm == null) {
      return l;
    }

    int index = rule.getItems().indexOf(itm);
    if (index == -1) {
      return l;
    }

    l.setCarry(true);

    if (index >= rule.getItems().size() - 1) {
      return l;
    }

    index++;
    while (index < rule.getItems().size()) {
      itm = rule.getItem(index);
      if (itm.getSymbol() instanceof Terminal) {
        l.getSymbolIds().add(itm.getSymbolId());
        l.setCarry(false);
        break;
      } else {
        l.getSymbolIds().addAll(((NonTerminal) itm.getSymbol()).getFirst());
        if (!isEmpty(itm.getSymbolId())) {
          l.setCarry(false);
          break;
        }
      }
      index++;
    }
    return l;
  }

  private int locate(List<Dot> ik) {
    int h;

    for (h = 1; h <= finalState; h++) {
      List<Dot> originalMarkers = I[h].getOriginalMarkers();
      if (ik.equals(originalMarkers)) {
        if (environment.isDebug()) {
          System.out.println("Equals " + ik + " against markers in state " + h + " " + originalMarkers);
        }
        return h;
      }
    }
    return finalState + 1;
  }

  private boolean mergeLookAheads(int j, List<Dot> ik) {
    Dot i, k;

    if (environment.getAlgorithm() != Algorithm.LALR) {
      return false;
    }

    i = I[j].getMarker(0);
    k = ik.get(0);

    if (i.getLookahead() != null && k.getLookahead() != null && i.getLookahead().containsAll(k.getLookahead())) {
      return false;
    }

    while (i != null && k != null && i.getRule().equals(k.getRule()) && i.getItem().equals(k.getItem())) {

      i.getLookahead().addAll(k.getLookahead());
      i = i.next();
      k = k.next();
    }
    return true;
  }

  private void closure(State state) {
    for (Dot marker = state.getMarker(0); marker != null; marker = marker.next()) {
      if (marker.getItem() != null && marker.getItem().getSymbol() instanceof NonTerminal) {
        for (Rule rule : runtimeData.getRules()) {
          if (rule.getLeftHandId() == marker.getItem().getSymbolId()) {
            Dot auxiliary = isThere(rule, rule.getItem(0), state.getMarkers());
            if (auxiliary != null) {
              if (environment.getAlgorithm() == Algorithm.LALR) {
                LookAhead l = getLookAhead(rule, marker.getItem());
                auxiliary.getLookahead().addAll(l.getSymbolIds());
                if (l.isCarry()) {
                  marker.getLookahead().addAll(auxiliary.getLookahead());
                }
              }
            } else {
              auxiliary = new Dot(state, rule, rule.getItem(0));
              if (environment.getAlgorithm() == Algorithm.LALR) {
                LookAhead l = getLookAhead(rule, marker.getItem());
                auxiliary.addAllLookaheads(l.getSymbolIds());
                if (l.isCarry()) {
                  marker.getLookahead().addAll(auxiliary.getLookahead());
                }
              }
              state.getMarkers().add(auxiliary);
            }
          }
        }
      }
    }
  }

  void printLookahead(Set<Integer> c) {
    if (c == null || environment.getAlgorithm() != Algorithm.LALR) {
      return;
    }

    environment.report.print("     { ");
    for (Symbol tkn : runtimeData.getTerminals()) {
      if (c.contains(tkn.getId())) {
        environment.report.print(tkn.getName() + " ");
      }
    }
    environment.report.print("}");
  }

  void printStateReport(int stateNum) {
    List<Dot> markers = I[stateNum].getMarkers();
    environment.report.println();
    environment.report.printf("State #%3d", stateNum);
    if (I[stateNum].getFrom() >= 0) {
      environment.report.printf(" Goto from state %d with symbol %s\n", I[stateNum].getFrom(), I[stateNum].getSymbol()
          .getName());
    } else {
      environment.report.println(" - Root");
    }
    for (Dot dot : markers) {
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
      printLookahead(dot.getLookahead());
      dot = dot.next();
      environment.report.println();
    }
  }

  int computeDefaultAction(int parserLine[], int nElems) {
    int iDefa = 0, iDefaCount = 0;
    int i, j, count;

    for (i = 0; i < nElems; i++) {
      if (parserLine[i] < 0 && parserLine[i] != iDefa) {
        // Cuenta ocurrencias
        for (j = count = 0; j < nElems; j++) {
          if (parserLine[j] == parserLine[i]) {
            count++;
          }
        }
        if (count > iDefaCount) {
          iDefaCount = count;
          iDefa = parserLine[i];
        }
      }
    }

    return iDefa;
  }

  private List<Action> packActions(int parserLine[], int iDefa) {
    List<Action> actions = new LinkedList<Action>();

    // loop parsing row on terminals only
    for (int i = 0; i < runtimeData.getTerminals().size(); i++) {
      if (parserLine[i] != 0 && parserLine[i] != iDefa) {
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

  int findActions(int stateNumber, List<Action> actions) {
    for (int state = 0; state < stateNumber; state++) {
      if (I[state].getActions().equals(actions)) {
        return state;
      }
    }
    return -1;
  }

  private void addGoto(NonTerminal id, int origen, int destino) {
    GoTo goTo = new GoTo(origen, destino);

    id.addGoTo(goTo);
    numberOfGotos++;
  }

  private int addErrorMessage(String message) {
    int index = errorMessages.indexOf(message);
    if (index != -1) {
      return index;
    }
    errorMessages.add(message);
    return errorMessages.size() - 1;
  }

  private void packState(int parserLine[], int estado) {
    /*
     * int t1, t2, col, defa, i, j, cTokens, cSimbolos; PACTION pAcciones, pAct,
     * pCual, pNext, pErrToken; PIDENT id, pErrSymbol; static char msg[1024];
     */

    if (environment.isPacked() == false) {
      return;
    }

    int defa = computeDefaultAction(parserLine, runtimeData.getTerminals().size());
    List<Action> actions = packActions(parserLine, defa);
    int existingState = findActions(estado, actions);
    if (existingState >= 0) {
      actions = I[existingState].getActions();
      I[estado].setPosition(I[existingState].getPosition());
      environment.report.printf("\nActions (same as state %d)\n------------------------------\n", existingState);
    } else {
      I[estado].setPosition(actionNumber);
      environment.report.printf("\nActions\n--------\n");
      actionNumber += actions.size();
    }
    I[estado].setDefaultValue(defa);
    I[estado].setActionSize(actions.size());
    I[estado].setActions(actions);
    int cTokens = 0;
    Action pErrToken = null;
    for (Action pAct : actions) {
      environment.report.printf("\tWith %s ", pAct.getSymbol().getName());
      if (pAct.getStateNumber() < 0) {
        environment.report.printf("Reduce by rule %d\n", -pAct.getStateNumber());
      } else if (pAct.getStateNumber() == ACCEPT) {
        environment.report.printf("Accept\n");
      } else {
        environment.report.printf("Shift to state %d\n", pAct.getStateNumber());
        pErrToken = pAct;
        cTokens++;
      }
    }
    // compute and emit GOTO's
    int cSimbolos = 0;

    int terminals = runtimeData.getTerminals().size();
    NonTerminal pErrSymbol = null;
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
        environment.report.printf("\tWith %s Goto %d\n", symbol.getName(), parserLine[i + terminals]);
        cSimbolos++;
        pErrSymbol = symbol;
        addGoto(symbol, estado, parserLine[i + terminals]);
      }
    }
    // imprime default
    environment.report.printf("\tDefault: ");
    if (defa < 0) {
      environment.report.printf("Reduce by rule %d\n", -defa);
    } else {
      environment.report.printf("Error\n");
    }
    environment.report.printf("\nErrors\n-------\n");
    if (cTokens == 1) {
      String message = "";
      message = pErrToken.getSymbol().getFullName() + " expected";
      environment.report.println("\t" + message);
      I[estado].setMessage(addErrorMessage(message));
    } else if (cSimbolos == 1) {
      String message = "Expecting " + pErrSymbol.getFullName();
      environment.report.println("\t" + message);
      I[estado].setMessage(addErrorMessage(message));
    } else if (cTokens != 0 && (cTokens < cSimbolos || cSimbolos == 0)) {
      StringBuilder messageBuffer = new StringBuilder();
      int i = 0;
      for (Action action : actions) {
        if (action.getStateNumber() > 0 && action.getStateNumber() != ACCEPT) {
          if (i > 0) {
            if (i == cTokens - 1) {
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
      environment.report.println("\t" + messageBuffer.toString());
      I[estado].setMessage(addErrorMessage(messageBuffer.toString()));
    } else if (cSimbolos != 0) {
      StringBuilder messageBuffer = new StringBuilder("Expecting ");
      int j = 0;
      for (int i = 0; i < runtimeData.getNonTerminals().size(); i++) {
        if (parserLine[terminals + i] != 0) {
          for (NonTerminal nonTerminal : runtimeData.getNonTerminals()) {
            if (nonTerminal.getId() == i + terminals) {
              if (j > 0) {
                if (j == cSimbolos - 1) {
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
      environment.report.println("\t" + messageBuffer.toString());
      I[estado].setMessage(addErrorMessage(messageBuffer.toString()));
    } else {
      I[estado].setMessage(-1);
    }
  }

  boolean Precedence(int parserLine[], Symbol tkn, Rule rule) {
    Associativity association;

    /* Alguno no tiene precedencia ?? */
    if (tkn.getPrecedence() == 0 || rule.getPrecedence() == 0) {
      return true;
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
        return true;

      case LEFT:
        parserLine[tkn.getId()] = -rule.getRulenum();
        environment.report.printf("Conflict with %s resolved by Reduce\n", tkn.getName());
        break;

      case RIGHT:
        environment.report.printf("Conflict with %s resolved by Shift\n", tkn.getName());
        break;
    }

    return false;
  }

  private void computeReduce(int parserLine[], int estado) {
    for (Dot dot : I[estado].getMarkers()) {
      if (dot.getItem() == null) {
        if (dot.getItem() != null && dot.getItem().getSymbol().equals(runtimeData.getRoot())) {
          environment.report.println("ACCEPT BY " + -dot.getRule().getRulenum());
          parserLine[0] = ACCEPT;
        } else {
          environment.report.printf("REDUCE BY -%d\n", dot.getRule().getRulenum());
          for (Symbol tkn : runtimeData.getTerminals()) {
            boolean containsToken;
            if (environment.getAlgorithm() == Algorithm.LALR) {
              containsToken = dot.getLookahead().contains(tkn.getId());
            } else {
              containsToken = dot.getRule().getLeftHand().getFollow().contains(tkn.getId());
            }
            if (containsToken) {
              if (parserLine[tkn.getId()] > 0) {
                if (Precedence(parserLine, tkn, dot.getRule())) {
                  environment.error(dot.getRule().getLineNumber(),
                      "Warning: Shift/Reduce conflict on state %d[%s Shift:%d Reduce:%d].", estado, tkn.getName(),
                      parserLine[tkn.getId()], dot.getRule().getRulenum());
                }
              } else if (parserLine[tkn.getId()] < 0) {
                environment.error(dot.getRule().getLineNumber(),
                    "Warning: Reduce/Reduce conflict on state %d[%s Reduce:%d Reduce:%d].", estado, tkn.getName(),
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
  
  private int AgregaGoto(NonTerminal id, int origen, int destino) {
    GoTo goTo = new GoTo(origen, destino);
    id.addGoTo(goTo);
    numberOfGotos++;
    return 1;
  }

  private int QuitaGoto(NonTerminal id, int iDefa) {
    for (int i = 0; i < id.getGotos().size(); i++) {
      if (id.getGotos().get(i).getDestination() == iDefa) {
        id.getGotos().remove(i);
        numberOfGotos--;
      }
    }
    return id.getGotos().size();
  }

  private int GotoDefault(NonTerminal id) {
    int iDefa = 0, iDefaCount = 0;
    int count;

    for (GoTo pGoto : id.getGotos()) {
      if (pGoto.getDestination() != iDefa) {
        count = 0;
        for (GoTo pAux : id.getGotos()) {
          if (pAux.getDestination() == pGoto.getDestination()) {
            count++;
          }
        }
        if (count > iDefaCount) {
          iDefaCount = count;
          iDefa = pGoto.getDestination();
        }
      }
    }

    return iDefa;
  }
  private void CompactaGotos() {
    int iDefa;
    int nElems, iPosicion;

    iPosicion = 0;
    for (NonTerminal id : runtimeData.getNonTerminals()) {
      iDefa = GotoDefault(id);
      if (iDefa != 0) {
        nElems = QuitaGoto(id, iDefa);
        id.setToken(iPosicion);
        AgregaGoto(id, -1, iDefa);
        iPosicion += nElems + 1;
      }
    }
  }

  public void execute() {
    int parserLine[] = new int[runtimeData.getTerminals().size() + runtimeData.getNonTerminals().size()];

    // Generate state 0
    I[0] = new State(0, -1, null);
    for (Rule rule : runtimeData.getRules()) {
      if (rule.getLeftHand().equals(runtimeData.getRoot())) {
        Dot dot = new Dot(I[0], rule, rule.getItem(0));
        if (environment.getAlgorithm() == Algorithm.LALR) {
          dot.addLookahead(0); // empty element
        }
        I[0].addMarker(dot);
      }
    }
    I[0].mark();
    closure(I[0]);
    if (environment.isDebug()) {
      printDebuggingState(0, I[0]);
    }

    // generate till the end
    boolean doPrint = environment.getAlgorithm() != Algorithm.LALR;
    finalState = 0;
    while (2 > 1) { // forever
      int stateIndex = 0;
      int affected = 0;
      while (stateIndex <= finalState) {
        if (!doPrint && !I[stateIndex].isReview()) {
          stateIndex++;
          continue;
        }
        if (environment.isVerbose()) {
          System.out.printf("Reviewing state %05d of %05d\r", stateIndex, finalState);
        }
        // empty line
        Arrays.fill(parserLine, 0);
        Dot marker = I[stateIndex].getMarker(0);
        // I[stateIndex].mark();
        // closure(I[stateIndex]);
        if (doPrint) {
          printStateReport(stateIndex);
        }

        // generate goto
        // marker points to the rules in the state
        while (marker != null) {
          if (marker.getItem() != null && parserLine[marker.getItem().getSymbolId()] == 0) {
            List<Dot> auxiliaryMarkers = new LinkedList<Dot>();
            for (Dot lookaheadMarker = marker; lookaheadMarker != null; lookaheadMarker = lookaheadMarker.next()) {
              if (lookaheadMarker.getItem() != null &&
                  lookaheadMarker.getItem().getSymbol().equals(marker.getItem().getSymbol())) {
                Dot auxiliary = new Dot(I[stateIndex], lookaheadMarker.getRule(), lookaheadMarker.nextRuleEntry());
                if (environment.getAlgorithm() == Algorithm.LALR) {
                  auxiliary.addAllLookaheads(lookaheadMarker.getLookahead());
                }
                auxiliaryMarkers.add(auxiliary);
              }
            }
            // I[stateIndex].cutToMark();
            int gotoState = locate(auxiliaryMarkers);
            // I[stateIndex].restore();
            if (gotoState > finalState) {
              if (++finalState >= I.length) { // resize if needed
                I = Arrays.copyOf(I, I.length + STATE_INCR_SIZE);
              }
              I[finalState] = new State(finalState, stateIndex, marker.getItem().getSymbol());
              for (Dot m : auxiliaryMarkers) {
                m.setState(I[finalState]);
              }
              I[finalState].addAllMarkers(auxiliaryMarkers);
              I[finalState].mark();
              closure(I[finalState]);
              if (environment.isDebug()) {
                printDebuggingState(finalState, I[finalState]);
              }
              gotoState = finalState;
            } else {
              // Merge lookaheads
              if (mergeLookAheads(gotoState, auxiliaryMarkers)) {
                I[gotoState].setReview(true);
                affected++;
              }
            }
            if (environment.isDebug()) {
              System.out.println("On state " +
                                 stateIndex +
                                   " with " +
                                   marker.getItem().getSymbol() +
                                   " go to " +
                                   gotoState);
            }
            parserLine[marker.getItem().getSymbolId()] = gotoState;
          }
          marker = marker.next();
        }/* while k <> null */
        if (doPrint) {
          computeReduce(parserLine, stateIndex);
          I[stateIndex].setMessage(-1);
          I[stateIndex].setRow(parserLine);
          packState(parserLine, stateIndex);
        }
        I[stateIndex].setReview(false);
        stateIndex++;
      }
      if (doPrint) {
        break;
      } else if (affected == 0) {
        doPrint = true;
      }
    }

    CompactaGotos();
    
    I = Arrays.copyOf(I, finalState + 1);
    runtimeData.setStates(I);
    runtimeData.setNumberOfActions(actionNumber);
    runtimeData.setNumberOfGoTos(numberOfGotos);
    runtimeData.setErrorMessages(errorMessages);
  }

  private void printDebuggingState(int stateNum, State state) {
    System.out.println("Created new state " + stateNum + ":\n" + state);
  }
}