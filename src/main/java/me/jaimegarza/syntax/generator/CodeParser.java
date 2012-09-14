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

import java.io.IOException;
import java.util.Arrays;

import me.jaimegarza.syntax.ParsingException;
import me.jaimegarza.syntax.definition.Associativity;
import me.jaimegarza.syntax.definition.ErrorToken;
import me.jaimegarza.syntax.definition.NonTerminal;
import me.jaimegarza.syntax.definition.Rule;
import me.jaimegarza.syntax.definition.RuleItem;
import me.jaimegarza.syntax.definition.Symbol;
import me.jaimegarza.syntax.definition.Terminal;
import me.jaimegarza.syntax.definition.Type;
import me.jaimegarza.syntax.env.Environment;

/**
 * Parser for a grammar.<p>
 * TODO: P1-This parser will be replaced for a generated one (which
 * probably will be created from this one)
 *
 * Phases:
 * 
 * <ol>
 *   <li><b>Code Parser</b> (This Phase)
 *   <li>Structural Analysis
 *   <li>Table Generation
 *   <li>Writing Code
 * </ol>
 * @author jaimegarza@gmail.com
 *
 */
public class CodeParser extends AbstractCodeParser {
  static final int TOK_MARCA = 256;
  private static final int TOK_START = 257;
  static final int TOK_TOKEN = 258;
  private static final int TOK_TYPE = 259;
  static final int TOK_UNION = 260;
  static final int TOK_TYPENAME = 261;
  static final int TOK_TERM = 262;
  static final int TOK_LEFT = 263;
  static final int TOK_RIGHT = 264;
  static final int TOK_BINARY = 265;
  static final int TOK_ERRDEF = 266;
  static final int TOK_NUM = 267;
  static final int TOK_PREC = 268;
  static final int TOK_NAME = 269;
  static final int TOK_ERROR = 270;
  private static final int TOK_LEXER = 271;
  static final int TOK_RX_PIPE = 272;
  static final int TOK_RX_LPAR = 273;
  static final int TOK_RX_RPAR = 274;
  static final int TOK_RX_STAR = 275;
  static final int TOK_RX_PLUS = 276;
  static final int TOK_RX_HUH = 277;
  static final int TOK_RX_ANY = 278;
  @SuppressWarnings("unused")
  private static final int TOK_RX_CHARS = 279;
  @SuppressWarnings("unused")
  private static final int TOK_LEXCODE = 280;
  static final int TOK_CHARS = 281;

  static final String tokenNames[] = { "TOK_MARCA", "TOK_START", "TOK_TOKEN", "TOK_TYPE", "TOK_UNION",
      "TOK_TYPENAME", "TOK_TERM", "TOK_LEFT", "TOK_RIGHT", "TOK_BINARY", "TOK_ERRDEF", "TOK_NUM", "TOK_PREC",
      "TOK_NAME", "TOK_ERROR", "TOK_LEXER", "TOK_RX_PIPE", "TOK_RX_LPAR", "TOK_RX_RPAR", "TOK_RX_STAR", "TOK_RX_PLUS",
      "TOK_RX_HUH", "TOK_RX_ANY", "TOK_RX_CHARS", "TOK_LEXCODE", "TOK_CHARS" };

  static final ReservedWord RWord[] = { new ReservedWord("token", TOK_TERM),
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

  static final Parser StxParsingTable[] = { new Parser(0, -3, 13, 0), new Parser(13, 0, 1, -1),
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

  static final String StxErrorTable[] = { "Expecting a declaration", "'%%' expected", "Token expected",
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
  
  StackElement StxValue = null;
  private int sStxStack[] = new int[MIN_STACK];
  private int pStxStack = 0;
  private StackElement[] StxStack = new StackElement[MIN_STACK];
  private int StxSym = 0;
  private int StxState = 0;
  int StxErrors = 0;
  private int StxErrorFlag = 0;
  public CodeParser(Environment environment) {
    super(environment);
  }

  /**
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

  /**
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

  /**
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

  /**
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

  /**
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

  /**
   * Generation of code
   */
  private boolean StxCode(int rule) throws IOException {
    int i;
    switch (rule) {

      case 1:
        generateLexerFooter();
        break;
      case 3:
        generateLexerFooter();
        break;
      case 5:
        {
          generateCodeGeneratorFooter();
          finalActions = false;
        }
        break;
      case 6:
        {
          generateCodeGeneratorFooter();
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
        if (!generateStructure()) {
          return false;
        }
        break;
      case 15:
        currentType = null;
        break;
      case 16:
        if (!generateDeclaration()) {
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
            currentType.addUsage(terminal);
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
        generateLexerCode();
        break;
      case 53:
        currentType = new Type(StxStack[pStxStack].id);
        if (runtimeData.getTypes().contains(currentType)) {
          currentType = runtimeData.getTypes().get(runtimeData.getTypes().indexOf(currentType));
        } else {
          runtimeData.getTypes().add(currentType);
        }
        break;
      case 54:
        currentType = null;
        break;
      case 55:
        return setLeftHandOfLastRule(StxStack[pStxStack - 3].id);
      case 56:
        return setLeftHandOfLastRule(StxStack[pStxStack - 3].id);
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
          i = runtimeData.currentRuleItems != null ? runtimeData.currentRuleItems.size() : 0;
          if (!ruleAction(runtimeData.getRules().size(), i, currentNonTerminalName)) {
            return false;
          }
          bActionDone = true;
        }
        break;

    }/* End of switch */
    return true; /* OK */
  }

  /**
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

  /**
   * Main parser routine, uses Shift, Reduce and Recover
   */
  boolean StxParse() throws IOException {
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

  /**
   * Found a rule action.  Copy it to the output stream as-is
   * @param ruleNumber the rule index
   * @param elementCount the elements in the rule
   * @param nonTerminalName the left hand symbol of the rule
   */
  private boolean ruleAction(int ruleNumber, int elementCount, String nonTerminalName) throws IOException {
    generateCodeGeneratorHeader();
    generateCaseStatement(ruleNumber, nonTerminalName + " -> " + runtimeData.currentRuleItems.toString());
    
    if (!environment.language.generateRuleCode(this, this, elementCount, nonTerminalName)) {
      return false;
    }
    
    generateCaseEnd();
    runtimeData.ruleActionCount++;
    
    return true;
  }

  /**
   * Get next token
   * 
   * @return the next token, changing mode as needed
   */
  protected int StxScan() throws IOException {
    int rc;
  
    if (isRegex) {
      rc = getRegexSymbol();
      if (environment.isVerbose()) {
        System.out.printf("RegexScanner: %d\n", rc);
      }
    } else {
      rc = getNormalSymbol();
      StxValue = new StackElement(-1, tokenNumber, mustClose, runtimeData.currentStringValue, null);
      if (environment.isDebug()) {
        System.out.printf("* StdScanner: %s(%d) {%s}\n",
            (rc >= 256 ? tokenNames[rc - 256] : "\"" + Character.toString((char) rc) + "\""), rc,
            StxValue != null ? StxValue.toString() : "");
      }
    }
    return rc;
  }

  /**
   * report an error
   * 
   * @param StxState state of the error
   * @param StxSym causing token
   * @param pStxStack the position in the stack when the error happened
   * @return
   */
  protected int StxError(int StxState, int StxSym, int pStxStack) {
    int msg = StxParsingTable[StxState].msg;
    if (msg >= 0) {
      environment.error(-1, "Syntax error %d :\'%s\'.", StxState, StxErrorTable[msg]);
    } else {
      System.err.printf("%s(%05d) : Unknown error on state %d\n", environment.getSourceFile().toString(),
          runtimeData.lineNumber + 1, StxState);
    }
    isError = true;
    return 0; /*
               * with actions, it recovers weird. Need to change the action
               * stuff to the scanner
               */
  }

  @Override
  public int getRegexSymbol() throws IOException {
    char c2;
  
    if (isEqual) {
      isEqual = false;
      runtimeData.currentStringValue = "";
      return TOK_TOKEN;
    }
  
    if (runtimeData.currentCharacter == '|') {
      getCharacter();
      return TOK_RX_PIPE;
    }
    if (runtimeData.currentCharacter == '(') {
      getCharacter();
      return TOK_RX_LPAR;
    }
    if (runtimeData.currentCharacter == ')') {
      getCharacter();
      return TOK_RX_RPAR;
    }
    if (runtimeData.currentCharacter == '*') {
      getCharacter();
      return TOK_RX_STAR;
    }
    if (runtimeData.currentCharacter == '+') {
      getCharacter();
      return TOK_RX_PLUS;
    }
    if (runtimeData.currentCharacter == '?') {
      getCharacter();
      return TOK_RX_HUH;
    }
    if (runtimeData.currentCharacter == '.') {
      getCharacter();
      return TOK_RX_ANY;
    }
    if (runtimeData.currentCharacter == '/') {
      isRegex = false;
      isEqual = true;
      getCharacter();
      return '/';
    }
  
    if (runtimeData.currentCharacter == '\\') {
      getCharacter();
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

  @Override
  public int getNormalSymbol() throws IOException {
    char c2;
    String s2;
    boolean end;
  
    s2 = runtimeData.currentStringValue;
    runtimeData.currentStringValue = "";
  
    if (markers >= 2) {
      return 0;
    }
  
    if (isCurlyBrace) {
      isCurlyBrace = false;
      return ';';
    }
  
    if (isEqual) {
      isEqual = false;
      runtimeData.currentStringValue = "";
      return TOK_TOKEN;
    }
  
    while (2 > 1) {
      while (Character.isWhitespace(runtimeData.currentCharacter)) {
        getCharacter();
      }
      if (runtimeData.currentCharacter == '/') {
        if ((getCharacter()) == '*') {
          getCharacter();
          end = false;
          while (!end) {
            while (runtimeData.currentCharacter == '*') {
              if ((getCharacter()) == '/') {
                end = true;
              }
            }
            getCharacter();
          }
        } else {
          ungetCharacter(runtimeData.currentCharacter);
          runtimeData.currentCharacter = '/';
          break;
        }
      } else {
        break;
      }
    }
  
    if (runtimeData.currentCharacter == '\0') {
      return 0;
    }
  
    if (runtimeData.currentCharacter == '%' || runtimeData.currentCharacter == '\\') {
      getCharacter();
      switch (runtimeData.currentCharacter) {
        case '0':
          getCharacter();
          return TOK_TERM;
        case '<':
          getCharacter();
          return TOK_LEFT;
        case '2':
          getCharacter();
          return TOK_BINARY;
        case '>':
          getCharacter();
          return TOK_RIGHT;
        case '%':
        case '\\':
          getCharacter();
          markers++;
          return TOK_MARCA;
        case '=':
          getCharacter();
          return TOK_PREC;
        case '@':
          getCharacter();
          return TOK_NAME;
        case '{':
          getCharacter();
          isCurlyBrace = true;
          return '{';
        case '!':
          getCharacter();
          return TOK_ERRDEF;
      }
      while (Character.isLetterOrDigit(runtimeData.currentCharacter)) {
        runtimeData.currentStringValue += runtimeData.currentCharacter;
        getCharacter();
      }
      for (ReservedWord rw : RWord) {
        if (runtimeData.currentStringValue.equals(rw.word)) {
          if (rw.token == TOK_UNION) {
            isCurlyBrace = true;
          }
          return rw.token;
        }
      }
      isError = true;
      environment.error(-1, "Reserved word \'%s\' is incorrect.", runtimeData.currentStringValue);
      return TOK_ERROR;
    }
  
    if (runtimeData.currentCharacter == ';') {
      getCharacter();
      return ';';
    }
  
    if (runtimeData.currentCharacter == ',') {
      getCharacter();
      return ',';
    }
  
    if (runtimeData.currentCharacter == ':') {
      currentNonTerminalName = s2;
      getCharacter();
      return ':';
    }
  
    if (runtimeData.currentCharacter == '|') {
      getCharacter();
      return '|';
    }
  
    if (runtimeData.currentCharacter == '=') {
      getCharacter();
      isEqual = true;
      return '=';
    }
  
    if (runtimeData.currentCharacter == '{') {
      isEqual = true;
      return '=';
    }
  
    if (runtimeData.currentCharacter == '<') {
      getCharacter();
      runtimeData.currentStringValue = "";
      while (runtimeData.currentCharacter != '\0' && runtimeData.currentCharacter != '>' && runtimeData.currentCharacter != '\n') {
        runtimeData.currentStringValue += runtimeData.currentCharacter;
        getCharacter();
      }
      if (runtimeData.currentCharacter != '>') {
        isError = true;
        environment.error(-1, "Statement < .. > not ended.");
        return TOK_ERROR;
      }
      getCharacter();
      return TOK_TYPENAME;
    }
  
    if (runtimeData.currentCharacter == '/') {
      isRegex = true;
      isEqual = true;
      getCharacter();
      return '/';
    }
  
    if (Character.isDigit(runtimeData.currentCharacter)) {
      runtimeData.currentStringValue = "";
      while (Character.isDigit(runtimeData.currentCharacter)) {
        runtimeData.currentStringValue += runtimeData.currentCharacter;
        getCharacter();
      }
      tokenNumber = Integer.parseInt(runtimeData.currentStringValue);
      return TOK_NUM;
    }
  
    mustClose = false;
    if (runtimeData.currentCharacter == '\'' || runtimeData.currentCharacter == '"') {
      c2 = runtimeData.currentCharacter;
      mustClose = true;
      getCharacter();
    } else {
      c2 = ':';
    }
  
    runtimeData.currentStringValue = "";
    do { /* TOKEN */
      runtimeData.currentStringValue += runtimeData.currentCharacter;
      getCharacter();
      if (runtimeData.currentCharacter == '\0') {
        break;
      }
      if (!mustClose && "%\\;,:|={< \r\t\n".indexOf(runtimeData.currentCharacter) >= 0) {
        break;
      }
    } while (runtimeData.currentCharacter != c2);
  
    if (mustClose && runtimeData.currentCharacter != c2) {
      isError = true;
      environment.error(-1, "Statement ' .. ' or \" .. \" not ended.");
      return TOK_ERROR;
    }
    tokenNumber = -1;
    if (runtimeData.currentStringValue.equals("\\a")) {
      tokenNumber = 7;
    } else if (runtimeData.currentStringValue.equals("\\b")) {
      tokenNumber = '\b';
    } else if (runtimeData.currentStringValue.equals("\\n")) {
      tokenNumber = '\n';
    } else if (runtimeData.currentStringValue.equals("\\t")) {
      tokenNumber = '\t';
    } else if (runtimeData.currentStringValue.equals("\\f")) {
      tokenNumber = '\f';
    } else if (runtimeData.currentStringValue.equals("\\r")) {
      tokenNumber = '\r';
    } else if (runtimeData.currentStringValue.length() >= 2 && runtimeData.currentStringValue.substring(0, 2).equals("\\x")) {
      int p = 2;
      tokenNumber = 0;
      while (2 > 1) {
        if (runtimeData.currentStringValue.charAt(p) >= '0' && runtimeData.currentStringValue.charAt(p) <= '9') {
          tokenNumber = tokenNumber * 16 + runtimeData.currentStringValue.charAt(p++) - '0';
        } else if (runtimeData.currentStringValue.charAt(p) >= 'A' && runtimeData.currentStringValue.charAt(p) <= 'F') {
          tokenNumber = tokenNumber * 16 + runtimeData.currentStringValue.charAt(p++) - 'A' + 10;
        } else if (runtimeData.currentStringValue.charAt(p) >= 'a' && runtimeData.currentStringValue.charAt(p) <= 'f') {
          tokenNumber = tokenNumber * 16 + runtimeData.currentStringValue.charAt(p++) - 'a' + 10;
        } else {
          break;
        }
      }
    } else if (runtimeData.currentStringValue.length() >= 2 && runtimeData.currentStringValue.substring(0, 2).equals("\\0")) {
      int p = 2;
      tokenNumber = 0;
      while (runtimeData.currentStringValue.charAt(p) >= '0' && runtimeData.currentStringValue.charAt(p) <= '7') {
        tokenNumber = tokenNumber * 8 + runtimeData.currentStringValue.charAt(p++) - '0';
      }
    }
  
    if (mustClose) {
      getCharacter();
      if (runtimeData.currentStringValue.length() == 1) {
        tokenNumber = runtimeData.currentStringValue.charAt(0);
      }
    }
  
    return TOK_TOKEN;
  }

  /**
   * Execute this phase
   * @throws ParsingException on error.  Check cause and message.
   */
  public void execute() throws ParsingException {
    if (environment.isVerbose()) {
      System.out.println("Parse");
    }
    try {
      getCharacter();
      runtimeData.lineNumber = 0;
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
      generateTokenDefinitions();
      runtimeData.setNumberOfErrors(StxErrors);
      runtimeData.setFinalActions(finalActions);
    } catch (IOException e) {
      throw new ParsingException("IOError ocurred when parsing: " + e.getMessage(), e);
    }
  }
  
  /* Inner classes */
  static class Parser {
    public int position;
    public int defa;
    public int elements;
    public int msg;

    Parser(int position, int defa, int elements, int msg) {
      super();
      this.position = position;
      this.defa = defa;
      this.elements = elements;
      this.msg = msg;
    }
  }
  
  private static class Grammar {
    public int symbol;
    public int reductions;

    Grammar(int symbol, int reductions) {
      super();
      this.symbol = symbol;
      this.reductions = reductions;
    }
  }
  
  private static class ParserAction {
    public int symbol;
    public int state;

    ParserAction(int symbol, int state) {
      super();
      this.symbol = symbol;
      this.state = state;
    }
  }

  private static class ParserGoTo {
    public int origin;
    public int destination;

    ParserGoTo(int origin, int destination) {
      super();
      this.origin = origin;
      this.destination = destination;
    }
  }

  static class ReservedWord {
    String word;
    int token;

    ReservedWord(String word, int token) {
      super();
      this.word = word;
      this.token = token;
    }
  }

  static class StackElement {
    public int stateNumber;
    public int value;
    public boolean mustClose;
    public String id;
    public String regex;

    StackElement(int estado, int value, boolean mustClose, String id, String regex) {
      super();
      this.stateNumber = estado;
      this.value = value;
      this.mustClose = mustClose;
      this.id = id;
      this.regex = regex;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return "state:" + stateNumber + ", value:" + value + ", mustClose:" + mustClose + ", id:" + id;
    }
  }
}
