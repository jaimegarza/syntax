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
package me.jaimegarza.syntax.generator;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import me.jaimegarza.syntax.ParsingException;
import me.jaimegarza.syntax.cli.Environment;
import me.jaimegarza.syntax.cli.Language;
import me.jaimegarza.syntax.definition.Associativity;
import me.jaimegarza.syntax.definition.ErrorToken;
import me.jaimegarza.syntax.definition.NonTerminal;
import me.jaimegarza.syntax.definition.Rule;
import me.jaimegarza.syntax.definition.RuleItem;
import me.jaimegarza.syntax.definition.Symbol;
import me.jaimegarza.syntax.definition.Terminal;
import me.jaimegarza.syntax.definition.Type;
import me.jaimegarza.syntax.generator.parser.Grammar;
import me.jaimegarza.syntax.generator.parser.Parser;
import me.jaimegarza.syntax.generator.parser.ParserAction;
import me.jaimegarza.syntax.generator.parser.ParserGoTo;
import me.jaimegarza.syntax.generator.parser.ReservedWord;
import me.jaimegarza.syntax.generator.parser.StackElement;

public class CodeParser extends AbstractPhase {
  private static final String DISTINGUISHED_SYMBOL_NAME = "Sys$Root";
  private static final int TOK_MARCA = 256;
  private static final int TOK_START = 257;
  private static final int TOK_TOKEN = 258;
  private static final int TOK_TYPE = 259;
  private static final int TOK_UNION = 260;
  private static final int TOK_TYPENAME = 261;
  private static final int TOK_TERM = 262;
  private static final int TOK_LEFT = 263;
  private static final int TOK_RIGHT = 264;
  private static final int TOK_BINARY = 265;
  private static final int TOK_ERRDEF = 266;
  private static final int TOK_NUM = 267;
  private static final int TOK_PREC = 268;
  private static final int TOK_NAME = 269;
  private static final int TOK_ERROR = 270;
  private static final int TOK_LEXER = 271;
  private static final int TOK_RX_PIPE = 272;
  private static final int TOK_RX_LPAR = 273;
  private static final int TOK_RX_RPAR = 274;
  private static final int TOK_RX_STAR = 275;
  private static final int TOK_RX_PLUS = 276;
  private static final int TOK_RX_HUH = 277;
  private static final int TOK_RX_ANY = 278;
  @SuppressWarnings("unused")
  private static final int TOK_RX_CHARS = 279;
  @SuppressWarnings("unused")
  private static final int TOK_LEXCODE = 280;
  private static final int TOK_CHARS = 281;

  private static final String tokenNames[] = { "TOK_MARCA", "TOK_START", "TOK_TOKEN", "TOK_TYPE", "TOK_UNION",
      "TOK_TYPENAME", "TOK_TERM", "TOK_LEFT", "TOK_RIGHT", "TOK_BINARY", "TOK_ERRDEF", "TOK_NUM", "TOK_PREC",
      "TOK_NAME", "TOK_ERROR", "TOK_LEXER", "TOK_RX_PIPE", "TOK_RX_LPAR", "TOK_RX_RPAR", "TOK_RX_STAR", "TOK_RX_PLUS",
      "TOK_RX_HUH", "TOK_RX_ANY", "TOK_RX_CHARS", "TOK_LEXCODE", "TOK_CHARS" };

  private static final ReservedWord RWord[] = { new ReservedWord("token", TOK_TERM),
      new ReservedWord("term", TOK_TERM), new ReservedWord("left", TOK_LEFT), new ReservedWord("nonassoc", TOK_BINARY),
      new ReservedWord("binary", TOK_BINARY), new ReservedWord("right", TOK_RIGHT), new ReservedWord("prec", TOK_PREC),
      new ReservedWord("start", TOK_START), new ReservedWord("type", TOK_TYPE), new ReservedWord("union", TOK_UNION),
      new ReservedWord("name", TOK_NAME), new ReservedWord("error", TOK_ERRDEF), new ReservedWord("lexer", TOK_LEXER) };

  private static final ParserAction StxActionTable[] = { new ParserAction(257, 6), new ParserAction(259, 8),
      new ParserAction(260, 10), new ParserAction(262, 14), new ParserAction(263, 15), new ParserAction(264, 16),
      new ParserAction(265, 17), new ParserAction(266, 18), new ParserAction(269, 9), new ParserAction(271, 13),
      new ParserAction(59, 5), new ParserAction(123, 12), new ParserAction(280, 7), new ParserAction(0, 9999),
      new ParserAction(256, 21), new ParserAction(258, 22), new ParserAction(258, 23), new ParserAction(261, 24),
      new ParserAction(258, 26), new ParserAction(261, 29), new ParserAction(61, 31), new ParserAction(256, 32),
      new ParserAction(258, 34), new ParserAction(258, 36), new ParserAction(258, 37), new ParserAction(44, 38),
      new ParserAction(58, 39), new ParserAction(258, 42), new ParserAction(258, 43), new ParserAction(256, 47),
      new ParserAction(258, 46), new ParserAction(58, 48), new ParserAction(258, 49), new ParserAction(44, 50),
      new ParserAction(58, 51), new ParserAction(258, 52), new ParserAction(258, 53), new ParserAction(258, 42),
      new ParserAction(44, 54), new ParserAction(267, 57), new ParserAction(58, 59), new ParserAction(258, 64),
      new ParserAction(268, 65), new ParserAction(61, 66), new ParserAction(258, 67), new ParserAction(258, 68),
      new ParserAction(58, 69), new ParserAction(58, 72), new ParserAction(59, 74), new ParserAction(124, 75),
      new ParserAction(258, 77), new ParserAction(258, 78), new ParserAction(47, 82), new ParserAction(61, 31),
      new ParserAction(258, 83), new ParserAction(59, 84), new ParserAction(124, 75), new ParserAction(258, 86),
      new ParserAction(273, 90), new ParserAction(278, 92), new ParserAction(281, 91), new ParserAction(258, 93),
      new ParserAction(272, 96), new ParserAction(273, 90), new ParserAction(278, 92), new ParserAction(47, 82),
      new ParserAction(281, 91), new ParserAction(275, 97), new ParserAction(276, 98), new ParserAction(277, 99),
      new ParserAction(272, 96), new ParserAction(273, 90), new ParserAction(274, 102), new ParserAction(278, 92),
      new ParserAction(281, 91) };

  private static final ParserGoTo StxGotoTable[] = { new ParserGoTo(-1, 56), new ParserGoTo(40, 55),
      new ParserGoTo(54, 70), new ParserGoTo(-1, 41), new ParserGoTo(-1, 71), new ParserGoTo(-1, 79),
      new ParserGoTo(-1, 89), new ParserGoTo(87, 95), new ParserGoTo(96, 101), new ParserGoTo(100, 95),
      new ParserGoTo(-1, 88), new ParserGoTo(90, 100), new ParserGoTo(-1, 87), new ParserGoTo(2, 20),
      new ParserGoTo(-1, 4), new ParserGoTo(44, 58), new ParserGoTo(-1, 45), new ParserGoTo(62, 76),
      new ParserGoTo(-1, 63), new ParserGoTo(75, 85), new ParserGoTo(-1, 61), new ParserGoTo(-1, 1),
      new ParserGoTo(-1, 2), new ParserGoTo(-1, 19), new ParserGoTo(32, 44), new ParserGoTo(-1, 33),
      new ParserGoTo(-1, 3), new ParserGoTo(-1, 35), new ParserGoTo(-1, 25), new ParserGoTo(-1, 11),
      new ParserGoTo(-1, 27), new ParserGoTo(71, 80), new ParserGoTo(-1, 30), new ParserGoTo(-1, 28),
      new ParserGoTo(-1, 40), new ParserGoTo(87, 94), new ParserGoTo(-1, 81), new ParserGoTo(59, 73),
      new ParserGoTo(-1, 60), new ParserGoTo(-1, 62) };

  private static final Parser StxParsingTable[] = { new Parser(0, -3, 13, 0), new Parser(13, 0, 1, -1),
      new Parser(0, -1, 13, 0), new Parser(14, 0, 1, 1), new Parser(15, -8, 0, -1), new Parser(15, -9, 0, -1),
      new Parser(15, 0, 1, 2), new Parser(16, 0, 1, 2), new Parser(17, 0, 1, 3), new Parser(18, 0, 1, 2),
      new Parser(15, -14, 0, -1), new Parser(19, -54, 1, 3), new Parser(15, -16, 0, -1), new Parser(20, 0, 1, 4),
      new Parser(15, -18, 0, -1), new Parser(15, -19, 0, -1), new Parser(15, -20, 0, -1), new Parser(15, -21, 0, -1),
      new Parser(15, -22, 0, -1), new Parser(21, 0, 1, 1), new Parser(15, -7, 0, -1), new Parser(22, 0, 1, 2),
      new Parser(15, -10, 0, -1), new Parser(15, -11, 0, -1), new Parser(23, 0, 1, 2), new Parser(24, -13, 2, 5),
      new Parser(26, 0, 1, 6), new Parser(15, -15, 0, -1), new Parser(27, 0, 1, 2), new Parser(15, -53, 0, -1),
      new Parser(28, 0, 1, 2), new Parser(15, -52, 0, -1), new Parser(22, 0, 1, 2), new Parser(29, -5, 2, 7),
      new Parser(31, 0, 1, 6), new Parser(32, -12, 2, 5), new Parser(15, -25, 0, -1), new Parser(34, 0, 1, 6),
      new Parser(35, 0, 1, 2), new Parser(36, 0, 1, 2), new Parser(37, -29, 2, 8), new Parser(15, -32, 0, -1),
      new Parser(39, -35, 1, 9), new Parser(15, -17, 0, -1), new Parser(29, -5, 2, 7), new Parser(15, -4, 0, -1),
      new Parser(40, 0, 1, 6), new Parser(15, -6, 0, -1), new Parser(41, -60, 3, 10), new Parser(15, -23, 0, -1),
      new Parser(44, 0, 1, 2), new Parser(45, 0, 1, 2), new Parser(46, 0, 1, 6), new Parser(15, -28, 0, -1),
      new Parser(27, 0, 1, 2), new Parser(15, -31, 0, -1), new Parser(47, -37, 1, 6), new Parser(15, -34, 0, -1),
      new Parser(15, -2, 0, -1), new Parser(41, -60, 3, 10), new Parser(48, 0, 2, 11), new Parser(15, -58, 0, -1),
      new Parser(41, -59, 3, 12), new Parser(15, -62, 0, -1), new Parser(15, -63, 0, -1), new Parser(50, 0, 1, 2),
      new Parser(15, -65, 0, -1), new Parser(15, -24, 0, -1), new Parser(15, -26, 0, -1), new Parser(51, 0, 1, 2),
      new Parser(15, -30, 0, -1), new Parser(52, -40, 2, 13), new Parser(54, 0, 1, 2), new Parser(55, 0, 2, 11),
      new Parser(15, -56, 0, -1), new Parser(41, -60, 3, 12), new Parser(15, -61, 0, -1), new Parser(15, -64, 0, -1),
      new Parser(15, -27, 0, -1), new Parser(15, -33, 0, -1), new Parser(57, 0, 1, 2), new Parser(58, 0, 3, 14),
      new Parser(61, 0, 1, 2), new Parser(15, -36, 0, -1), new Parser(15, -55, 0, -1), new Parser(15, -57, 0, -1),
      new Parser(15, -38, 0, -1), new Parser(62, 0, 5, 14), new Parser(15, -44, 0, -1), new Parser(67, -48, 3, 15),
      new Parser(58, 0, 3, 14), new Parser(15, -50, 0, -1), new Parser(15, -51, 0, -1), new Parser(15, -41, 0, -1),
      new Parser(15, -39, 0, -1), new Parser(15, -42, 0, -1), new Parser(58, 0, 3, 14), new Parser(15, -45, 0, -1),
      new Parser(15, -46, 0, -1), new Parser(15, -47, 0, -1), new Parser(70, 0, 5, 14), new Parser(15, -43, 0, -1),
      new Parser(15, -49, 0, -1) };

  private static final String StxErrorTable[] = { "Expecting a declaration", "'%%' expected", "Token expected",
      "Type definition expected", "= expected", "Token or Comma expected", "Colon expected", "Expecting code section",
      "Expecting a token definition", "Number expected", "Token, '%prec' or = expected",
      "Semicolon or Rule separator ('|') expected", "Expecting token, precedence declaration or '='",
      "Regular expression marker ('/') or = expected", "Expecting basic element", "'*', '+' or '?' expected" };

  private static final Grammar StxGrammarTable[] = { new Grammar(-1, 1), new Grammar(23, 0), new Grammar(21, 5),
      new Grammar(26, 0), new Grammar(21, 4), new Grammar(15, 0), new Grammar(15, 1), new Grammar(22, 2),
      new Grammar(22, 1), new Grammar(13, 1), new Grammar(13, 2), new Grammar(13, 2), new Grammar(13, 3),
      new Grammar(13, 2), new Grammar(13, 1), new Grammar(13, 2), new Grammar(13, 1), new Grammar(13, 3),
      new Grammar(29, 1), new Grammar(29, 1), new Grammar(29, 1), new Grammar(29, 1), new Grammar(29, 1),
      new Grammar(27, 2), new Grammar(27, 3), new Grammar(27, 1), new Grammar(28, 4), new Grammar(28, 5),
      new Grammar(28, 3), new Grammar(30, 2), new Grammar(34, 3), new Grammar(34, 2), new Grammar(34, 1),
      new Grammar(1, 4), new Grammar(0, 1), new Grammar(0, 0), new Grammar(4, 2), new Grammar(4, 0), new Grammar(5, 2),
      new Grammar(5, 3), new Grammar(5, 0), new Grammar(35, 2), new Grammar(11, 2), new Grammar(11, 3),
      new Grammar(11, 1), new Grammar(7, 2), new Grammar(7, 2), new Grammar(7, 2), new Grammar(7, 1),
      new Grammar(6, 3), new Grammar(6, 1), new Grammar(6, 1), new Grammar(31, 1), new Grammar(33, 1),
      new Grammar(33, 0), new Grammar(24, 5), new Grammar(24, 4), new Grammar(37, 3), new Grammar(37, 1),
      new Grammar(19, 1), new Grammar(19, 0), new Grammar(39, 2), new Grammar(39, 1), new Grammar(17, 1),
      new Grammar(17, 2), new Grammar(17, 1) };

  private static final int StxRecoverTable[] = { 59 };

  @SuppressWarnings("unused")
  private static final int StxNonTerminals[] = { 0, 1, 4, 5, 6, 7, 11, 13, 15, 17, 19, 21, 22, 23, 24, 26, 27, 28, 29,
      30, 31, 33, 34, 35, 37, 39, -1 };

  private static int MIN_STACK = 150;
  private static int INCR_STACK = 150;

  private StackElement StxValue = null;
  private int sStxStack[] = new int[MIN_STACK];
  private int pStxStack = 0;
  private StackElement[] StxStack = new StackElement[MIN_STACK];
  private int StxSym = 0;
  private int StxState = 0;
  private int StxErrors = 0;
  private int StxErrorFlag = 0;
  private boolean bActionDone = false;
  private char currentCharacter;
  private int currentRuleIndex;
  private List<RuleItem> currentRuleItems = null;
  private String currentStringValue;
  private Type currentType;

  private int lineNumber = 0;
  private int markers = 0;
  private boolean isCurlyBrace;
  private boolean isEqual;
  private Stack<Character> inputChars = new Stack<Character>();
  private boolean isError;
  private boolean isRegex;
  private int tokenNumber;
  private String currentNonTerminalName;
  private boolean mustClose;
  private boolean finalActions;
  private boolean isErrorToken;
  private Associativity ruleAssociativity;
  private int rulePrecedence;
  private int tokenActionCount;
  private int ruleActionCount;
  private int actLine;
  private boolean isFirstToken = true;
  private int numberOfRecoveries;

  public CodeParser(Environment environment, RuntimeData runtimeData) {
    super();
    this.environment = environment;
    this.runtimeData = runtimeData;
  }

  /*
   * This routine maps a state and a token to a new state on the action table
   */
  private int StxAction(int state, int sym) {
    int position = StxParsingTable[state].position;
    int i;

    /* Look in actions if there is a transaction with the token */
    for (i = 0; i < StxParsingTable[state].elements; i++) {
      if (StxActionTable[position + i].symbol == sym) {
        return StxActionTable[position + i].state;
      }
    }
    /* otherwise */
    return StxParsingTable[state].defa;
  }

  /*
   * This routine maps a origin state to a destination state using the symbol
   * position
   */
  int StxGoto(int state, int position) {
    /* Search in gotos if there is a state transition */
    for (; StxGotoTable[position].origin != -1; position++) {
      if (StxGotoTable[position].origin == state) {
        return StxGotoTable[position].destination;
      }
    }
    /* default */
    return StxGotoTable[position].destination;
  }

  /*
   * This routine prints the contents of the parsing stack
   */

  private void PrintStack() {
    if (!environment.isDebug()) {
      return;
    }
    System.out.printf("  Stack pointer = %d\n", pStxStack);
    System.out.printf("  States: [");
    for (int i = 0; i <= pStxStack; i++) {
      System.out.printf(" %d %s", sStxStack[i], "{" + (StxStack[i] != null ? StxStack[i].toString() : "") + "}");
    }
    System.out.printf("]\n");
  }

  /*
   * Does a shift operation. Puts a new state on the top of the stack
   */
  int StxShift(int sym, int state) {
    ++pStxStack;
    if (pStxStack >= StxStack.length) {
      Arrays.copyOf(StxStack, StxStack.length + INCR_STACK);
    }
    sStxStack[pStxStack] = state;
    StxStack[pStxStack] = StxValue;
    StxState = state;
    if (environment.isDebug()) {
      System.out.printf("Shift to %d with %d\n", StxState, sym);
    }
    PrintStack();
    return 1;
  }

  /*
   * Recognizes a rule an removes all its elements from the stack
   */
  int StxReduce(int sym, int rule) throws IOException {
    if (environment.isDebug()) {
      System.out.printf("Reduce on rule %d with symbol %s(%d)\n", rule,
          (sym >= 256 ? tokenNames[sym - 256] : "\"" + Character.toString((char) sym) + "\""), sym);
    }
    if (!StxCode(rule)) {
      return 0;
    }
    pStxStack -= StxGrammarTable[rule].reductions;
    sStxStack[pStxStack + 1] = StxGoto(sStxStack[pStxStack], StxGrammarTable[rule].symbol);
    StxState = sStxStack[++pStxStack];
    if (environment.isDebug()) {
      PrintStack();
    }
    return 1;
  }

  private boolean StxCode(int rule) throws IOException {
    int i;
    switch (rule) {

      case 1:
        tokenEndAction();
        break;
      case 3:
        tokenEndAction();
        break;
      case 5:
        {
          if (!ruleEndAction()) {
            return false;
          }

          finalActions = false;
        }
        break;
      case 6:
        {
          if (!ruleEndAction()) {
            return false;
          }
          finalActions = true;
        }
        break;
      case 10:
        {
          if (runtimeData.getStart() != null) {
            environment.error(-1, "Distinguished symbol \'%s\' declared more than once.", runtimeData.getStart()
                .getName());
            return false;
          }
          Terminal terminal = runtimeData.findTerminalByName(StxStack[pStxStack].id);
          if (terminal == null) {
            NonTerminal nonTerminal = runtimeData.findNonTerminalByName(StxStack[pStxStack].id);
            if (nonTerminal == null) {
              nonTerminal = new NonTerminal(StxStack[pStxStack].id);
              runtimeData.getNonTerminals().add(nonTerminal);
            }
            nonTerminal.setCount(nonTerminal.getCount() - 1);
            runtimeData.setStart(nonTerminal);
          } else {
            environment.error(-1, "Distinguished symbol \'%s\' previously declared as token.", StxStack[pStxStack].id);
            return false;
          }
        }
        break;
      case 14:
        if (!declareUnion()) {
          return false;
        }
        break;
      case 15:
        currentType = null;
        break;
      case 16:
        if (!declareAction()) {
          return false;
        }
        break;
      case 18:
        {
          ruleAssociativity = Associativity.LEFT;
          isErrorToken = false;
        }
        break;
      case 19:
        {
          rulePrecedence++;
          ruleAssociativity = Associativity.LEFT;
          isErrorToken = false;
        }
        break;
      case 20:
        {
          rulePrecedence++;
          ruleAssociativity = Associativity.RIGHT;
          isErrorToken = false;
        }
        break;
      case 21:
        {
          rulePrecedence++;
          ruleAssociativity = Associativity.BINARY;
          isErrorToken = false;
        }
        break;
      case 22:
        {
          ruleAssociativity = Associativity.NONE;
          isErrorToken = true;
        }
        break;
      case 23:
        return declareOneNonTerminal(StxStack[pStxStack - 2].id, StxStack[pStxStack].id);
      case 24:
        return declareOneNonTerminal(StxStack[pStxStack - 3].id, StxStack[pStxStack].id);
      case 25:
        return declareOneNonTerminal(StxStack[pStxStack - 1].id, StxStack[pStxStack].id);
      case 26:
        return nameOneNonTerminal(StxStack[pStxStack - 2].id, StxStack[pStxStack].id);
      case 27:
        return nameOneNonTerminal(StxStack[pStxStack - 2].id, StxStack[pStxStack].id);
      case 28:
        return nameOneNonTerminal(StxStack[pStxStack - 2].id, StxStack[pStxStack].id);
      case 33:
        {
          if (StxStack[pStxStack - 2].value != -1) {
            StxStack[pStxStack - 3].value = StxStack[pStxStack - 2].value;
          }
          Terminal terminal = runtimeData.findTerminalByName(StxStack[pStxStack - 3].id);
          if (terminal == null) {
            terminal = isErrorToken ? new ErrorToken(StxStack[pStxStack - 3].id) : new Terminal(
                StxStack[pStxStack - 3].id);
            runtimeData.getTerminals().add(terminal);
          }
          terminal.setCount(terminal.getCount() - 1);
          if (ruleAssociativity != Associativity.NONE) {
            if (terminal.getAssociativity() != Associativity.NONE) {
              environment.error(-1, "Reassigning precedence/associativity for token \'%s\'.", terminal.getName());
              return false;
            }
            terminal.setPrecedence(rulePrecedence);
            terminal.setAssociativity(ruleAssociativity);
          }
          if (currentType != null) {
            terminal.setType(currentType);
          }
          if (StxStack[pStxStack - 3].value >= 0) {
            if (terminal.getToken() != -1) {
              environment.error(-1, "Warning: Token \'%s\' already has a value.", terminal.getName());
            }
            for (Terminal cual : runtimeData.getTerminals()) {
              if (cual != terminal && cual.getToken() == StxStack[pStxStack - 3].value) {
                environment.error(-1, "Warning: Token number %d already used on token \'%s\'.",
                    StxStack[pStxStack - 3].value, cual.getName());
                return false;
              }
            }
            terminal.setToken(StxStack[pStxStack - 3].value);
          }
          if (StxStack[pStxStack - 1].id != "") {
            terminal.setFullName(StxStack[pStxStack - 1].id);
          }
          if (StxStack[pStxStack].regex != null) {
            // SetEndToken(StxStack[pStxStack].regex, terminal.getName());
          }
        }
        break;
      case 35:
        StxStack[pStxStack + 1].value = -1;
        break;
      case 36:
        StxStack[pStxStack - 1].id = StxStack[pStxStack].id;
        break;
      case 37:
        StxStack[pStxStack + 1].id = "";
        break;
      case 38:
        StxStack[pStxStack - 1].regex = null;
        break;
      case 39:
        // StxStack[pStxStack - 2].regex = AddTree(StxStack[pStxStack -
        // 1].regex);
        break;
      case 40:
        StxStack[pStxStack + 1].regex = null;
        break;
      case 42:
        // StxStack[pStxStack - 1].regex = SequentialNode(StxStack[pStxStack -
        // 1].regex,
        // StxStack[pStxStack].regex);
        break;
      case 43:
        // StxStack[pStxStack - 2].regex = AlternateNode(StxStack[pStxStack -
        // 2].regex,
        // StxStack[pStxStack].regex);
        break;
      case 45:
        // StxStack[pStxStack - 1].regex = ZeroOrManyNode(StxStack[pStxStack -
        // 1].regex);
        break;
      case 46:
        // StxStack[pStxStack - 1].regex = OneOrManyNode(StxStack[pStxStack -
        // 1].regex);
        break;
      case 47:
        // StxStack[pStxStack - 1].regex = ZeroOrOneNode(StxStack[pStxStack -
        // 1].regex);
        break;
      case 49:
        StxStack[pStxStack - 2].regex = StxStack[pStxStack - 1].regex;
        break;
      case 50:
        // CharNode(StxStack[pStxStack].regex);
        break;
      case 51:
        // AnyNode();
        break;
      case 52:
        tokenAction();
        break;
      case 53:
        currentType = new Type(StxStack[pStxStack].id);
        runtimeData.getTypes().add(currentType);
        break;
      case 54:
        currentType = null;
        break;
      case 55:
        return declareRules(StxStack[pStxStack - 3].id);
      case 56:
        return declareRules(StxStack[pStxStack - 3].id);
      case 57:
        newRule();
        bActionDone = false;
        break;
      case 58:
        {
          newRule();
          currentRuleIndex = runtimeData.getRules().size() - 1;
          bActionDone = false;
        }
        break;
      case 60:
        {
          bActionDone = false;
        }
        break;
      case 63:
        {
          if (StxStack[pStxStack].id.length() == 0) {
            break;
          }
          if (isFirstToken) {
            rulePrecedence = 0;
            ruleAssociativity = Associativity.NONE;
            isFirstToken = false;
          }
          if (bActionDone) {
            Rule stx = newEmptyRule();
            String rootName = "Sys$Prod" + (runtimeData.getRules().size() - 1);
            NonTerminal rootSymbol = new NonTerminal(rootName);
            runtimeData.getNonTerminals().add(rootSymbol);
            stx.setLeftHand(rootSymbol);
            rootSymbol.setCount(rootSymbol.getCount() + 1);
            rootSymbol.setPrecedence(1); /* usado como no terminal */
            RuleItem item = newItem(rootSymbol);
            stx.getItems().add(item);
            bActionDone = false;
          }
          Symbol symbol;
          NonTerminal nonTerminal = runtimeData.findNonTerminalByName(StxStack[pStxStack].id);
          if (nonTerminal == null) {
            Terminal terminal = runtimeData.findTerminalByName(StxStack[pStxStack].id);
            if (terminal != null) {
              rulePrecedence = terminal.getPrecedence();
              ruleAssociativity = terminal.getAssociativity();
              symbol = terminal;
            } else {
              if (StxStack[pStxStack].mustClose && StxStack[pStxStack].value >= 0) {
                terminal = new Terminal(StxStack[pStxStack].id);
                runtimeData.getTerminals().add(terminal);
                if (StxStack[pStxStack].value >= 0) {
                  for (Terminal cual : runtimeData.getTerminals()) {
                    if (cual != terminal && cual.getToken() == StxStack[pStxStack].value) {
                      environment.error(-1, "Warning: Token number %d already used on token \'%s\'.",
                          StxStack[pStxStack].value, cual.getName());
                      return false;
                    }
                  }
                  terminal.setToken(StxStack[pStxStack].value);
                }
                symbol = terminal;
              } else {
                nonTerminal = new NonTerminal(StxStack[pStxStack].id);
                runtimeData.getNonTerminals().add(nonTerminal);
                nonTerminal.setCount(nonTerminal.getCount() + 1);
                symbol = nonTerminal;
              }
            }
          } else {
            symbol = nonTerminal;
          }
          newItem(symbol);

        }
        break;
      case 64:
        {
          NonTerminal nonTerminal = runtimeData.findNonTerminalByName(StxStack[pStxStack].id);
          if (nonTerminal == null) {
            Terminal terminal = runtimeData.findTerminalByName(StxStack[pStxStack].id);
            if (terminal == null) {
              environment.error(-1, "Warning: token \'%s\' not declared.", StxStack[pStxStack].id);
              return false;
            } else {
              rulePrecedence = terminal.getPrecedence();
              ruleAssociativity = terminal.getAssociativity();
            }
          } else {
            environment.error(-1, "Warning: token \'%s\' not declared.", StxStack[pStxStack].id);
            return false;
          }
        }
        break;
      case 65:
        {
          i = currentRuleItems != null ? currentRuleItems.size() : 0;
          if (!ruleAction(runtimeData.getRules().size(), i, currentNonTerminalName)) {
            return false;
          }
          bActionDone = true;
        }
        break;

    }/* End of switch */
    return true; /* OK */
  }

  /*
   * Recover from a syntax error removing stack states/symbols, and removing
   * input tokens. The array StxRecover contains the tokens that bound the error
   */
  int StxRecover() throws IOException {
    int i, acc;

    switch (StxErrorFlag) {
      case 0: /* 1st error */
        if (StxError(StxState, StxSym, pStxStack) == 0) {
          return 0;
        }
        StxErrors++;
        /* goes into 1 and 2 */

      case 1:
      case 2: /* three attempts are made before dropping the current token */
        StxErrorFlag = 3; /* Remove token */

        while (pStxStack >= 0) {
          /*
           * Look if the state on the stack's top has a transition with one of
           * the recovering elements in StxRecoverTable
           */
          for (i = 0; i < StxRecoverTable.length; i++) {
            if ((acc = StxAction(StxState, StxRecoverTable[i])) > 0) {
              /* valid shift */
              return StxShift(StxRecoverTable[i], acc);
            }
          }
          if (environment.isDebug()) {
            System.out.printf("Recuperate removing state %d and go to state %d\n", StxState, sStxStack[pStxStack - 1]);
          }
          StxState = sStxStack[--pStxStack];
        }
        pStxStack = 0;
        return 0;

      case 3: /* I need to drop the current token */
        if (environment.isDebug()) {
          System.out.printf("Recuperate removing symbol %d\n", StxSym);
        }
        if (StxSym == 0) {
          return 0;
        }
        StxSym = StxScan();
        return 1;
    }
    return 0;
  }

  /*
   * Main parser routine, uses Shift, Reduce and Recover
   */
  private boolean StxParse() throws IOException {
    int action;

    pStxStack = 0;
    sStxStack[0] = 0;
    StxSym = StxScan();
    StxState = 0;
    StxErrorFlag = 0;
    StxErrors = 0;

    while (2 > 1) {
      action = StxAction(StxState, StxSym);
      if (action == 9999) {
        if (environment.isDebug()) {
          System.out.printf("Program Accepted\n");
        }
        return true;
      }

      if (action > 0) {
        if (StxShift(StxSym, action) == 0) {
          return false;
        }
        StxSym = StxScan();
        if (StxErrorFlag > 0) {
          StxErrorFlag--; /* properly recovering from error */
        }
      } else if (action < 0) {
        if (StxReduce(StxSym, -action) == 0) {
          if (StxErrorFlag == -1) {
            if (StxRecover() == 0) {
              return false;
            }
          } else {
            return false;
          }
        }
      } else if (action == 0) {
        if (StxRecover() == 0) {
          return false;
        }
      }
    }
  }

  private boolean declareOneNonTerminal(String idnt, String tkn) {
    if (runtimeData.findTerminalByName(tkn) != null) {
      environment.error(-1, "Token \'%s\' cannot appear on a %%type clause.", tkn);
      return false;
    }
    NonTerminal nonTerminal = runtimeData.findNonTerminalByName(tkn);
    if (nonTerminal == null) {
      nonTerminal = new NonTerminal(tkn);
      runtimeData.getNonTerminals().add(nonTerminal);
    } else {
      nonTerminal.setCount(nonTerminal.getCount() - 1);
    }
    Type type = new Type(idnt);
    runtimeData.getTypes().add(type);
    nonTerminal.setType(type);
    return true;
  }

  private boolean nameOneNonTerminal(String ntr, String name) {
    if (runtimeData.findTerminalByName(ntr) != null) {
      environment.error(-1, "Token \'%s\' cannot appear on a %%name clause.", ntr);
      return false;
    }
    NonTerminal nonTerminal = runtimeData.findNonTerminalByName(ntr);
    if (nonTerminal == null) {
      runtimeData.getNonTerminals().add(new NonTerminal(ntr));
    } else {
      nonTerminal.setCount(nonTerminal.getCount() - 1);
    }
    nonTerminal.setFullName(name);
    return true;
  }

  private boolean declareRules(String name) {
    if (runtimeData.findTerminalByName(name) != null) {
      environment.error(-1, "The token \'%s\' cannot appear to the right of a rule.", name);
      return false;
    }
    NonTerminal nonTerminal = runtimeData.findNonTerminalByName(name);
    if (nonTerminal == null) {
      runtimeData.getNonTerminals().add(new NonTerminal(name));
    } else {
      nonTerminal.setCount(nonTerminal.getCount() - 1);
    }
    nonTerminal.setPrecedence(1); /* usado como no terminal */
    for (int i = currentRuleIndex; i < runtimeData.getRules().size(); i++) {
      Rule rule = runtimeData.getRules().get(i);
      if (rule.getLeftHand() == null) {
        rule.setLeftHand(nonTerminal);
      }
    }
    bActionDone = false;
    return true;
  }

  private char getCharacter() throws IOException {
    if (inputChars.size() > 0) {
      return inputChars.pop();
    }

    currentCharacter = (char) environment.source.read();
    if (currentCharacter == -1) {
      return 0;
    }

    if (currentCharacter == '\n') {
      lineNumber++;
      // if (environment.isVerbose()) {
      // System.out.printf("Lines : %05d\r", lineNumber);
      // }
    }

    if (currentCharacter == 26) {
      return 0;
    }

    return currentCharacter;
  }

  private void ungetCharacter(char c) {
    inputChars.push(c);
  }

  private char decodeOctal() throws IOException {
    int iCount = 3;
    char c2 = 0;

    while (iCount != 0) {
      c2 *= 8;

      if (currentCharacter >= '0' && currentCharacter <= '7') {
        c2 += currentCharacter - '0';
        currentCharacter = getCharacter();
      } else if (currentCharacter == '\0') {
        return c2;
      } else {
        break;
      }

      iCount--;
    }

    return c2;
  }

  private char decodeControlChar() throws IOException {
    char c2;
    currentCharacter = getCharacter();

    if (currentCharacter == '\0') {
      return '\0';
    }

    if (currentCharacter >= 'a' && currentCharacter <= 'z') {
      c2 = currentCharacter;
      currentCharacter = getCharacter();
      return (char) (c2 - ('a' - 1));
    } else if (currentCharacter >= 'A' && currentCharacter <= 'Z') {
      c2 = currentCharacter;
      currentCharacter = getCharacter();
      return (char) (c2 - ('A' - 1));
    } else {
      return 'c' - 'a';
    }
  }

  private char decodeHex() throws IOException {
    int iCount = 2;
    char c2 = 0;

    currentCharacter = getCharacter();

    while (iCount != 0) {
      c2 *= 16;

      if (currentCharacter >= '0' && currentCharacter <= '9') {
        c2 += currentCharacter - '0';
      } else if (currentCharacter >= 'a' && currentCharacter <= 'f') {
        c2 += 10 + (currentCharacter - 'a');
      } else if (currentCharacter >= 'A' && currentCharacter <= 'F') {
        c2 += 10 + (currentCharacter - 'A');
      } else if (currentCharacter == '\0') {
        return '\0';
      } else {
        return 'x' - 'a';
      }

      iCount--;
    }

    return c2;
  }

  private char decodeEscape() throws IOException {
    char c2;
    switch (currentCharacter) {
      case '0':
      case '1':
      case '2':
      case '3':
      case '4':
      case '5':
      case '6':
      case '7':
        return decodeOctal();
      case 'a':
        currentCharacter = getCharacter();
        return 7;
      case 'b':
        currentCharacter = getCharacter();
        return '\b';
      case 'c':
        currentCharacter = getCharacter();
        return decodeControlChar();
      case 'e':
        currentCharacter = getCharacter();
        return '\\';
      case 'f':
        currentCharacter = getCharacter();
        return '\f';
      case 'n':
        currentCharacter = getCharacter();
        return '\n';
      case 'r':
        currentCharacter = getCharacter();
        return '\r';
      case 't':
        currentCharacter = getCharacter();
        return '\t';
      case 'v':
        currentCharacter = getCharacter();
        return 11;
      case 'x':
        currentCharacter = getCharacter();
        return decodeHex();
      default:
        c2 = currentCharacter;
        currentCharacter = getCharacter();
        return c2;
    }
  }

  private int getRegexSymbol() throws IOException {
    char c2;

    if (isEqual) {
      isEqual = false;
      currentStringValue = "";
      return TOK_TOKEN;
    }

    if (currentCharacter == '|') {
      currentCharacter = getCharacter();
      return TOK_RX_PIPE;
    }
    if (currentCharacter == '(') {
      currentCharacter = getCharacter();
      return TOK_RX_LPAR;
    }
    if (currentCharacter == ')') {
      currentCharacter = getCharacter();
      return TOK_RX_RPAR;
    }
    if (currentCharacter == '*') {
      currentCharacter = getCharacter();
      return TOK_RX_STAR;
    }
    if (currentCharacter == '+') {
      currentCharacter = getCharacter();
      return TOK_RX_PLUS;
    }
    if (currentCharacter == '?') {
      currentCharacter = getCharacter();
      return TOK_RX_HUH;
    }
    if (currentCharacter == '.') {
      currentCharacter = getCharacter();
      return TOK_RX_ANY;
    }
    if (currentCharacter == '/') {
      isRegex = false;
      isEqual = true;
      currentCharacter = getCharacter();
      return '/';
    }

    if (currentCharacter == '\\') {
      currentCharacter = getCharacter();
      c2 = decodeEscape();
      if (c2 == 0) {
        return '\0';
      }
    }
    /*
     * StxValue.node = malloc(sizeof(REGEXNODE)); StxValue.node.parent = NULL;
     * StxValue.node.child1 = NULL; StxValue.node.child2 = NULL;
     * StxValue.node.nodeType = NODE_LEX; StxValue.node.ranges =
     * malloc(sizeof(REGEXRANGE)); StxValue.node.ranges.next = NULL;
     * StxValue.node.ranges.prev = NULL; StxValue.node.ranges.charStart = c2;
     * StxValue.node.ranges.charEnd = c2;
     */
    return TOK_CHARS;
  }

  private int getNormalSymbol() throws IOException {
    char c2;
    String s2;
    boolean end;

    s2 = currentStringValue;
    currentStringValue = "";

    if (markers >= 2) {
      return 0;
    }

    if (isCurlyBrace) {
      isCurlyBrace = false;
      return ';';
    }

    if (isEqual) {
      isEqual = false;
      currentStringValue = "";
      return TOK_TOKEN;
    }

    while (2 > 1) {
      while (Character.isWhitespace(currentCharacter)) {
        currentCharacter = getCharacter();
      }
      if (currentCharacter == '/') {
        if ((currentCharacter = getCharacter()) == '*') {
          currentCharacter = getCharacter();
          end = false;
          while (!end) {
            while (currentCharacter == '*') {
              if ((currentCharacter = getCharacter()) == '/') {
                end = true;
              }
            }
            currentCharacter = getCharacter();
          }
        } else {
          ungetCharacter(currentCharacter);
          currentCharacter = '/';
          break;
        }
      } else {
        break;
      }
    }

    if (currentCharacter == '\0') {
      return 0;
    }

    if (currentCharacter == '%' || currentCharacter == '\\') {
      currentCharacter = getCharacter();
      switch (currentCharacter) {
        case '0':
          currentCharacter = getCharacter();
          return TOK_TERM;
        case '<':
          currentCharacter = getCharacter();
          return TOK_LEFT;
        case '2':
          currentCharacter = getCharacter();
          return TOK_BINARY;
        case '>':
          currentCharacter = getCharacter();
          return TOK_RIGHT;
        case '%':
        case '\\':
          currentCharacter = getCharacter();
          markers++;
          return TOK_MARCA;
        case '=':
          currentCharacter = getCharacter();
          return TOK_PREC;
        case '@':
          currentCharacter = getCharacter();
          return TOK_NAME;
        case '{':
          currentCharacter = getCharacter();
          isCurlyBrace = true;
          return '{';
        case '!':
          currentCharacter = getCharacter();
          return TOK_ERRDEF;
      }
      while (Character.isLetterOrDigit(currentCharacter)) {
        currentStringValue += currentCharacter;
        currentCharacter = getCharacter();
      }
      for (ReservedWord rw : RWord) {
        if (currentStringValue.equals(rw.getWord())) {
          if (rw.getToken() == TOK_UNION) {
            isCurlyBrace = true;
          }
          return rw.getToken();
        }
      }
      isError = true;
      environment.error(-1, "Reserved word \'%s\' is incorrect.", currentStringValue);
      return TOK_ERROR;
    }

    if (currentCharacter == ';') {
      currentCharacter = getCharacter();
      return ';';
    }

    if (currentCharacter == ',') {
      currentCharacter = getCharacter();
      return ',';
    }

    if (currentCharacter == ':') {
      currentNonTerminalName = s2;
      currentCharacter = getCharacter();
      return ':';
    }

    if (currentCharacter == '|') {
      currentCharacter = getCharacter();
      return '|';
    }

    if (currentCharacter == '=') {
      currentCharacter = getCharacter();
      isEqual = true;
      return '=';
    }

    if (currentCharacter == '{') {
      isEqual = true;
      return '=';
    }

    if (currentCharacter == '<') {
      currentCharacter = getCharacter();
      currentStringValue = "";
      while (currentCharacter != '\0' && currentCharacter != '>' && currentCharacter != '\n') {
        currentStringValue += currentCharacter;
        currentCharacter = getCharacter();
      }
      if (currentCharacter != '>') {
        isError = true;
        environment.error(-1, "Statement < .. > not ended.");
        return TOK_ERROR;
      }
      currentCharacter = getCharacter();
      return TOK_TYPENAME;
    }

    if (currentCharacter == '/') {
      isRegex = true;
      isEqual = true;
      currentCharacter = getCharacter();
      return '/';
    }

    if (Character.isDigit(currentCharacter)) {
      currentStringValue = "";
      while (Character.isDigit(currentCharacter)) {
        currentStringValue += currentCharacter;
        currentCharacter = getCharacter();
      }
      tokenNumber = Integer.parseInt(currentStringValue);
      return TOK_NUM;
    }

    mustClose = false;
    if (currentCharacter == '\'' || currentCharacter == '"') {
      c2 = currentCharacter;
      mustClose = true;
      currentCharacter = getCharacter();
    } else {
      c2 = ':';
    }

    currentStringValue = "";
    do { /* TOKEN */
      currentStringValue += currentCharacter;
      currentCharacter = getCharacter();
      if (currentCharacter == '\0') {
        break;
      }
      if (!mustClose && "%\\;,:|={< \r\t\n".indexOf(currentCharacter) >= 0) {
        break;
      }
    } while (currentCharacter != c2);

    if (mustClose && currentCharacter != c2) {
      isError = true;
      environment.error(-1, "Statement ' .. ' or \" .. \" not ended.");
      return TOK_ERROR;
    }
    tokenNumber = -1;
    if (currentStringValue.equals("\\a")) {
      tokenNumber = 7;
    } else if (currentStringValue.equals("\\b")) {
      tokenNumber = '\b';
    } else if (currentStringValue.equals("\\n")) {
      tokenNumber = '\n';
    } else if (currentStringValue.equals("\\t")) {
      tokenNumber = '\t';
    } else if (currentStringValue.equals("\\f")) {
      tokenNumber = '\f';
    } else if (currentStringValue.equals("\\r")) {
      tokenNumber = '\r';
    } else if (currentStringValue.length() >= 2 && currentStringValue.substring(0, 2).equals("\\x")) {
      int p = 2;
      tokenNumber = 0;
      while (2 > 1) {
        if (currentStringValue.charAt(p) >= '0' && currentStringValue.charAt(p) <= '9') {
          tokenNumber = tokenNumber * 16 + currentStringValue.charAt(p++) - '0';
        } else if (currentStringValue.charAt(p) >= 'A' && currentStringValue.charAt(p) <= 'F') {
          tokenNumber = tokenNumber * 16 + currentStringValue.charAt(p++) - 'A' + 10;
        } else if (currentStringValue.charAt(p) >= 'a' && currentStringValue.charAt(p) <= 'f') {
          tokenNumber = tokenNumber * 16 + currentStringValue.charAt(p++) - 'a' + 10;
        } else {
          break;
        }
      }
    } else if (currentStringValue.length() >= 2 && currentStringValue.substring(0, 2).equals("\\0")) {
      int p = 2;
      tokenNumber = 0;
      while (currentStringValue.charAt(p) >= '0' && currentStringValue.charAt(p) <= '7') {
        tokenNumber = tokenNumber * 8 + currentStringValue.charAt(p++) - '0';
      }
    }

    if (mustClose) {
      currentCharacter = getCharacter();
      if (currentStringValue.length() == 1) {
        tokenNumber = currentStringValue.charAt(0);
      }
    }

    return TOK_TOKEN;
  }

  /*
   * int GetRegexSym() { char c2;
   * 
   * if(bEqual){ bEqual = 0; s[0] = 0; return TOK_TOKEN; }
   * 
   * if (c == '|') {c = GetCar(); return TOK_RX_PIPE;} if (c == '(') {c =
   * GetCar(); return TOK_RX_LPAR;} if (c == ')') {c = GetCar(); return
   * TOK_RX_RPAR;} if (c == '*') {c = GetCar(); return TOK_RX_STAR;} if (c ==
   * '+') {c = GetCar(); return TOK_RX_PLUS;} if (c == '?') {c = GetCar();
   * return TOK_RX_HUH;} if (c == '.') {c = GetCar(); return TOK_RX_ANY;} if (c
   * == '/') { bRegEx = 0; bEqual = 1; c = GetCar(); return '/'; }
   * 
   * if (c == '\\') { c = GetCar(); c2 = DecodeEscape(); if (c2 == 0) { return
   * EOS; } StxValue.regex = malloc(sizeof(REGEXNODE)); StxValue.regex.parent =
   * null; StxValue.regex.child1 = null; StxValue.regex.child2 = null;
   * StxValue.regex.regexType = NODE_LEX; StxValue.regex.ranges =
   * malloc(sizeof(REGEXRANGE)); StxValue.regex.ranges.next = null;
   * StxValue.regex.ranges.prev = null; StxValue.regex.ranges.charStart = c2;
   * StxValue.regex.ranges.charEnd = c2; return TOK_CHARS; }
   * 
   * if (c != '\0') { StxValue.regex = malloc(sizeof(REGEXNODE));
   * StxValue.regex.parent = null; StxValue.regex.child1 = null;
   * StxValue.regex.child2 = null; StxValue.regex.regexType = NODE_LEX;
   * StxValue.regex.ranges = malloc(sizeof(REGEXRANGE));
   * StxValue.regex.ranges.next = null; StxValue.regex.ranges.prev = null;
   * StxValue.regex.ranges.charStart = c; StxValue.regex.ranges.charEnd = c; c =
   * GetCar(); return TOK_CHARS; }
   * 
   * return EOS; }
   */

  int StxScan() throws IOException {
    int rc;

    if (isRegex) {
      rc = getRegexSymbol();
      if (environment.isVerbose()) {
        System.out.printf("RegexScanner: %d\n", rc);
      }
    } else {
      rc = getNormalSymbol();
      StxValue = new StackElement(-1, tokenNumber, mustClose, currentStringValue, null);
      if (environment.isDebug()) {
        System.out.printf("* StdScanner: %s(%d) {%s}\n",
            (rc >= 256 ? tokenNames[rc - 256] : "\"" + Character.toString((char) rc) + "\""), rc,
            StxValue != null ? StxValue.toString() : "");
      }
    }
    return rc;
  }

  int StxError(int StxState, int StxSym, int pStxStack) {
    int msg = StxParsingTable[StxState].msg;
    if (msg >= 0) {
      environment.error(-1, "Syntax error %d :\'%s\'.", StxState, StxErrorTable[msg]);
    } else {
      System.err.printf("%s(%05d) : Unknown error on state %d\n", environment.getSourceFile().toString(),
          lineNumber + 1, StxState);
    }
    isError = true;
    return 0; /*
               * with actions, it recovers weird. Need to change the action
               * stuff to the scanner
               */
  }

  private boolean ruleAction(int regla, int elems, String id) throws IOException {
    int nBracks = 0;
    boolean end = false;
    boolean bBreak;
    int num;
    int base;
    char characterToFind;
    Type type;
    String s2;
    int sign;
    int i;
    String stackExpression;
    ;

    if (ruleActionCount == 0) {
      /* header */
      switch (environment.getLanguage()) {
        case C:
          environment.output.printf("\n"
                                    + "/* Code Generator */\n"
                                      + "\n"
                                      + "#ifndef TSTACK\n"
                                      + "#define TSTACK int\n"
                                      + "#endif\n"
                                      + "\n"
                                      + "TSTACK StxStack[150];\n"
                                      + "\n"
                                      + "int pStxStack;\n"
                                      + "\n"
                                      + "#define STXCODE_DEFINED\n"
                                      + "\n"
                                      + "int StxCode(int rule)\n"
                                      + "{\n");
          indent(environment.output, environment.getIndent() - 1);
          environment.output.printf("switch(rule){\n");
          environment.output.println();
          break;

        case java:
          environment.output.printf("\n");
          indent(environment.output, 1);
          environment.output.printf("// Code Generator\n");
          indent(environment.output, 1);
          environment.output.printf("private static final int STACK_DEPTH = 5000;\n");
          indent(environment.output, 1);
          environment.output.printf("LexicalValue stack[] = new LexicalValue[STACK_DEPTH];\n");
          indent(environment.output, 1);
          environment.output.printf("int stackTop;\n\n");
          indent(environment.output, 1);
          environment.output.printf("int generateCode(int rule) {\n");
          indent(environment.output, environment.getIndent());
          environment.output.printf("switch(rule){\n");
          environment.output.println();
          break;

        case pascal:
          environment.output.printf("\n"
                                    + "{ Code generator }\n"
                                      + "\n"
                                      + "Var\n"
                                      + "  {$define STXCODE_DEFINED}\n"
                                      + "  StxStack : Array [0..512] of TStack;\n"
                                      + "  pStxStack: Integer;\n"
                                      + "\n"
                                      + "function StxCode(rule:integer):boolean;\n"
                                      + "begin\n");
          indent(environment.output, environment.getIndent() - 1);
          environment.output.printf("Case rule Of\n");
          break;
      }
    }
    switch (environment.getLanguage()) {
      case C:
        indent(environment.output, environment.getIndent());
        environment.output.printf("case %d:\n", regla + 1);
        indent(environment.output, environment.getIndent() + 1);
        if (environment.isEmitLine()) {
          environment.output.printf("#line %d \"%s\"\n", lineNumber + 1, environment.getSourceFile().toString());
          indent(environment.output, environment.getIndent() + 1);
        }
        break;
      case java:
        indent(environment.output, environment.getIndent() + 1);
        indent(environment.output, environment.getIndent() + 1);
        environment.output.printf("case %d: ", regla + 1);
        indent(environment.output, environment.getIndent() + 1);
        break;
      case pascal:
        indent(environment.output, environment.getIndent());
        environment.output.printf("%d: Begin\n", regla + 1);
        indent(environment.output, environment.getIndent() + 1);
        break;
    }

    while (!end) {
      switch (currentCharacter) {
        case ';': /* final action in C & comment in ASM */
          if ((environment.getLanguage() == Language.C || environment.getLanguage() == Language.java) && nBracks == 0) {
            end = true;
          }
          break;

        case '{': /* level++ in C & COMMENT in PAS */
          if (environment.getLanguage() == Language.C || environment.getLanguage() == Language.java) {
            nBracks++;
          } else if (environment.getLanguage() == Language.pascal) {
            environment.output.print(currentCharacter);
            while ((currentCharacter = getCharacter()) != '}') {
              environment.output.print(currentCharacter);
            }
          }
          break;

        case '}': /* level -- in C */
          if (environment.getLanguage() == Language.C || environment.getLanguage() == Language.java) {
            if (--nBracks <= 0) {
              end = true;
            }
          }
          break;

        case '%':
        case '\\': /* finact in PAS y ASM */
          if (environment.getLanguage() != Language.C && environment.getLanguage() != Language.java) {
            end = true;
            currentCharacter = getCharacter();
            continue;
          }
          break;

        case '(': /* possible comment in PAS */
        case '/': /* possible comment in C */
          if (currentCharacter == '(' && environment.getLanguage() != Language.pascal) {
            break;
          } else {
            characterToFind = ')';
          }
          if (currentCharacter == '/' &&
              environment.getLanguage() != Language.C &&
                environment.getLanguage() == Language.java) {
            break;
          } else {
            characterToFind = '/';
          }
          environment.output.print(currentCharacter);
          currentCharacter = getCharacter();
          if (currentCharacter != '*') {
            continue;
          }

          environment.output.print(currentCharacter);
          currentCharacter = getCharacter();
          bBreak = false;
          while (!bBreak) {
            if (currentCharacter == '\0') {
              environment.error(-1, "Unfinished comment.");
              return false;
            }
            while (currentCharacter == '*') {
              environment.output.print(currentCharacter);
              if ((currentCharacter = getCharacter()) == characterToFind) {
                bBreak = true;
              }
            }
            environment.output.print(currentCharacter);
            currentCharacter = getCharacter();
          }
          continue;

        case '\'': /* constant */
        case '"': /* string */
          characterToFind = currentCharacter;
          environment.output.print(currentCharacter);
          while ((currentCharacter = getCharacter()) != characterToFind) {
            if (currentCharacter == '\0') {
              environment.error(-1, "Statement ' .. ' or \" .. \" not ended.");
              return false;
            }
            if (currentCharacter == '\n') {
              environment.error(-1, "End of line reached on string literal.");
              break;
            }
            if (currentCharacter == '\\') {
              environment.output.print(currentCharacter);
              currentCharacter = getCharacter();
            }
            environment.output.print(currentCharacter);
          }
          break;

        case '\n':
          environment.output.print(currentCharacter);
          currentCharacter = getCharacter();
          indent(environment.output, environment.getIndent() + 1);
          continue;

        case 0:
          environment.error(-1, "Unfinished action detected.");
          return false;

        case '$':
          currentCharacter = getCharacter();
          type = null;
          sign = 1;
          if (currentCharacter == '<') { /* type */
            s2 = currentStringValue;
            getNormalSymbol();
            type = runtimeData.findType(currentStringValue);
            if (type == null) {
              environment.error(-1, "Cannot find type '%s'.", currentStringValue);
              return false;
            }
            currentStringValue = s2;
          }
          if (environment.getLanguage() == Language.java) {
            stackExpression = "stack[stackTop";
          } else {
            stackExpression = "StxStack[pStxStack";
          }
          if (currentCharacter == '$') {
            if (elems == 1) {
              environment.output.printf("%s]", stackExpression);
            } else if (elems != 0) {
              environment.output.printf("%s-%d]", stackExpression, elems - 1);
            } else {
              environment.output.printf("%s+1]", stackExpression);
            }
            if (runtimeData.getTypes().size() != 0) {
              if (type == null) {
                NonTerminal idp = runtimeData.findNonTerminalByName(id);
                if (idp != null) {
                  idp.setCount(idp.getCount() - 1);
                  type = idp.getType();
                }
              }
              if (type != null) {
                environment.output.printf(".%s", type.getName());
              }
            }
            currentCharacter = getCharacter();
            continue;
          }
          if (currentCharacter == '-') {
            sign = -sign;
            currentCharacter = getCharacter();
          }
          if (Character.isDigit(currentCharacter)) {
            num = 0;
            if (currentCharacter == '0') {
              base = 8;
            } else {
              base = 10;
            }
            while (Character.isDigit(currentCharacter)) {
              num = num * base + currentCharacter - '0';
              currentCharacter = getCharacter();
            }
            num = num * sign - elems;
            if (num > 0) {
              environment.error(-1, "Incorrect value of \'$%d\'. Bigger than the number of elements.", num + elems);
              return false;
            }
            if (num == 0) {
              environment.output.printf("%s]", stackExpression);
            } else {
              environment.output.printf("%s%+d]", stackExpression, num);
            }
            if (runtimeData.getTypes().size() != 0) {
              if (num + elems <= 0 && type == null) {
                environment.error(-1, "Cannot determine the type for \'$%d\'.", num + elems);
                return false;
              }
              if (type == null) {
                int j = 0;
                RuleItem rule = null;
                for (i = 1; i < num + elems && j < currentRuleItems.size(); j++, i++) {
                  rule = currentRuleItems.get(j);
                }
                if (rule != null) {
                  Terminal terminal = runtimeData.findTerminalByName(rule.getSymbol().getName());
                  if (terminal != null) {
                    terminal.setCount(terminal.getCount() - 1);
                    type = terminal.getType();
                  } else {
                    NonTerminal nonTerminal = runtimeData.findNonTerminalByName(rule.getSymbol().getName());
                    if (nonTerminal != null) {
                      nonTerminal.setCount(nonTerminal.getCount() - 1);
                      type = nonTerminal.getType();
                    }
                  }
                }
              }
              if (type != null) {
                environment.output.printf(".%s", type.getName());
              }
            }
            continue;
          }
          environment.output.print('$');
          if (sign < 0) {
            environment.output.print('-');
          }
          break;
      }
      environment.output.print(currentCharacter);
      currentCharacter = getCharacter();
    }
    switch (environment.getLanguage()) {
      case C:
        environment.output.println();
        indent(environment.output, environment.getIndent() + 1);
        environment.output.printf("break;\n");
        break;
      case java:
        environment.output.println();
        indent(environment.output, environment.getIndent() + 2);
        environment.output.printf("break;\n");
        break;
      case pascal:
        environment.output.println();
        indent(environment.output, environment.getIndent() + 1);
        environment.output.printf("end; (* StxCode *)\n");
        break;
    }
    ruleActionCount++;
    return true;
  }

  /**
   * copy action until the next ';' or '}' that actually closes
   */
  private boolean tokenAction() throws IOException 
  {
    int nBracks = 0;
    boolean end = false;
    boolean bBreak;
    char characterToFind;
    boolean bStart = true;
    boolean bSkip = false;

    if (tokenActionCount == 0) {
      /* encabezado */
      switch (environment.getLanguage()) {
        case C:
          environment.output.printf("\n"
                                    + "/* Lexical Recognizer */\n"
                                      + "\n"
                                      + "char StxChar;"
                                      + "\n"
                                      + "int StxLexer()\n"
                                      + "{\n");
          break;

        case java:
          environment.output.printf("\n");
          indent(environment.output, environment.getIndent() - 1);
          environment.output.printf("// LexicalRecognizer\n");
          indent(environment.output, environment.getIndent() - 1);
          environment.output.printf("private char currentChar;\n\n");
          indent(environment.output, environment.getIndent() - 1);
          environment.output.printf("int parserElement(boolean initialize) {\n");
          indent(environment.output, environment.getIndent());
          environment.output.printf("lexicalValue = new LexicalValue();\n\n");
          break;

        case pascal:
          environment.output.printf("\n"
                                    + "{ Lexical Analyzer }\n"
                                      + "\n"
                                      + "VAR StxChar:char;\n"
                                      + "\n"
                                      + "function StxLexer():int;\n"
                                      + "begin\n");
          break;
      }
    }
    switch (environment.getLanguage()) {
      case C:
        indent(environment.output, environment.getIndent() + 1);
        if (environment.isEmitLine()) {
          environment.output.printf("#line %d \"%s\"\n", lineNumber + 1, environment.getSourceFile().toString());
          indent(environment.output, environment.getIndent() + 1);
        }
        break;
      case java:
        indent(environment.output, environment.getIndent() + 1);
        break;
      case pascal:
        indent(environment.output, environment.getIndent() + 1);
        break;
    }

    while (!end) {
      switch (currentCharacter) {
        case '$':
          currentCharacter = getCharacter();
          if (currentCharacter == '+') {
            currentCharacter = getCharacter();
            switch (environment.getLanguage()) {
              case C:
                environment.output.printf("StxChar = StxNextChar()");
                break;
              case java:
                environment.output.printf("currentChar = getNextChar(false)");
                break;
              case pascal:
                environment.output.printf("StxChar := StxNextChar()");
                break;
            }
            continue;
          } else if (currentCharacter == 'c') {
            currentCharacter = getCharacter();
            environment.output.printf((environment.getLanguage() == Language.java) ? "currentChar" : "StxChar");
            continue;
          } else if (currentCharacter == 'v') {
            currentCharacter = getCharacter();
            environment.output.printf((environment.getLanguage() == Language.java) ? "lexicalValue" : "StxValue");
            continue;
          }
          environment.output.print('$');
          break;

        case ';': /* finact in C y comment in ASM */
          if ((environment.getLanguage() == Language.C || environment.getLanguage() == Language.java) && nBracks <= 0) {
            end = true;
          }
          break;

        case '{': /* level++ in C y COMMENT in PAS */
          if (environment.getLanguage() == Language.C || environment.getLanguage() == Language.java) {
            nBracks++;
          } else if (environment.getLanguage() == Language.pascal) {
            environment.output.print(currentCharacter);
            while ((currentCharacter = getCharacter()) != '}') {
              environment.output.print(currentCharacter);
            }
          }
          break;

        case '}': /* level -- in C */
          if (environment.getLanguage() == Language.C || environment.getLanguage() == Language.java) {
            if (--nBracks <= 0 && bSkip) {
              end = true;
            }
          }
          if (end && bSkip) {
            currentCharacter = getCharacter();
            continue;
          }
          break;

        case '%':
        case '\\': /* finact in PAS y ASM */
          if (environment.getLanguage() != Language.C && environment.getLanguage() != Language.java) {
            end = true;
            currentCharacter = getCharacter();
            continue;
          }
          break;

        case '(': /* possible comment in PAS */
        case '/': /* possible comment in C */
          if (currentCharacter == '(' && environment.getLanguage() != Language.pascal) {
            break;
          } else {
            characterToFind = ')';
          }
          if (currentCharacter == '/' &&
              environment.getLanguage() != Language.C &&
                environment.getLanguage() == Language.java) {
            break;
          } else {
            characterToFind = '/';
          }
          environment.output.print(currentCharacter);
          currentCharacter = getCharacter();
          if (currentCharacter != '*') {
            continue;
          }

          environment.output.print(currentCharacter);
          currentCharacter = getCharacter();
          bBreak = false;
          while (!bBreak) {
            if (currentCharacter == '\0') {
              environment.error(-1, "Unfinished comment.");
              return false;
            }
            while (currentCharacter == '*') {
              environment.output.print(currentCharacter);
              if ((currentCharacter = getCharacter()) == characterToFind) {
                bBreak = true;
              }
            }
            environment.output.print(currentCharacter);
            currentCharacter = getCharacter();
          }
          continue;

        case '\'': /* constant */
        case '"': /* string */
          characterToFind = currentCharacter;
          environment.output.print(currentCharacter);
          while ((currentCharacter = getCharacter()) != characterToFind) {
            if (currentCharacter == '\0') {
              environment.error(-1, "Statement ' .. ' or \" .. \" not ended");
              return false;
            }
            if (currentCharacter == '\n') {
              environment.error(-1, "End of line reached on string literal.");
              break;
            }
            if (currentCharacter == '\\') {
              environment.output.print(currentCharacter);
              currentCharacter = getCharacter();
            }
            environment.output.print(currentCharacter);
          }
          break;

        case '\n':
          environment.output.print(currentCharacter);
          currentCharacter = getCharacter();
          indent(environment.output, environment.getIndent() + 1);
          continue;

        case 0:
          environment.error(-1, "Unfinished action detected.");
          return false;

      }
      if (!bStart || currentCharacter != '{') {
        environment.output.print(currentCharacter);
      } else {
        bSkip = true;
      }
      if (currentCharacter > ' ') {
        bStart = false;
      }
      currentCharacter = getCharacter();
    }
    environment.output.println();
    tokenActionCount++;
    return true;
  }

  private boolean ruleEndAction() {
    if (ruleActionCount != 0) {
      environment.output.println();
      switch (environment.getLanguage()) {
        case C:
          indent(environment.output, environment.getIndent() - 1);
          environment.output.printf("}/* End of switch */\n");
          indent(environment.output, environment.getIndent() - 1);
          environment.output.printf("return 1; /* OK */\n");
          environment.output.printf("}/* End of StxCode */\n");
          break;

        case java:
          indent(environment.output, environment.getIndent());
          environment.output.printf("}\n");
          indent(environment.output, environment.getIndent());
          environment.output.printf("return 1; // OK\n");
          indent(environment.output, environment.getIndent() - 1);
          environment.output.printf("}\n");
          break;

        case pascal:
          indent(environment.output, environment.getIndent());
          environment.output.printf("END;(* CASE *)\n");
          indent(environment.output, environment.getIndent() - 1);
          environment.output.printf("StxCode := true;\n");
          environment.output.printf("END;(* StxCode *)\n");
          break;

      }
    }
    return true;
  }

  private boolean tokenEndAction() {
    if (tokenActionCount != 0) {
      environment.output.println();
      switch (environment.getLanguage()) {
        case C:
          indent(environment.output, environment.getIndent() - 1);
          environment.output.printf("return 0; /* UNKNOWN */\n");
          environment.output.printf("}/* End of StxLexer */\n");
          break;

        case java:
          indent(environment.output, environment.getIndent());
          environment.output.printf("return 0; // UNKNOWN\n");
          indent(environment.output, environment.getIndent() - 1);
          environment.output.printf("}\n");
          break;

        case pascal:
          indent(environment.output, environment.getIndent() - 1);
          environment.output.printf("StxLexer := 0;\n");
          environment.output.printf("END;(* StxLexer *)\n");
          break;

      }
    }
    // ComputeDFA();
    return true;
  }

  private boolean declareAction() throws IOException {
    while (Character.isWhitespace(currentCharacter)) {
      currentCharacter = getCharacter();
    }
    if (environment.getLanguage() == Language.C && environment.isEmitLine()) {
      environment.output.printf("\n#line %d \"%s\"\n", lineNumber, environment.getSourceFile().toString());
    }
    while (currentCharacter != '\0') {
      if (currentCharacter == '\\') {
        if ((currentCharacter = getCharacter()) == '}') {
          currentCharacter = getCharacter();
          return true;
        } else {
          environment.output.print('\\');
        }
      }
      if (currentCharacter == '%') {
        if ((currentCharacter = getCharacter()) == '}') {
          currentCharacter = getCharacter();
          return true;
        } else {
          environment.output.print('%');
        }
      }
      environment.output.print(currentCharacter);
      currentCharacter = getCharacter();
    }
    environment.error(-1, "End of file before \'%%}\'.");
    return false;
  }

  private boolean declareUnion() throws IOException {
    int level;
    boolean hasCharacters;

    if (environment.getLanguage() == Language.C && environment.isEmitLine()) {
      environment.include.printf("\n#line %d \"%s\"\n", lineNumber, environment.getSourceFile().toString());
    }

    runtimeData.setStackTypeDefined(true);
    switch (environment.getLanguage()) {
      case C:
        environment.include.printf("typedef union");
        level = 0;
        while (2 > 1) {
          if (currentCharacter == '\0') {
            environment.error(-1, "End of file processing \'%%union\'.");
            return false;
          }

          environment.include.print(currentCharacter);
          switch (currentCharacter) {
            case '{':
              ++level;
              break;

            case '}':
              --level;
              if (level == 0) {
                environment.include.printf(" tstack, *ptstack;\n\n");
                environment.include.printf("#define TSTACK tstack\n" + "#define PTSTACK ptstack\n\n");
                currentCharacter = getCharacter();
                return true;
              }
          }
          currentCharacter = getCharacter();
        }
        // break;

      case java:
        indent(environment.include, 1);
        environment.include.printf("private class LexicalValue");
        level = 0;
        while (2 > 1) {
          if (currentCharacter == '\0') {
            environment.error(-1, "End of file processing \'%%union\'.");
            return false;
          }

          environment.include.print(currentCharacter);
          switch (currentCharacter) {
            case '{':
              ++level;
              break;

            case '}':
              --level;
              if (level == 0) {
                environment.include.printf("\n\n");
                currentCharacter = getCharacter();
                return true;
              }
            case '\n':
              indent(environment.include, 1);
              break;
          }
          currentCharacter = getCharacter();
        }
        // break;

      case pascal:
        environment.output.printf("Type\n"
                                  + "  {$define TSTACK_DEFINED}\n"
                                    + "  PTStack = ^TStack;\n"
                                    + "  TStack = Record\n"
                                    + "    case integer of");
        level = 0;
        hasCharacters = false;
        while (currentCharacter != '%' && currentCharacter != '\\') {
          if (currentCharacter == '\n') {
            if (hasCharacters) {
              environment.output.printf(");");
            }
            hasCharacters = false;
          } else {
            if (hasCharacters == false) {
              environment.output.printf("      %d:(", level);
              level++;
            }
            hasCharacters = true;
          }
          environment.output.print(currentCharacter);
          currentCharacter = getCharacter();
        }
        currentCharacter = getCharacter();
        environment.output.printf("  end;\n");
        break;
    }

    return true;
  }

  private RuleItem newItem(Symbol elem) {
    RuleItem item;

    item = new RuleItem(elem);
    if (currentRuleItems == null) {
      currentRuleItems = new LinkedList<RuleItem>();
    }
    currentRuleItems.add(item);
    return item;
  }

  private Rule newEmptyRule() {
    Rule rule;

    rule = new Rule(0, actLine, 0, null);
    runtimeData.getRules().add(rule);
    return rule;
  }

  private Rule newRule() {
    Rule rule;

    rule = new Rule(0, actLine, rulePrecedence, null);
    if (currentRuleItems != null) {
      rule.getItems().addAll(currentRuleItems);
      for (RuleItem item : currentRuleItems) {
        item.setRule(rule);
      }
      currentRuleItems = null;
    }
    runtimeData.getRules().add(rule);
    rulePrecedence = 0;
    ruleAssociativity = Associativity.NONE;
    return rule;
  }

  private Rule newRootRule(NonTerminal root) {
    Rule rule;

    rule = new Rule(0, actLine, rulePrecedence, root);
    if (currentRuleItems != null) {
      rule.getItems().addAll(currentRuleItems);
      for (RuleItem item : currentRuleItems) {
        item.setRule(rule);
      }
      currentRuleItems = null;
    }
    runtimeData.getRules().add(0, rule);
    rulePrecedence = 0;
    ruleAssociativity = Associativity.NONE;
    return rule;
  }
  
  /**
   * Check non terminals whose precedence is zero, and make them terminals.
   */
  private void reviewDeclarations() {
    for (int i = 0; i < runtimeData.getNonTerminals().size(); ) {
      NonTerminal nonTerminal = runtimeData.getNonTerminals().get(i);
      if (nonTerminal.getPrecedence() == 0) {
        environment.error(-1, "Warning: token \'%s\' not declared.", nonTerminal.getName());
        runtimeData.getTerminals().add(new Terminal(nonTerminal));
        runtimeData.getNonTerminals().remove(nonTerminal);
      } else {
        i++;
      }
    }
  }

  private void computeRootSymbol() {
    runtimeData.setRoot(null);
    boolean bError = false;
    for (NonTerminal nonTerminal : runtimeData.getNonTerminals()) {
      if (nonTerminal.getCount() == 0) {
        if (runtimeData.getRoot() == null) {
          runtimeData.setRoot(nonTerminal);
        } else {
          bError = true;
          runtimeData.setRoot(null);
          break;
        }
      }
    }

    if (runtimeData.getStart() != null) { // Was it given with %start ?
      for (NonTerminal nonTerminal : runtimeData.getNonTerminals()) {
        if (nonTerminal.getCount() == 0 && !nonTerminal.equals(runtimeData.getStart())) {
          Rule stx = locateRuleWithId(nonTerminal.getId());
          environment.error(lineNumber(stx), "Warning: Symbol \'%s\' not used.", nonTerminal.getName());
        }
      }
    } else {
      if (runtimeData.getRoot() != null) {
        Rule stx = locateRuleWithId(runtimeData.getRoot().getId());
        environment.error(lineNumber(stx), "Assumed \'%s\' as distinguished symbol.", runtimeData.getRoot().getName());
        runtimeData.setStart(runtimeData.getRoot());
      } else if (bError) {
        for (NonTerminal id : runtimeData.getNonTerminals()) {
          if (id.getCount() == 0) {
            Rule stx = locateRuleWithId(id.getId());
            environment.error(lineNumber(stx), "Warning: Symbol \'%s\' not used.", id.getName());
          }
        }
        environment.error(-1, "Distinguished symbol cannot be determined. Use %%start.");
        return;
      } else {
        environment.error(-1, "The distinguished symbol does not exist.");
        return;
      }
    }

    boolean found = false;
    for (NonTerminal nonTerminal : runtimeData.getNonTerminals()) {
      if (nonTerminal.getName().equals(DISTINGUISHED_SYMBOL_NAME)) {
        runtimeData.setRoot(nonTerminal);
        found = true;
        break;
      }
    }
    if (!found) {
      NonTerminal root = new NonTerminal(DISTINGUISHED_SYMBOL_NAME);
      runtimeData.getNonTerminals().add(root);
      runtimeData.setRoot(root);
    }
    newItem(runtimeData.getStart());
    newRootRule(runtimeData.getRoot());
  }

  private void generateTopRecoveryTable() {
    for (Terminal id : runtimeData.getTerminals()) {
      if (id instanceof ErrorToken) {
        numberOfRecoveries++;
      }
    }
    switch (environment.getLanguage()) {
      case C:
        environment.output.printf("\n#define RECOVERS %d\n\n"
                                  + "/* Contains tokens in compact mode, and column in matrix */", numberOfRecoveries);
        if (numberOfRecoveries != 0) {
          environment.output.printf("\nint StxRecoverTable[RECOVERS] = {\n");
        } else {
          environment.output.printf("\nint StxRecoverTable[1] = {0};\n\n");
        }
        break;
      case java:
        environment.output.printf("\n");
        indent(environment.output, environment.getIndent() - 1);
        environment.output.printf("private static final int RECOVERS=%d;\n\n", numberOfRecoveries);
        indent(environment.output, environment.getIndent() - 1);

        environment.output.printf("// Contains tokens in compact mode, and column in matrix\n");
        indent(environment.output, environment.getIndent() - 1);
        if (numberOfRecoveries != 0) {
          environment.output.printf("int recoverTable[] = {\n");
        } else {
          environment.output.printf("int recoverTable[] = {0};\n\n");
        }
        break;
      case pascal:
        environment.output.printf("\nConst\n  RECOVERS = %d;\n"
                                  + "{ Contains tokens in compact mode, and column in matrix }", numberOfRecoveries - 1);
        if (numberOfRecoveries != 0) {
          environment.output.printf("\n  StxRecoverTable : array [0..RECOVERS] of INTEGER = (\n");
        } else {
          environment.output.printf("\n  StxRecoverTable : array [0..0] of INTEGER = (0);\n\n");
        }
        break;
    }
  }

  private void finalizeSymbols() {
    environment.report
        .printf("## Token                                    Name                                     Value Err  Refs  Prec Assc  Type\n");
    environment.report
        .printf("________________________________________________________________________________________________________________________\n");

    int whichRecoveries = 0;
    int terminals = 0;
    for (Terminal id : runtimeData.getTerminals()) {
      // Look for the default token for a non assigned terminal symbol
      if (id.getToken() == -1) {
        int tok_num = 1;
        for (tok_num = environment.isPacked() ? 32768 : 1;; tok_num++) {
          Terminal cual = runtimeData.findTerminalByToken(tok_num);
          if (cual == null) {
            break;
          }
        }
        id.setToken(tok_num);
      }
      id.setId(terminals++);
      environment.report.printf("%2d %-40s %-40s %5d %s %5d %5d %-5s ", terminals, id.getId(), id.getName(), id
          .getToken(), id instanceof ErrorToken ? "Yes" : "No ", id.getCount(), id.getPrecedence(), id
          .getAssociativity().displayName());
      if (id.getType() != null) {
        environment.report.printf("%s", id.getType().getName());
      }
      environment.report.printf("\n");
      if (id instanceof ErrorToken) {
        int recoveryToken = environment.isPacked() ? id.getToken() : terminals;
        switch (environment.getLanguage()) {
          case C:
            if (++whichRecoveries < numberOfRecoveries) {
              environment.output.printf("\t%d /* %s */,\n", recoveryToken, id.getName());
            } else {
              environment.output.printf("\t%d /* %s */\n};\n\n", recoveryToken, id.getName());
            }
            break;
          case java:
            indent(environment.output, environment.getIndent());
            if (++whichRecoveries < numberOfRecoveries) {
              environment.output.printf("%d, // %s\n", recoveryToken, id.getName());
            } else {
              environment.output.printf("%d // %s\n", recoveryToken, id.getName());
              indent(environment.output, environment.getIndent() - 1);
              environment.output.printf("};\n\n");
            }
            break;
          case pascal:
            if (++whichRecoveries < numberOfRecoveries) {
              environment.output.printf("\t%d, (* %s *)\n", recoveryToken, id.getName());
            } else {
              environment.output.printf("\t%d (* %s *) );\n\n", recoveryToken, id.getName());
            }
            break;
        }
      }
    }

    switch (environment.getLanguage()) {
      case C:
        environment.output.printf("\n#define TOKENS %d\n", terminals);
        environment.output.printf("\nint StxTokens[TOKENS] = {\n");
        break;
      case java:
        indent(environment.output, environment.getIndent() - 1);
        environment.output.printf("private static int TOKENS=%d;\n", terminals);
        indent(environment.output, environment.getIndent() - 1);
        environment.output.printf("private static int tokens[] = {\n");
        break;
      case pascal:
        environment.output.printf("\nConst\n  TOKENS = %d;\n", terminals - 1);
        environment.output.printf("\n  StxTokens : array [0..TOKENS] of Integer = (\n");
        break;
    }

    int i = 1;
    for (Terminal id : runtimeData.getTerminals()) {
      switch (environment.getLanguage()) {
        case C:
          if (i == terminals) {
            environment.output.printf("\t%d /* %s (%s)*/\n};\n\n", id.getToken(), id.getName(), id.getFullName());
          } else {
            environment.output.printf("\t%d, /* %s (%s) */\n", id.getToken(), id.getName(), id.getFullName());
          }
          break;
        case java:
          indent(environment.output, environment.getIndent());
          if (i == terminals) {
            environment.output.printf("%d // %s (%s)\n", id.getToken(), id.getName(), id.getFullName());
            indent(environment.output, environment.getIndent() - 1);
            environment.output.printf("};\n\n");

          } else {
            environment.output.printf("%d, // %s (%s)\n", id.getToken(), id.getName(), id.getFullName());
          }
          break;
        case pascal:
          if (i == terminals) {
            environment.output.printf("    %d\n (* %s (%s) *) );\n", id.getToken(), id.getName(), id.getFullName());
          } else {
            environment.output.printf("    %d,\n (* %s (%s) *)", id.getToken(), id.getName(), id.getFullName());
          }
          break;
      }
      i++;
    }
    environment.report.printf("\n");
    environment.report
        .printf("## Non Terminals                            Name                                     Refs  Type\n");
    environment.report
        .printf("__________________________________________________________________________________________________\n");

    int noterminals = 0;
    for (NonTerminal id : runtimeData.getNonTerminals()) {
      environment.report.printf("%2d %-40s %-40s %-2d    ", noterminals + terminals, id.getId(), id.getName(),
          id.getCount());
      if (id.getType() != null) {
        environment.report.printf("%s", id.getType().getName());
      }
      environment.report.printf("\n");
      id.setId(noterminals++ + terminals);
      id.setFirst(null);
      id.setFollow(null);
    }
  }

  private void finalizeRules() {
    environment.report.printf("\n");
    environment.report.printf("Prec Rule  Grammar\n");
    environment.report.printf("_____________________________________________________\n");
    int i = 0;
    for (Rule stx : runtimeData.getRules()) {
      stx.setRulenum(i);
      environment.report.printf("[%2d]  %3d. %s -> ", stx.getPrecedence(), i, stx.getLeftHand().getName());
      for (RuleItem itm : stx.getItems()) {
        environment.report.printf("%s ", itm.getSymbol().getName());
      }
      environment.report.printf("\n");
      i = i + 1;
    }
  }

  private void defineTokens() {
    boolean first = true;
    for (Terminal id : runtimeData.getTerminals()) {
      id.computeVariable();
      if (id.getVariable().equals("_")) {
        switch (environment.getLanguage()) {
          case C:
            if (first) {
              environment.include.printf("\n/* Token definitions */\n");
              first = false;
            }
            environment.include.printf("#define %s %d\n", id.getVariable(), id.getToken());
            break;
          case java:
            if (first) {
              indent(environment.include, environment.getIndent() - 1);
              environment.include.printf("// Token definitions\n");
              first = false;
            }
            indent(environment.include, environment.getIndent() - 1);
            environment.include.printf("private static final int %s=%d;\n", id.getVariable(), id.getToken());
            break;
          case pascal:
            if (first) {
              environment.include.printf("\n(* Token definitions *)\n");
              environment.include.printf("\nConst\n");
              first = false;
            }
            environment.include.printf("  %s = %d;\n", id.getVariable(), id.getToken());
            break;
        }
      }
    }
    environment.include.printf("\n");
  }

  private Rule locateRuleWithId(int id) {
    Rule rule = null;
    for (int i = 0; i < runtimeData.getRules().size(); i++) {
      rule = runtimeData.getRules().get(i);
      if (id == rule.getLeftHandId()) {
        break;
      }
    }
    return rule;
  }

  private int lineNumber(Rule rule) {
    return rule != null ? rule.getLineNumber() - 1 : -1;
  }

  public void execute() throws ParsingException {
    if (environment.isVerbose()) {
      System.out.println("Parse");
    }
    try {
      currentCharacter = getCharacter();
      lineNumber = 0;
      markers = 0;
      Terminal terminal = new Terminal("$");
      runtimeData.getTerminals().add(terminal);
      terminal.setCount(0);
      terminal.setToken(0);
      if (!StxParse() || isError) {
        throw new ParsingException("Parser returned errors.  Please see messages from parser");
      }
      reviewDeclarations();
      computeRootSymbol();
      generateTopRecoveryTable();
      finalizeSymbols();
      finalizeRules();
      defineTokens();
      runtimeData.setNumberOfErrors(StxErrors);
      runtimeData.setFinalActions(finalActions);
    } catch (IOException e) {
      throw new ParsingException("IOError ocurred when parsing: " + e.getMessage(), e);
    }
  }
}
