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
@SuppressWarnings("unused")
public class SyntaxCodeParser extends AbstractCodeParser {

  private String currentTypeName = null;

  public SyntaxCodeParser(Environment env) {
    super(env);
  }
  
  public static class StackElement {
      public int stateNumber;
      public int value;
      public boolean mustClose;
      public String id;
      public String regex;
  
      StackElement(int state, int value, boolean mustClose, String id, String regex) {
        super();
        this.stateNumber = state;
        this.value = value;
        this.mustClose = mustClose;
        this.id = id;
        this.regex = regex;
      }  
  
      @Override
      public String toString() {
        return "state:" + stateNumber + ", value:" + value + ", mustClose:" + mustClose + ", id:" + id;
      }  
  }


  // Code Generator
  private static final int STACK_DEPTH = 5000;
  StackElement stack[] = new StackElement[STACK_DEPTH];
  int stackTop;

  boolean generateCode(int rule) {
    switch(rule){

      // 3. DeclMarker ->  MARK
      case 3:
        generateLexerFooter();
        break;
      // 4. Actions -> 
      case 4:
        {
                                                        generateCodeGeneratorFooter();
                                                        finalActions = false;
                                                      }
        break;
      // 5. Actions ->  MARK
      case 5:
        {
                                                        generateCodeGeneratorFooter();
                                                        finalActions = true;
                                                      }
        break;
      // 9. Declaration ->  START TOKEN
      case 9:
        if (!declareStart(stack[stackTop].id)) return false;
        break;
      // 11. Declaration ->  TYPE TYPENAME
      case 11:
        currentTypeName = stack[stackTop].id;
        break;
      // 12. Declaration ->  TYPE TYPENAME $code-fragment-1 Tokens
      case 12:
        currentTypeName = null;
        break;
      // 13. Declaration ->  DECLARE TYPENAME
      case 13:
        declareOneType(stack[stackTop].id);
        break;
      // 15. Declaration ->  UNION
      case 15:
        if (!generateStructure()) return false;
        break;
      // 16. Declaration ->  PrecDef Definition
      case 16:
        currentType = null;
        break;
      // 17. Declaration ->  {
      case 17:
        if (!generateDeclaration()) return false;
        break;
      // 19. PrecDef ->  TERM
      case 19:
        {
                                                        ruleAssociativity = Associativity.NONE;
                                                        isErrorToken = false;
                                                      }
        break;
      // 20. PrecDef ->  LEFT
      case 20:
        {
                                                        rulePrecedence++;
                                                        ruleAssociativity = Associativity.LEFT;
                                                        isErrorToken = false;
                                                      }
        break;
      // 21. PrecDef ->  RIGHT
      case 21:
        {
                                                        rulePrecedence++;
                                                        ruleAssociativity = Associativity.RIGHT;
                                                        isErrorToken = false;
                                                      }
        break;
      // 22. PrecDef ->  BINARY
      case 22:
        {
                                                        rulePrecedence++;
                                                        ruleAssociativity = Associativity.BINARY;
                                                        isErrorToken = false;
                                                      }
        break;
      // 23. PrecDef ->  ERRDEF
      case 23:
        {
                                                        ruleAssociativity = Associativity.NONE;
                                                        isErrorToken = true;
                                                      }
        break;
      // 24. Tokens ->  Tokens , TOKEN
      case 24:
        if (!declareOneNonTerminal(currentTypeName, stack[stackTop].id)) return false;
        break;
      // 25. Tokens ->  TOKEN
      case 25:
        if (!declareOneNonTerminal(currentTypeName, stack[stackTop].id)) return false;
        break;
      // 28. Name ->  TOKEN : TOKEN
      case 28:
        if (!nameOneNonTerminal(stack[stackTop-2].id, stack[stackTop].id)) return false;
        break;
      // 32. Precedence ->  TOKEN Number ErrInfo LexicAction
      case 32:
        {
                                                        if (stack[stackTop-2].value != -1) {
                                                          stack[stackTop-3].value = stack[stackTop-2].value;
                                                        }
                                                        if (!declareOneTerminal(stack[stackTop-3].id, isErrorToken, ruleAssociativity, rulePrecedence, currentType, stack[stackTop-3].value, stack[stackTop-1].id)) return false;
                                                      }
        break;
      // 34. Number -> 
      case 34:
        stack[stackTop+1] = new StackElement(0, -1, false, "", null);
        break;
      // 35. ErrInfo ->  : TOKEN
      case 35:
        stack[stackTop-1].id = stack[stackTop].id;
        break;
      // 36. ErrInfo -> 
      case 36:
        stack[stackTop+1] = new StackElement(0, 0, false, "", null);
        break;
      // 37. LexicAction ->  Equals TOKEN
      case 37:
        stack[stackTop-1].regex = null;
        break;
      // 38. LexicAction ->  Slash RegExp Slash
      case 38:
        //stack[stackTop-2].regex = AddTree(stack[stackTop-1].regex);
        break;
      // 39. LexicAction -> 
      case 39:
        stack[stackTop+1] = new StackElement(0, -1, false, "", null);
        break;
      // 41. RegExp ->  RegExp ConcatElement
      case 41:
        //stack[stackTop-1].regex = SequentialNode(stack[stackTop-1].regex, stack[stackTop].regex);
        break;
      // 42. RegExp ->  RegExp RX_PIPE ConcatElement
      case 42:
        //stack[stackTop-2].regex = AlternateNode(stack[stackTop-2].regex, stack[stackTop].regex);
        break;
      // 44. ConcatElement ->  BasicElement RX_STAR
      case 44:
        //stack[stackTop-1].regex = ZeroOrManyNode(stack[stackTop-1].regex);
        break;
      // 45. ConcatElement ->  BasicElement RX_PLUS
      case 45:
        //stack[stackTop-1].regex = OneOrManyNode(stack[stackTop-1].regex);
        break;
      // 46. ConcatElement ->  BasicElement RX_HUH
      case 46:
        //stack[stackTop-1].regex = ZeroOrOneNode(stack[stackTop-1].regex);
        break;
      // 48. BasicElement ->  RX_LPAR RegExp RX_RPAR
      case 48:
        stack[stackTop-2].regex = stack[stackTop-1].regex;
        break;
      // 49. BasicElement ->  CHARS
      case 49:
        //CharNode(stack[stackTop].regex);
        break;
      // 50. BasicElement ->  RX_ANY
      case 50:
        //AnyNode();
        break;
      // 51. Equals ->  =
      case 51:
        generateLexerCode();
        break;
      // 52. Type ->  TYPENAME
      case 52:
        {
                                                        currentType = new Type(stack[stackTop].id);
                                                        if (runtimeData.getTypes().contains(currentType)) {
                                                          currentType = runtimeData.getTypes().get(runtimeData.getTypes().indexOf(currentType));
                                                        } else {
                                                          runtimeData.getTypes().add(currentType);
                                                        }
                                                      }
        break;
      // 53. Type -> 
      case 53:
        currentType = null;
        break;
      // 54. Productions ->  Productions TOKEN : Rules ;
      case 54:
        if (!setLeftHandOfLastRule(stack[stackTop-3].id)) return false;
        break;
      // 55. Productions ->  TOKEN : Rules ;
      case 55:
        if (!setLeftHandOfLastRule(stack[stackTop-3].id)) return false;
        break;
      // 56. Rules ->  Rules | GrammarRule
      case 56:
        {
                                                        newRule();
                                                        bActionDone = false;
                                                      }
        break;
      // 57. Rules ->  GrammarRule
      case 57:
        {
                                                        newRule();
                                                        currentRuleIndex = runtimeData.getRules().size() - 1;
                                                        bActionDone = false;
                                                      }
        break;
      // 59. GrammarRule -> 
      case 59:
        {
                                                        bActionDone = false;
                                                      }
        break;
      // 62. Symbol ->  TOKEN
      case 62:
        {
                                                        if (stack[stackTop].id.length() == 0) {
                                                          break;
                                                        }
                                                        if (!declareOneItem(stack[stackTop].id, stack[stackTop].value, stack[stackTop].mustClose)) return false;
                                                      }
        break;
      // 63. Symbol ->  PREC TOKEN
      case 63:
        if(!computeAssociativityAndPrecedence(stack[stackTop].id)) return false;
        break;
      // 64. Symbol ->  =
      case 64:
        {
                                                        int i = runtimeData.currentRuleItems != null ? runtimeData.currentRuleItems.size() : 0;
                                                        if (!ruleAction(runtimeData.getRules().size(), i, currentNonTerminalName)) {
                                                          return false;
                                                        }
                                                        bActionDone = true;
                                                      }
        break;
    }
    return true; // OK
  }

  private static final int RECOVERS=0;

  // Contains tokens in compact mode, and column in matrix
  int recoverTable[] = {0};

  private static int TOKENS=37;
  private static int tokens[] = {
    0, // $ ($)
    256, // MARK ('%%')
    257, // START ('%start')
    258, // TOKEN (Token)
    259, // TYPE ('%type')
    260, // UNION ('%union')
    261, // TYPENAME (Type definition)
    262, // TERM ('%token')
    263, // LEFT ('%left')
    264, // RIGHT ('%right')
    265, // BINARY ('%binary')
    266, // ERRDEF ('%error')
    267, // NUM (Number)
    268, // PREC ('%prec')
    269, // NAME ('%name')
    270, // ERROR (Error)
    271, // LEXER (%lexer)
    272, // DECLARE (%declare)
    273, // RX_PIPE ('|')
    274, // RX_LPAR ('(')
    275, // RX_RPAR (')')
    276, // RX_STAR ('*')
    277, // RX_PLUS ('+')
    278, // RX_HUH ('?')
    279, // RX_ANY ('.')
    280, // RX_CHARS (Character or set of characters)
    290, // LEXCODE (lexical code)
    291, // CHARS (regular expression characters)
    59, // ; (semicolon)
    58, // : (colon)
    61, // = (equals sign)
    44, // , (comma)
    124, // | (rule separator ('|'))
    40, // ( (opening parenthesis)
    41, // ) (closing parenthesis)
    47, // / (regular expression marker ('/'))
    123 // { ({)
  };

  // Token definitions
  private static final int EOS=0;
  private static final int MARK=256;
  private static final int START=257;
  private static final int TOKEN=258;
  private static final int TYPE=259;
  private static final int UNION=260;
  private static final int TYPENAME=261;
  private static final int TERM=262;
  private static final int LEFT=263;
  private static final int RIGHT=264;
  private static final int BINARY=265;
  private static final int ERRDEF=266;
  private static final int NUM=267;
  private static final int PREC=268;
  private static final int NAME=269;
  private static final int ERROR=270;
  private static final int LEXER=271;
  private static final int DECLARE=272;
  private static final int RX_PIPE=273;
  private static final int RX_LPAR=274;
  private static final int RX_RPAR=275;
  private static final int RX_STAR=276;
  private static final int RX_PLUS=277;
  private static final int RX_HUH=278;
  private static final int RX_ANY=279;
  private static final int RX_CHARS=280;
  private static final int LEXCODE=290;
  private static final int CHARS=291;

  private class TokenDef {
    int token;
    String name;
    String fullName;
    boolean reserved;

    TokenDef(String name, String fullName, int token, boolean reserved) {
      this.name = name;
      this.fullName = fullName;
      this.token = token;
      this.reserved = reserved;
    }
  }

  private TokenDef tokenDefs[] = {
    new TokenDef("EOS", "$", 0, true),
    new TokenDef("MARK", "'%%'", 256, true),
    new TokenDef("START", "'%start'", 257, true),
    new TokenDef("TOKEN", "Token", 258, true),
    new TokenDef("TYPE", "'%type'", 259, true),
    new TokenDef("UNION", "'%union'", 260, true),
    new TokenDef("TYPENAME", "Type definition", 261, true),
    new TokenDef("TERM", "'%token'", 262, true),
    new TokenDef("LEFT", "'%left'", 263, true),
    new TokenDef("RIGHT", "'%right'", 264, true),
    new TokenDef("BINARY", "'%binary'", 265, true),
    new TokenDef("ERRDEF", "'%error'", 266, true),
    new TokenDef("NUM", "Number", 267, true),
    new TokenDef("PREC", "'%prec'", 268, true),
    new TokenDef("NAME", "'%name'", 269, true),
    new TokenDef("ERROR", "Error", 270, true),
    new TokenDef("LEXER", "%lexer", 271, true),
    new TokenDef("DECLARE", "%declare", 272, true),
    new TokenDef("RX_PIPE", "'|'", 273, true),
    new TokenDef("RX_LPAR", "'('", 274, true),
    new TokenDef("RX_RPAR", "')'", 275, true),
    new TokenDef("RX_STAR", "'*'", 276, true),
    new TokenDef("RX_PLUS", "'+'", 277, true),
    new TokenDef("RX_HUH", "'?'", 278, true),
    new TokenDef("RX_ANY", "'.'", 279, true),
    new TokenDef("RX_CHARS", "Character or set of characters", 280, true),
    new TokenDef("LEXCODE", "lexical code", 290, true),
    new TokenDef("CHARS", "regular expression characters", 291, true),
    new TokenDef(";", "semicolon", 59, false),
    new TokenDef(":", "colon", 58, false),
    new TokenDef("=", "equals sign", 61, false),
    new TokenDef(",", "comma", 44, false),
    new TokenDef("|", "rule separator ('|')", 124, false),
    new TokenDef("(", "opening parenthesis", 40, false),
    new TokenDef(")", "closing parenthesis", 41, false),
    new TokenDef("/", "regular expression marker ('/')", 47, false),
    new TokenDef("{", "{", 123, false)
  };

  private static final int FINAL=99;
  private static final int SYMBS=64;

  private static final int ACCEPT=Integer.MAX_VALUE;

  // Parsing Table
  private int parsingTable[][] = {
          //       $     %% %start  Token  %type %union Type d %token  %left %right %binar %error Number  %prec  %name  Error %lexer %decla      |      (      )      *      +      ?      . Charac lexica regula semico  colon equals  comma rule s openin closin regula      { Number a toke ErrInf lexica  basic operat regula declar a decl code s token, parser a toke Descri DeclMa Produc $code- Tokens TokenN PrecDe Defini Equals   Type Preced  Slash  Rules   Rule 
     /*  0*/ {     0,     5,     7,     0,     9,    12,     0,    16,    17,    18,    19,    20,     0,     0,    11,     0,    15,    10,     0,     0,     0,     0,     0,     0,     0,     0,     8,     0,     6,     0,     0,     0,     0,     0,     0,     0,    14,     0,     0,     0,     0,     0,     0,     0,     2,     4,     0,     0,     0,     0,     1,     3,     0,     0,     0,     0,    13,     0,     0,     0,     0,     0,     0,     0},
     /*  1*/ {ACCEPT,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /*  2*/ {     0,     5,     7,     0,     9,    12,     0,    16,    17,    18,    19,    20,     0,     0,    11,     0,    15,    10,     0,     0,     0,     0,     0,     0,     0,     0,     8,     0,     6,     0,     0,     0,     0,     0,     0,     0,    14,     0,     0,     0,     0,     0,     0,     0,     0,    22,     0,     0,     0,     0,     0,    21,     0,     0,     0,     0,    13,     0,     0,     0,     0,     0,     0,     0},
     /*  3*/ {     0,     0,     0,    24,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,    23,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /*  4*/ {     0,    -7,    -7,    -7,    -7,    -7,    -7,    -7,    -7,    -7,    -7,    -7,     0,     0,    -7,     0,    -7,    -7,     0,     0,     0,     0,     0,     0,     0,     0,    -7,     0,    -7,    -7,    -7,     0,     0,     0,     0,     0,    -7,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /*  5*/ {     0,     0,     0,    -3,     0,     0,     0,     0,     0,     0,     0,     0,     0,    -3,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,    -3,    -3,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /*  6*/ {     0,    -8,    -8,    -8,    -8,    -8,    -8,    -8,    -8,    -8,    -8,    -8,     0,     0,    -8,     0,    -8,    -8,     0,     0,     0,     0,     0,     0,     0,     0,    -8,     0,    -8,    -8,    -8,     0,     0,     0,     0,     0,    -8,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /*  7*/ {     0,     0,     0,    25,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /*  8*/ {     0,     0,     0,    26,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /*  9*/ {     0,     0,     0,     0,     0,     0,    27,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 10*/ {     0,     0,     0,     0,     0,     0,    28,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 11*/ {     0,     0,     0,    31,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,    30,     0,     0,     0,     0,     0,    29,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 12*/ {     0,   -15,   -15,   -15,   -15,   -15,   -15,   -15,   -15,   -15,   -15,   -15,     0,     0,   -15,     0,   -15,   -15,     0,     0,     0,     0,     0,     0,     0,     0,   -15,     0,   -15,   -15,   -15,     0,     0,     0,     0,     0,   -15,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 13*/ {     0,     0,     0,   -53,     0,     0,    34,     0,     0,     0,     0,     0,   -53,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,   -53,   -53,     0,     0,     0,     0,   -53,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,    32,     0,    33,     0,     0,     0,     0},
     /* 14*/ {     0,   -17,   -17,   -17,   -17,   -17,   -17,   -17,   -17,   -17,   -17,   -17,     0,     0,   -17,     0,   -17,   -17,     0,     0,     0,     0,     0,     0,     0,     0,   -17,     0,   -17,   -17,   -17,     0,     0,     0,     0,     0,   -17,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 15*/ {     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,    36,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,    35,     0,     0,     0,     0,     0},
     /* 16*/ {     0,     0,     0,   -19,     0,     0,   -19,     0,     0,     0,     0,     0,   -19,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,   -19,   -19,     0,     0,     0,     0,   -19,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 17*/ {     0,     0,     0,   -20,     0,     0,   -20,     0,     0,     0,     0,     0,   -20,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,   -20,   -20,     0,     0,     0,     0,   -20,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 18*/ {     0,     0,     0,   -21,     0,     0,   -21,     0,     0,     0,     0,     0,   -21,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,   -21,   -21,     0,     0,     0,     0,   -21,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 19*/ {     0,     0,     0,   -22,     0,     0,   -22,     0,     0,     0,     0,     0,   -22,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,   -22,   -22,     0,     0,     0,     0,   -22,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 20*/ {     0,     0,     0,   -23,     0,     0,   -23,     0,     0,     0,     0,     0,   -23,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,   -23,   -23,     0,     0,     0,     0,   -23,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 21*/ {     0,     0,     0,    24,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,    37,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 22*/ {     0,    -6,    -6,    -6,    -6,    -6,    -6,    -6,    -6,    -6,    -6,    -6,     0,     0,    -6,     0,    -6,    -6,     0,     0,     0,     0,     0,     0,     0,     0,    -6,     0,    -6,    -6,    -6,     0,     0,     0,     0,     0,    -6,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 23*/ {    -4,    40,     0,    39,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,    38,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 24*/ {     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,    41,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 25*/ {     0,    -9,    -9,    -9,    -9,    -9,    -9,    -9,    -9,    -9,    -9,    -9,     0,     0,    -9,     0,    -9,    -9,     0,     0,     0,     0,     0,     0,     0,     0,    -9,     0,    -9,    -9,    -9,     0,     0,     0,     0,     0,    -9,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 26*/ {     0,   -10,   -10,   -10,   -10,   -10,   -10,   -10,   -10,   -10,   -10,   -10,     0,     0,   -10,     0,   -10,   -10,     0,     0,     0,     0,     0,     0,     0,     0,   -10,     0,   -10,   -10,   -10,     0,     0,     0,     0,     0,   -10,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 27*/ {     0,     0,     0,   -11,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,    42,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 28*/ {     0,   -13,   -13,   -13,   -13,   -13,   -13,   -13,   -13,   -13,   -13,   -13,     0,     0,   -13,     0,   -13,   -13,     0,     0,     0,     0,     0,     0,     0,     0,   -13,     0,   -13,   -13,   -13,     0,     0,     0,     0,     0,   -13,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 29*/ {     0,   -14,   -14,   -14,   -14,   -14,   -14,   -14,   -14,   -14,   -14,   -14,     0,     0,   -14,     0,   -14,   -14,     0,     0,     0,     0,     0,     0,     0,     0,   -14,     0,   -14,   -14,   -14,    43,     0,     0,     0,     0,   -14,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 30*/ {     0,   -27,   -27,   -27,   -27,   -27,   -27,   -27,   -27,   -27,   -27,   -27,     0,     0,   -27,     0,   -27,   -27,     0,     0,     0,     0,     0,     0,     0,     0,   -27,     0,   -27,   -27,   -27,   -27,     0,     0,     0,     0,   -27,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 31*/ {     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,    44,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 32*/ {     0,   -16,   -16,   -16,   -16,   -16,   -16,   -16,   -16,   -16,   -16,   -16,     0,     0,   -16,     0,   -16,   -16,     0,     0,     0,     0,     0,     0,     0,     0,   -16,     0,   -16,   -16,   -16,     0,     0,     0,     0,     0,   -16,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 33*/ {     0,     0,     0,    47,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,    46,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,    45,     0,     0,     0},
     /* 34*/ {     0,     0,     0,   -52,     0,     0,     0,     0,     0,     0,     0,     0,   -52,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,   -52,   -52,     0,     0,     0,     0,   -52,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 35*/ {     0,     0,     0,    48,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 36*/ {     0,     0,     0,   -51,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 37*/ {    -4,    40,     0,    39,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,    49,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 38*/ {    -2,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 39*/ {     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,    50,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 40*/ {    -5,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 41*/ {     0,     0,     0,    55,     0,     0,     0,     0,     0,     0,     0,     0,     0,    56,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,   -59,     0,    57,     0,   -59,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,    54,    52,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,    51,    53},
     /* 42*/ {     0,     0,     0,    59,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,    58,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 43*/ {     0,     0,     0,    31,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,    60,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 44*/ {     0,     0,     0,    61,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 45*/ {     0,   -29,   -29,   -29,   -29,   -29,   -29,   -29,   -29,   -29,   -29,   -29,     0,     0,   -29,     0,   -29,   -29,     0,     0,     0,     0,     0,     0,     0,     0,   -29,     0,   -29,   -29,   -29,    62,     0,     0,     0,     0,   -29,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 46*/ {     0,   -31,   -31,   -31,   -31,   -31,   -31,   -31,   -31,   -31,   -31,   -31,     0,     0,   -31,     0,   -31,   -31,     0,     0,     0,     0,     0,     0,     0,     0,   -31,     0,   -31,   -31,   -31,   -31,     0,     0,     0,     0,   -31,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 47*/ {     0,   -34,   -34,   -34,   -34,   -34,   -34,   -34,   -34,   -34,   -34,   -34,    64,     0,   -34,     0,   -34,   -34,     0,     0,     0,     0,     0,     0,     0,     0,   -34,     0,   -34,   -34,   -34,   -34,     0,     0,     0,   -34,   -34,    63,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 48*/ {     0,   -18,   -18,   -18,   -18,   -18,   -18,   -18,   -18,   -18,   -18,   -18,     0,     0,   -18,     0,   -18,   -18,     0,     0,     0,     0,     0,     0,     0,     0,   -18,     0,   -18,   -18,   -18,     0,     0,     0,     0,     0,   -18,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 49*/ {    -1,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 50*/ {     0,     0,     0,    55,     0,     0,     0,     0,     0,     0,     0,     0,     0,    56,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,   -59,     0,    57,     0,   -59,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,    54,    52,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,    65,    53},
     /* 51*/ {     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,    66,     0,     0,     0,    67,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 52*/ {     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,   -57,     0,     0,     0,   -57,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 53*/ {     0,     0,     0,    55,     0,     0,     0,     0,     0,     0,     0,     0,     0,    56,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,   -58,     0,    57,     0,   -58,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,    68,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 54*/ {     0,     0,     0,   -61,     0,     0,     0,     0,     0,     0,     0,     0,     0,   -61,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,   -61,     0,   -61,     0,   -61,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 55*/ {     0,     0,     0,   -62,     0,     0,     0,     0,     0,     0,     0,     0,     0,   -62,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,   -62,     0,   -62,     0,   -62,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 56*/ {     0,     0,     0,    69,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 57*/ {     0,     0,     0,   -64,     0,     0,     0,     0,     0,     0,     0,     0,     0,   -64,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,   -64,     0,   -64,     0,   -64,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 58*/ {     0,   -12,   -12,   -12,   -12,   -12,   -12,   -12,   -12,   -12,   -12,   -12,     0,     0,   -12,     0,   -12,   -12,     0,     0,     0,     0,     0,     0,     0,     0,   -12,     0,   -12,   -12,   -12,    70,     0,     0,     0,     0,   -12,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 59*/ {     0,   -25,   -25,   -25,   -25,   -25,   -25,   -25,   -25,   -25,   -25,   -25,     0,     0,   -25,     0,   -25,   -25,     0,     0,     0,     0,     0,     0,     0,     0,   -25,     0,   -25,   -25,   -25,   -25,     0,     0,     0,     0,   -25,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 60*/ {     0,   -26,   -26,   -26,   -26,   -26,   -26,   -26,   -26,   -26,   -26,   -26,     0,     0,   -26,     0,   -26,   -26,     0,     0,     0,     0,     0,     0,     0,     0,   -26,     0,   -26,   -26,   -26,   -26,     0,     0,     0,     0,   -26,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 61*/ {     0,   -28,   -28,   -28,   -28,   -28,   -28,   -28,   -28,   -28,   -28,   -28,     0,     0,   -28,     0,   -28,   -28,     0,     0,     0,     0,     0,     0,     0,     0,   -28,     0,   -28,   -28,   -28,   -28,     0,     0,     0,     0,   -28,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 62*/ {     0,     0,     0,    47,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,    71,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 63*/ {     0,   -36,   -36,   -36,   -36,   -36,   -36,   -36,   -36,   -36,   -36,   -36,     0,     0,   -36,     0,   -36,   -36,     0,     0,     0,     0,     0,     0,     0,     0,   -36,     0,   -36,    73,   -36,   -36,     0,     0,     0,   -36,   -36,     0,     0,    72,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 64*/ {     0,   -33,   -33,   -33,   -33,   -33,   -33,   -33,   -33,   -33,   -33,   -33,     0,     0,   -33,     0,   -33,   -33,     0,     0,     0,     0,     0,     0,     0,     0,   -33,     0,   -33,   -33,   -33,   -33,     0,     0,     0,   -33,   -33,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 65*/ {     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,    74,     0,     0,     0,    67,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 66*/ {   -55,   -55,     0,   -55,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 67*/ {     0,     0,     0,    55,     0,     0,     0,     0,     0,     0,     0,     0,     0,    56,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,   -59,     0,    57,     0,   -59,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,    54,    75,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,    53},
     /* 68*/ {     0,     0,     0,   -60,     0,     0,     0,     0,     0,     0,     0,     0,     0,   -60,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,   -60,     0,   -60,     0,   -60,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 69*/ {     0,     0,     0,   -63,     0,     0,     0,     0,     0,     0,     0,     0,     0,   -63,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,   -63,     0,   -63,     0,   -63,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 70*/ {     0,     0,     0,    76,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 71*/ {     0,   -30,   -30,   -30,   -30,   -30,   -30,   -30,   -30,   -30,   -30,   -30,     0,     0,   -30,     0,   -30,   -30,     0,     0,     0,     0,     0,     0,     0,     0,   -30,     0,   -30,   -30,   -30,   -30,     0,     0,     0,     0,   -30,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 72*/ {     0,   -39,   -39,   -39,   -39,   -39,   -39,   -39,   -39,   -39,   -39,   -39,     0,     0,   -39,     0,   -39,   -39,     0,     0,     0,     0,     0,     0,     0,     0,   -39,     0,   -39,   -39,    36,   -39,     0,     0,     0,    80,   -39,     0,     0,     0,    77,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,    78,     0,     0,    79,     0,     0},
     /* 73*/ {     0,     0,     0,    81,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 74*/ {   -54,   -54,     0,   -54,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 75*/ {     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,   -56,     0,     0,     0,   -56,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 76*/ {     0,   -24,   -24,   -24,   -24,   -24,   -24,   -24,   -24,   -24,   -24,   -24,     0,     0,   -24,     0,   -24,   -24,     0,     0,     0,     0,     0,     0,     0,     0,   -24,     0,   -24,   -24,   -24,   -24,     0,     0,     0,     0,   -24,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 77*/ {     0,   -32,   -32,   -32,   -32,   -32,   -32,   -32,   -32,   -32,   -32,   -32,     0,     0,   -32,     0,   -32,   -32,     0,     0,     0,     0,     0,     0,     0,     0,   -32,     0,   -32,   -32,   -32,   -32,     0,     0,     0,     0,   -32,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 78*/ {     0,     0,     0,    82,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 79*/ {     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,    86,     0,     0,     0,     0,    88,     0,     0,    87,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,    85,    84,    83,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 80*/ {     0,     0,     0,    89,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 81*/ {     0,   -35,   -35,   -35,   -35,   -35,   -35,   -35,   -35,   -35,   -35,   -35,     0,     0,   -35,     0,   -35,   -35,     0,     0,     0,     0,     0,     0,     0,     0,   -35,     0,   -35,   -35,   -35,   -35,     0,     0,     0,   -35,   -35,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 82*/ {     0,   -37,   -37,   -37,   -37,   -37,   -37,   -37,   -37,   -37,   -37,   -37,     0,     0,   -37,     0,   -37,   -37,     0,     0,     0,     0,     0,     0,     0,     0,   -37,     0,   -37,   -37,   -37,   -37,     0,     0,     0,     0,   -37,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 83*/ {     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,    92,    86,     0,     0,     0,     0,    88,     0,     0,    87,     0,     0,     0,     0,     0,     0,     0,    80,     0,     0,     0,     0,     0,    85,    91,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,    90,     0,     0},
     /* 84*/ {     0,     0,     0,   -43,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,   -43,   -43,   -43,     0,     0,     0,   -43,     0,     0,   -43,     0,     0,     0,     0,     0,     0,     0,   -43,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 85*/ {     0,     0,     0,   -47,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,   -47,   -47,     0,    93,    94,    95,   -47,     0,     0,   -47,     0,     0,     0,     0,     0,     0,     0,   -47,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 86*/ {     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,    86,     0,     0,     0,     0,    88,     0,     0,    87,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,    85,    84,    96,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 87*/ {     0,     0,     0,   -49,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,   -49,   -49,   -49,   -49,   -49,   -49,   -49,     0,     0,   -49,     0,     0,     0,     0,     0,     0,     0,   -49,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 88*/ {     0,     0,     0,   -50,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,   -50,   -50,   -50,   -50,   -50,   -50,   -50,     0,     0,   -50,     0,     0,     0,     0,     0,     0,     0,   -50,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 89*/ {     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,   -40,     0,     0,     0,     0,   -40,     0,     0,   -40,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 90*/ {     0,   -38,   -38,   -38,   -38,   -38,   -38,   -38,   -38,   -38,   -38,   -38,     0,     0,   -38,     0,   -38,   -38,     0,     0,     0,     0,     0,     0,     0,     0,   -38,     0,   -38,   -38,   -38,   -38,     0,     0,     0,     0,   -38,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 91*/ {     0,     0,     0,   -41,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,   -41,   -41,   -41,     0,     0,     0,   -41,     0,     0,   -41,     0,     0,     0,     0,     0,     0,     0,   -41,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 92*/ {     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,    86,     0,     0,     0,     0,    88,     0,     0,    87,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,    85,    97,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 93*/ {     0,     0,     0,   -44,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,   -44,   -44,   -44,     0,     0,     0,   -44,     0,     0,   -44,     0,     0,     0,     0,     0,     0,     0,   -44,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 94*/ {     0,     0,     0,   -45,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,   -45,   -45,     0,     0,     0,     0,   -45,     0,     0,   -45,     0,     0,     0,     0,     0,     0,     0,   -45,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 95*/ {     0,     0,     0,   -46,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,   -46,   -46,     0,     0,     0,     0,   -46,     0,     0,   -46,     0,     0,     0,     0,     0,     0,     0,   -46,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 96*/ {     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,    92,    86,    98,     0,     0,     0,    88,     0,     0,    87,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,    85,    91,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 97*/ {     0,     0,     0,   -42,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,   -42,   -42,   -42,     0,     0,     0,   -42,     0,     0,   -42,     0,     0,     0,     0,     0,     0,     0,   -42,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0},
     /* 98*/ {     0,     0,     0,   -48,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,   -48,   -48,   -48,   -48,   -48,   -48,   -48,     0,     0,   -48,     0,     0,     0,     0,     0,     0,     0,   -48,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0}
  };
  
// Parsing Errors
  private int parsingError[] = {
     /*   0 */ 0, // Expecting declarations
     /*   1 */ 1, // No more elements expected
     /*   2 */ 2, // Expecting a declaration
     /*   3 */ 3, // Token expected
     /*   4 */ 4, // One of the symbols '%%', '%start', Token, '%type', '%union', Type definition, '%token', '%left', '%right', '%binary', '%error', '%name', %lexer, %declare, lexical code, semicolon, colon, equals sign, { may be missing
     /*   5 */ 5, // One of the symbols Token, '%prec', colon, equals sign may be missing
     /*   6 */ 4, // One of the symbols '%%', '%start', Token, '%type', '%union', Type definition, '%token', '%left', '%right', '%binary', '%error', '%name', %lexer, %declare, lexical code, semicolon, colon, equals sign, { may be missing
     /*   7 */ 3, // Token expected
     /*   8 */ 3, // Token expected
     /*   9 */ 6, // Type definition expected
     /*  10 */ 6, // Type definition expected
     /*  11 */ 3, // Token expected
     /*  12 */ 4, // One of the symbols '%%', '%start', Token, '%type', '%union', Type definition, '%token', '%left', '%right', '%binary', '%error', '%name', %lexer, %declare, lexical code, semicolon, colon, equals sign, { may be missing
     /*  13 */ 6, // Type definition expected
     /*  14 */ 4, // One of the symbols '%%', '%start', Token, '%type', '%union', Type definition, '%token', '%left', '%right', '%binary', '%error', '%name', %lexer, %declare, lexical code, semicolon, colon, equals sign, { may be missing
     /*  15 */ 7, // equals sign expected
     /*  16 */ 8, // One of the symbols Token, Type definition, Number, colon, equals sign, regular expression marker ('/') may be missing
     /*  17 */ 8, // One of the symbols Token, Type definition, Number, colon, equals sign, regular expression marker ('/') may be missing
     /*  18 */ 8, // One of the symbols Token, Type definition, Number, colon, equals sign, regular expression marker ('/') may be missing
     /*  19 */ 8, // One of the symbols Token, Type definition, Number, colon, equals sign, regular expression marker ('/') may be missing
     /*  20 */ 8, // One of the symbols Token, Type definition, Number, colon, equals sign, regular expression marker ('/') may be missing
     /*  21 */ 3, // Token expected
     /*  22 */ 4, // One of the symbols '%%', '%start', Token, '%type', '%union', Type definition, '%token', '%left', '%right', '%binary', '%error', '%name', %lexer, %declare, lexical code, semicolon, colon, equals sign, { may be missing
     /*  23 */ 9, // Expecting code section
     /*  24 */ 10, // colon expected
     /*  25 */ 4, // One of the symbols '%%', '%start', Token, '%type', '%union', Type definition, '%token', '%left', '%right', '%binary', '%error', '%name', %lexer, %declare, lexical code, semicolon, colon, equals sign, { may be missing
     /*  26 */ 4, // One of the symbols '%%', '%start', Token, '%type', '%union', Type definition, '%token', '%left', '%right', '%binary', '%error', '%name', %lexer, %declare, lexical code, semicolon, colon, equals sign, { may be missing
     /*  27 */ 11, // Expecting $code-fragment-1
     /*  28 */ 4, // One of the symbols '%%', '%start', Token, '%type', '%union', Type definition, '%token', '%left', '%right', '%binary', '%error', '%name', %lexer, %declare, lexical code, semicolon, colon, equals sign, { may be missing
     /*  29 */ 12, // comma expected
     /*  30 */ 13, // One of the symbols '%%', '%start', Token, '%type', '%union', Type definition, '%token', '%left', '%right', '%binary', '%error', '%name', %lexer, %declare, lexical code, semicolon, colon, equals sign, comma, { may be missing
     /*  31 */ 10, // colon expected
     /*  32 */ 4, // One of the symbols '%%', '%start', Token, '%type', '%union', Type definition, '%token', '%left', '%right', '%binary', '%error', '%name', %lexer, %declare, lexical code, semicolon, colon, equals sign, { may be missing
     /*  33 */ 3, // Token expected
     /*  34 */ 14, // One of the symbols Token, Number, colon, equals sign, regular expression marker ('/') may be missing
     /*  35 */ 3, // Token expected
     /*  36 */ 3, // Token expected
     /*  37 */ 9, // Expecting code section
     /*  38 */ 15, // $ expected
     /*  39 */ 10, // colon expected
     /*  40 */ 15, // $ expected
     /*  41 */ 16, // Token, '%prec' or equals sign expected
     /*  42 */ 3, // Token expected
     /*  43 */ 3, // Token expected
     /*  44 */ 3, // Token expected
     /*  45 */ 12, // comma expected
     /*  46 */ 13, // One of the symbols '%%', '%start', Token, '%type', '%union', Type definition, '%token', '%left', '%right', '%binary', '%error', '%name', %lexer, %declare, lexical code, semicolon, colon, equals sign, comma, { may be missing
     /*  47 */ 17, // Number expected
     /*  48 */ 4, // One of the symbols '%%', '%start', Token, '%type', '%union', Type definition, '%token', '%left', '%right', '%binary', '%error', '%name', %lexer, %declare, lexical code, semicolon, colon, equals sign, { may be missing
     /*  49 */ 15, // $ expected
     /*  50 */ 16, // Token, '%prec' or equals sign expected
     /*  51 */ 18, // semicolon or rule separator ('|') expected
     /*  52 */ 19, // One of the symbols semicolon, rule separator ('|') may be missing
     /*  53 */ 20, // Expecting token, precedence declaration or '='
     /*  54 */ 21, // One of the symbols Token, '%prec', semicolon, equals sign, rule separator ('|') may be missing
     /*  55 */ 21, // One of the symbols Token, '%prec', semicolon, equals sign, rule separator ('|') may be missing
     /*  56 */ 3, // Token expected
     /*  57 */ 21, // One of the symbols Token, '%prec', semicolon, equals sign, rule separator ('|') may be missing
     /*  58 */ 12, // comma expected
     /*  59 */ 13, // One of the symbols '%%', '%start', Token, '%type', '%union', Type definition, '%token', '%left', '%right', '%binary', '%error', '%name', %lexer, %declare, lexical code, semicolon, colon, equals sign, comma, { may be missing
     /*  60 */ 13, // One of the symbols '%%', '%start', Token, '%type', '%union', Type definition, '%token', '%left', '%right', '%binary', '%error', '%name', %lexer, %declare, lexical code, semicolon, colon, equals sign, comma, { may be missing
     /*  61 */ 13, // One of the symbols '%%', '%start', Token, '%type', '%union', Type definition, '%token', '%left', '%right', '%binary', '%error', '%name', %lexer, %declare, lexical code, semicolon, colon, equals sign, comma, { may be missing
     /*  62 */ 3, // Token expected
     /*  63 */ 10, // colon expected
     /*  64 */ 22, // One of the symbols '%%', '%start', Token, '%type', '%union', Type definition, '%token', '%left', '%right', '%binary', '%error', '%name', %lexer, %declare, lexical code, semicolon, colon, equals sign, comma, regular expression marker ('/'), { may be missing
     /*  65 */ 18, // semicolon or rule separator ('|') expected
     /*  66 */ 23, // One of the symbols $, '%%', Token may be missing
     /*  67 */ 20, // Expecting token, precedence declaration or '='
     /*  68 */ 21, // One of the symbols Token, '%prec', semicolon, equals sign, rule separator ('|') may be missing
     /*  69 */ 21, // One of the symbols Token, '%prec', semicolon, equals sign, rule separator ('|') may be missing
     /*  70 */ 3, // Token expected
     /*  71 */ 13, // One of the symbols '%%', '%start', Token, '%type', '%union', Type definition, '%token', '%left', '%right', '%binary', '%error', '%name', %lexer, %declare, lexical code, semicolon, colon, equals sign, comma, { may be missing
     /*  72 */ 24, // equals sign or regular expression marker ('/') expected
     /*  73 */ 3, // Token expected
     /*  74 */ 23, // One of the symbols $, '%%', Token may be missing
     /*  75 */ 19, // One of the symbols semicolon, rule separator ('|') may be missing
     /*  76 */ 13, // One of the symbols '%%', '%start', Token, '%type', '%union', Type definition, '%token', '%left', '%right', '%binary', '%error', '%name', %lexer, %declare, lexical code, semicolon, colon, equals sign, comma, { may be missing
     /*  77 */ 13, // One of the symbols '%%', '%start', Token, '%type', '%union', Type definition, '%token', '%left', '%right', '%binary', '%error', '%name', %lexer, %declare, lexical code, semicolon, colon, equals sign, comma, { may be missing
     /*  78 */ 3, // Token expected
     /*  79 */ 25, // Expecting basic element
     /*  80 */ 3, // Token expected
     /*  81 */ 22, // One of the symbols '%%', '%start', Token, '%type', '%union', Type definition, '%token', '%left', '%right', '%binary', '%error', '%name', %lexer, %declare, lexical code, semicolon, colon, equals sign, comma, regular expression marker ('/'), { may be missing
     /*  82 */ 13, // One of the symbols '%%', '%start', Token, '%type', '%union', Type definition, '%token', '%left', '%right', '%binary', '%error', '%name', %lexer, %declare, lexical code, semicolon, colon, equals sign, comma, { may be missing
     /*  83 */ 25, // Expecting basic element
     /*  84 */ 26, // One of the symbols Token, '|', '(', ')', '.', regular expression characters, regular expression marker ('/') may be missing
     /*  85 */ 27, // '*', '+' or '?' expected
     /*  86 */ 25, // Expecting basic element
     /*  87 */ 28, // One of the symbols Token, '|', '(', ')', '*', '+', '?', '.', regular expression characters, regular expression marker ('/') may be missing
     /*  88 */ 28, // One of the symbols Token, '|', '(', ')', '*', '+', '?', '.', regular expression characters, regular expression marker ('/') may be missing
     /*  89 */ 29, // One of the symbols '(', '.', regular expression characters may be missing
     /*  90 */ 13, // One of the symbols '%%', '%start', Token, '%type', '%union', Type definition, '%token', '%left', '%right', '%binary', '%error', '%name', %lexer, %declare, lexical code, semicolon, colon, equals sign, comma, { may be missing
     /*  91 */ 26, // One of the symbols Token, '|', '(', ')', '.', regular expression characters, regular expression marker ('/') may be missing
     /*  92 */ 25, // Expecting basic element
     /*  93 */ 26, // One of the symbols Token, '|', '(', ')', '.', regular expression characters, regular expression marker ('/') may be missing
     /*  94 */ 30, // One of the symbols Token, '|', '(', '.', regular expression characters, regular expression marker ('/') may be missing
     /*  95 */ 30, // One of the symbols Token, '|', '(', '.', regular expression characters, regular expression marker ('/') may be missing
     /*  96 */ 25, // Expecting basic element
     /*  97 */ 26, // One of the symbols Token, '|', '(', ')', '.', regular expression characters, regular expression marker ('/') may be missing
     /*  98 */ 28  // One of the symbols Token, '|', '(', ')', '*', '+', '?', '.', regular expression characters, regular expression marker ('/') may be missing
  };

  // Error Messages
  private String errorTable[] = {
     /* 0 */ "Expecting declarations",
     /* 1 */ "No more elements expected",
     /* 2 */ "Expecting a declaration",
     /* 3 */ "Token expected",
     /* 4 */ "One of the symbols '%%', '%start', Token, '%type', '%union', Type definition, '%token', '%left', '%right', '%binary', '%error', '%name', %lexer, %declare, lexical code, semicolon, colon, equals sign, { may be missing",
     /* 5 */ "One of the symbols Token, '%prec', colon, equals sign may be missing",
     /* 6 */ "Type definition expected",
     /* 7 */ "equals sign expected",
     /* 8 */ "One of the symbols Token, Type definition, Number, colon, equals sign, regular expression marker ('/') may be missing",
     /* 9 */ "Expecting code section",
     /* 10 */ "colon expected",
     /* 11 */ "Expecting $code-fragment-1",
     /* 12 */ "comma expected",
     /* 13 */ "One of the symbols '%%', '%start', Token, '%type', '%union', Type definition, '%token', '%left', '%right', '%binary', '%error', '%name', %lexer, %declare, lexical code, semicolon, colon, equals sign, comma, { may be missing",
     /* 14 */ "One of the symbols Token, Number, colon, equals sign, regular expression marker ('/') may be missing",
     /* 15 */ "$ expected",
     /* 16 */ "Token, '%prec' or equals sign expected",
     /* 17 */ "Number expected",
     /* 18 */ "semicolon or rule separator ('|') expected",
     /* 19 */ "One of the symbols semicolon, rule separator ('|') may be missing",
     /* 20 */ "Expecting token, precedence declaration or '='",
     /* 21 */ "One of the symbols Token, '%prec', semicolon, equals sign, rule separator ('|') may be missing",
     /* 22 */ "One of the symbols '%%', '%start', Token, '%type', '%union', Type definition, '%token', '%left', '%right', '%binary', '%error', '%name', %lexer, %declare, lexical code, semicolon, colon, equals sign, comma, regular expression marker ('/'), { may be missing",
     /* 23 */ "One of the symbols $, '%%', Token may be missing",
     /* 24 */ "equals sign or regular expression marker ('/') expected",
     /* 25 */ "Expecting basic element",
     /* 26 */ "One of the symbols Token, '|', '(', ')', '.', regular expression characters, regular expression marker ('/') may be missing",
     /* 27 */ "'*', '+' or '?' expected",
     /* 28 */ "One of the symbols Token, '|', '(', ')', '*', '+', '?', '.', regular expression characters, regular expression marker ('/') may be missing",
     /* 29 */ "One of the symbols '(', '.', regular expression characters may be missing",
     /* 30 */ "One of the symbols Token, '|', '(', '.', regular expression characters, regular expression marker ('/') may be missing"
  };

  // symbols and reductions table
  private class Grammar {
    int symbol;
    int reductions;

    Grammar(int symbol, int reductions) {
      this.symbol = symbol;
      this.reductions = reductions;
    }
  }

  private Grammar grammarTable[]={
    /*Rule   0 */ new Grammar(    64,      1),
    /*Rule   1 */ new Grammar(    50,      4),
    /*Rule   2 */ new Grammar(    50,      3),
    /*Rule   3 */ new Grammar(    51,      1),
    /*Rule   4 */ new Grammar(    46,      0),
    /*Rule   5 */ new Grammar(    46,      1),
    /*Rule   6 */ new Grammar(    44,      2),
    /*Rule   7 */ new Grammar(    44,      1),
    /*Rule   8 */ new Grammar(    45,      1),
    /*Rule   9 */ new Grammar(    45,      2),
    /*Rule  10 */ new Grammar(    45,      2),
    /*Rule  11 */ new Grammar(    53,      0),
    /*Rule  12 */ new Grammar(    45,      4),
    /*Rule  13 */ new Grammar(    45,      2),
    /*Rule  14 */ new Grammar(    45,      2),
    /*Rule  15 */ new Grammar(    45,      1),
    /*Rule  16 */ new Grammar(    45,      2),
    /*Rule  17 */ new Grammar(    45,      1),
    /*Rule  18 */ new Grammar(    45,      3),
    /*Rule  19 */ new Grammar(    56,      1),
    /*Rule  20 */ new Grammar(    56,      1),
    /*Rule  21 */ new Grammar(    56,      1),
    /*Rule  22 */ new Grammar(    56,      1),
    /*Rule  23 */ new Grammar(    56,      1),
    /*Rule  24 */ new Grammar(    54,      3),
    /*Rule  25 */ new Grammar(    54,      1),
    /*Rule  26 */ new Grammar(    55,      3),
    /*Rule  27 */ new Grammar(    55,      1),
    /*Rule  28 */ new Grammar(    49,      3),
    /*Rule  29 */ new Grammar(    57,      2),
    /*Rule  30 */ new Grammar(    60,      3),
    /*Rule  31 */ new Grammar(    60,      1),
    /*Rule  32 */ new Grammar(    38,      4),
    /*Rule  33 */ new Grammar(    37,      1),
    /*Rule  34 */ new Grammar(    37,      0),
    /*Rule  35 */ new Grammar(    39,      2),
    /*Rule  36 */ new Grammar(    39,      0),
    /*Rule  37 */ new Grammar(    40,      2),
    /*Rule  38 */ new Grammar(    40,      3),
    /*Rule  39 */ new Grammar(    40,      0),
    /*Rule  40 */ new Grammar(    61,      2),
    /*Rule  41 */ new Grammar(    43,      2),
    /*Rule  42 */ new Grammar(    43,      3),
    /*Rule  43 */ new Grammar(    43,      1),
    /*Rule  44 */ new Grammar(    42,      2),
    /*Rule  45 */ new Grammar(    42,      2),
    /*Rule  46 */ new Grammar(    42,      2),
    /*Rule  47 */ new Grammar(    42,      1),
    /*Rule  48 */ new Grammar(    41,      3),
    /*Rule  49 */ new Grammar(    41,      1),
    /*Rule  50 */ new Grammar(    41,      1),
    /*Rule  51 */ new Grammar(    58,      1),
    /*Rule  52 */ new Grammar(    59,      1),
    /*Rule  53 */ new Grammar(    59,      0),
    /*Rule  54 */ new Grammar(    52,      5),
    /*Rule  55 */ new Grammar(    52,      4),
    /*Rule  56 */ new Grammar(    62,      3),
    /*Rule  57 */ new Grammar(    62,      1),
    /*Rule  58 */ new Grammar(    48,      1),
    /*Rule  59 */ new Grammar(    48,      0),
    /*Rule  60 */ new Grammar(    63,      2),
    /*Rule  61 */ new Grammar(    63,      1),
    /*Rule  62 */ new Grammar(    47,      1),
    /*Rule  63 */ new Grammar(    47,      2),
    /*Rule  64 */ new Grammar(    47,      1)
  };

  private final int NON_TERMINALS=28;
  private final int nonTerminals[] = {
    37,// Number
    38,// Precedence
    39,// ErrInfo
    40,// LexicAction
    41,// BasicElement
    42,// ConcatElement
    43,// RegExp
    44,// Declarations
    45,// Declaration
    46,// Actions
    47,// Symbol
    48,// GrammarRule
    49,// Name
    50,// Descriptor
    51,// DeclMarker
    52,// Productions
    53,// $code-fragment-1
    54,// Tokens
    55,// TokenNames
    56,// PrecDef
    57,// Definition
    58,// Equals
    59,// Type
    60,// Precedences
    61,// Slash
    62,// Rules
    63,// Rule
    
    64 // $start
  };

  /*
   *
   * Begin of Skeleton
   *
   */

  /* ****************************************************************
    Java Skeleton Parser for matrix tables

    This is not a sample program, but rather the parser skeleton
    to be included in the generated code.
    Modify at your own risk.

    Copyright (c), 1985-2012 Jaime Garza
  ***************************************************************** */

  private static final int ERROR_FAIL = 0;
  private static final int ERROR_RE_ATTEMPT = 1;

  private int    stateStack[] = new int[STACK_DEPTH];
  int            state;
  StackElement   lexicalValue;
  int            lexicalToken;
  int            errorCount;
  int            errorFlag;
  boolean        verbose = false;

  /**
   * Change the verbose flag
   */
  public void setVerbose(boolean verbose) {
    this.verbose = verbose;
  }

  /**
   * Obtain the verbose flag
   */
  public boolean isVerbose() {
    return this.verbose;
  }

  /**
   * This routine maps a state and a token to a new state on the action table
   * @param state is the current state
   * @param symbol is the given symbol to find (if not found, defa will be used
   */
  private int parserAction(int state, int symbol) {
    int index = getTokenIndex(symbol);
    return parsingTable[state][index];
  }

  /**
   * This routine maps a origin state to a destination state
   * using the symbol position
   * @param state is the current state
   *@param non terminal that causes the transition
   */
  private int parserGoto(int state, int symbol) {
    int index = symbol;
    return parsingTable[state][index];
  }

  /**
   * This routine prints the contents of the parsing stack
   */
  private void parserPrintStack() {
    int i;

    System.out.print("States: [");
    for(i=0;i<=stackTop;i++) {
      System.out.print(stateStack[i] + " ");
    }
    System.out.println("<--Top Of Stack (" + stackTop + ")");
    System.out.print("Values: [");
    for(i=0;i<=stackTop;i++) {
      System.out.print("|" + (stack[i] != null ? stack[i].toString() : "(nothing)") + "| ");
    }
    System.out.println("<--Top Of Stack (" + stackTop + ")\n");
  }

  /**
   * Does a shift operation.  Puts a new state on the top of the stack
   * @param sym is the symbol causing the shift
   * @param state is the current state
   */
  private int parserShift(int sym, int state) {
    if(stackTop >= STACK_DEPTH-1) {
      return 0;
    }

    stateStack[++stackTop] = state;
    stack[stackTop] = lexicalValue;
    this.state = state;
    if (isVerbose()) {
      System.out.println("Shift to " + state + " with " + sym);
      parserPrintStack();
    }
    return 1;
  }

  /**
   * Recognizes a rule an removes all its elements from the stack
   * @param sym is the symbol causing the shift
   * @param rule is the number of rule being used
   */
  int parserReduce(int sym, int rule) {
    if (isVerbose()) {
      System.out.println("Reduce on rule " + rule + " with symbol " + sym);
    }
    if(generateCode(rule) == false) {
      return 0;
    }
    stackTop -= grammarTable[rule].reductions;
    stateStack[stackTop+1] =
        parserGoto(stateStack[stackTop], grammarTable[rule].symbol);
    state = stateStack[++stackTop];
    if (isVerbose()) {
        parserPrintStack();
    }
    return 1;
  }

  /**
   * Get the error message for a state
   */
  private String getErrorMessage() {
    int msgIndex = parsingError[state];
    if (msgIndex >= 0) {
      return errorTable[msgIndex];
    } else {
      return "Syntax error on state " + state + " with token " + getTokenName(lexicalToken);
    }
  }

  /**
   * Recover from a syntax error removing stack states/symbols, and removing
   * input tokens.  The array StxRecover contains the tokens that bound
   * the error
   */
  private int parserRecover() {
    int i, acc;

    switch(errorFlag) {
      case 0: // 1st error
        if(parserError(state, lexicalToken, stackTop, getErrorMessage()) == 0) {
          return 0;
        }
        errorCount++;
        // continues and goes into 1 and 2.  No break on purpose

      case 1:
      case 2: // three attempts are made before dropping the current token
        errorFlag = 3; // Remove token

        while(stackTop > 0) {
          // Look if the state on the stack's top has a transition with one of
          // the recovering elements in StxRecoverTable
          for (i=0; i<RECOVERS; i++) {
            if((acc = parserAction(state, recoverTable[i])) > 0) {
              // valid shift
              return parserShift(recoverTable[i], acc);
            }
          }
          if (isVerbose()) {
            System.out.println("Recuperate removing state " + state + " and going to state " +
                            stack[stackTop-1]);
          }
          state = stateStack[--stackTop];
        }
        stackTop = 0;
        return 0;

      case 3: // I need to drop the current token
        if (isVerbose()) {
          System.out.println("Recuperate removing symbol " + lexicalToken);
        }
        if(lexicalToken == 0) { // end of file
          return 0;
        }
        lexicalToken = parserElement(false);
        return 1;
    }
    // should never reach
    System.err.println("ASSERTION FAILED ON PARSER");
    Exception e = new Exception();
    e.printStackTrace(System.err);
    return 0;
  }

  /**
   * Main parser routine, uses Shift, Reduce and Recover
   */
  public int parse() {
    int action;

    stackTop = 0;
    stateStack[0] = 0;
    stack[0] = null;
    lexicalToken = parserElement(true);
    state = 0;
    errorFlag = 0;
    errorCount = 0;

    if (isVerbose()) {
      System.out.println("Starting to parse");
      parserPrintStack();
    }

    while(2 != 1) { // forever with break and return below
      action = parserAction(state, lexicalToken);
      if(action == ACCEPT) {
        if (isVerbose()) {
          System.out.println("Program Accepted");
        }
        return 1;
      }

      if(action > 0) {
        if(parserShift(lexicalToken, action) == 0) {
          return 0;
        }
        lexicalToken = parserElement(false);
        if(errorFlag > 0) {
           errorFlag--; // properly recovering from error
        }
      } else if(action < 0) {
        if(parserReduce(lexicalToken, -action) == 0) {
          if(errorFlag == -1) {
            if(parserRecover() == 0) {
              return 0;
            }
          } else {
            return 0;
          }
        }
      } else if(action == 0) {
        if(parserRecover() == 0) {
          return 0;
        }
      }
    }
  }

  /**
   * @returns the current lexical value
   */
  public StackElement getResult() {
    return stack[stackTop];
  }

  /**
   * @param token is the number of the token
   * @returns the name of a token, given the token number
   */
  public String getTokenName(int token) {
    for (int i = 0; i < tokenDefs.length; i++) {
      if (tokenDefs[i].token == token) {
        return tokenDefs[i].name;
        }
    }
    if (token < 256) {
      return "\'" + (char) token + "\'";
    } else {
      return "UNKNOWN TOKEN";
    }
  }

  /**
   * @param token is the number of the token
   * @returns the name of a token, given the token number
   */
  public int getTokenIndex(int token) {
    for (int i = 0; i < tokenDefs.length; i++) {
      if (tokenDefs[i].token == token) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Perform a round of tokenization and dump the results
   */
  public void dumpTokens() {
    lexicalToken = parserElement(true);
    while (lexicalToken != 0) {
      System.out.println("Token: " + getTokenName(lexicalToken) + "(" + lexicalToken + "):" + lexicalValue.toString());
      lexicalToken = parserElement(false);
    }
  }

  int findReservedWord(String word) {
    for (int i = 0; i < tokenDefs.length; i++) {
      if (tokenDefs[i].reserved && tokenDefs[i].name.equals(word)) {
        return tokenDefs[i].token;
      }
    }
    return -1;
  }

  int findReservedWordIgnoreCase(String word) {
    for (int i = 0; i < tokenDefs.length; i++) {
      if (tokenDefs[i].reserved && tokenDefs[i].name.equalsIgnoreCase(word)) {
        return tokenDefs[i].token;
      }
    }
    return -1;
  }

  private static final int REGEX_MATCHED = 0;
  private static final int REGEX_NONE = 1;
  private static final int REGEX_TOOMANY = 2;

  private class RegexpMatch {
    int index;
    String matched;
    int error;

    public RegexpMatch(int index, String matched, int error) {
      this.index = index;
      this.matched = matched;
      this.error = error;
    }
    
    public String toString() {
      return "{index:" + index + ",matched:\"" + matched + "\",error:" + error + "}";
    }
  }

  /*private RegexpMatch matchRegExp() {
    String s = "";

    int candidates[] = new int[tokenDefs.length];

    for (int i = 0; i < candidates.length; i++) {
      candidates[i] = 1;
    }

    s += currentChar;

    // search which regular expressions match the first char
    int count = 0;
    int index = -1;
    int previousCount;
    int previousIndex;

    do {
      previousCount = count;
      previousIndex = index;
      count = 0;
      index = -1;
      for (int i = 0; i < tokenDefs.length; i++) {
        if (candidates[i] == 1 && tokenDefs[i].regex != null && tokenDefs[i].regex.length() > 0) {
          if (s.toString().matches(tokenDefs[i].regex)) {
            index = i;
            count++;
          } else {
            candidates[i] = -1;
          }
        }
      }

      if (count > 0) {
        s += currentChar;
      }
    } while (count > 0);

    // restore last try
    count = previousCount;
    index = previousIndex;
    s = s.substring(0, s.length()-2);
    // currentChar is OK now as I went one back internally to this function

    // see what happened
    if (count == 0) {
      // none matches
      return new RegexpMatch(-1, "", REGEX_NONE);
    } else if (count == 1) {
      return new RegexpMatch(index, s, REGEX_MATCHED);
    } else {
      return new RegexpMatch(-1, s, REGEX_TOOMANY);
    }
  }*/

/*
   *
   * End of packed skeleton for java
   *
   */




  /**
   * Get next token
   * 
   * @return the next token, changing mode as needed
   */
  protected int parserElement(boolean init) {
    int rc;
  
    if (init) {
      getNextChar(init);
    }
    if (isRegex) {
      rc = getRegexSymbol();
      if (environment.isVerbose()) {
        System.out.printf("RegexScanner: %d\n", rc);
      }
    } else {
      rc = getNormalSymbol();
      lexicalValue = new StackElement(-1, tokenNumber, mustClose, runtimeData.currentStringValue, null);
      if (environment.isDebug()) {
        System.out.printf("* StdScanner: %s(%d) {%s}\n",
            getTokenName(rc), rc, lexicalValue != null ? lexicalValue.toString() : "");
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
  protected int parserError(int state, int sym, int stackTop, String errorMessage) {
    if (errorMessage != null) {
      environment.error(-1, "Syntax error %d :\'%s\'.", state, errorMessage);
    } else {
      System.err.printf("%s(%05d) : Unknown error on state %d\n", environment.getSourceFile().toString(),
          runtimeData.lineNumber + 1, state);
    }
    isError = true;
    return 0; /*
               * with actions, it recovers weird. Need to change the action
               * stuff to the scanner
               */
  }
  
  /**
   * Get the next character
   * @param init indicates if this is the first call
   */
  public char getNextChar(boolean init) {
    return getCharacter();
  }

  @Override
  public int getRegexSymbol() {
    char c2;
  
    if (isEqual) {
      isEqual = false;
      runtimeData.currentStringValue = "";
      return TOKEN;
    }
  
    if (runtimeData.currentCharacter == '|') {
      getCharacter();
      return RX_PIPE;
    }
    if (runtimeData.currentCharacter == '(') {
      getCharacter();
      return RX_LPAR;
    }
    if (runtimeData.currentCharacter == ')') {
      getCharacter();
      return RX_RPAR;
    }
    if (runtimeData.currentCharacter == '*') {
      getCharacter();
      return RX_STAR;
    }
    if (runtimeData.currentCharacter == '+') {
      getCharacter();
      return RX_PLUS;
    }
    if (runtimeData.currentCharacter == '?') {
      getCharacter();
      return RX_HUH;
    }
    if (runtimeData.currentCharacter == '.') {
      getCharacter();
      return RX_ANY;
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
    return CHARS;
  }

  @Override
  public int getNormalSymbol() {
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
      return TOKEN;
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
          return TERM;
        case '<':
          getCharacter();
          return LEFT;
        case '2':
          getCharacter();
          return BINARY;
        case '>':
          getCharacter();
          return RIGHT;
        case '%':
        case '\\':
          getCharacter();
          markers++;
          return MARK;
        case '=':
          getCharacter();
          return PREC;
        case '@':
          getCharacter();
          return NAME;
        case '{':
          getCharacter();
          isCurlyBrace = true;
          return '{';
        case '!':
          getCharacter();
          return ERRDEF;
      }
      while (Character.isLetterOrDigit(runtimeData.currentCharacter)) {
        runtimeData.currentStringValue += runtimeData.currentCharacter;
        getCharacter();
      }
      for (ReservedWord rw : reservedWords) {
        if (runtimeData.currentStringValue.equals(rw.word)) {
          if (rw.token == UNION) {
            isCurlyBrace = true;
          }
          return rw.token;
        }
      }
      isError = true;
      environment.error(-1, "Reserved word \'%s\' is incorrect.", runtimeData.currentStringValue);
      return ERROR;
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
        return ERROR;
      }
      getCharacter();
      return TYPENAME;
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
      return NUM;
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
      return ERROR;
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
  
    return TOKEN;
  }

  /**
   * Execute this phase
   * @throws ParsingException on error.  Check cause and message.
   */
  public void execute() throws ParsingException {
    if (environment.isVerbose()) {
      System.out.println("Parse");
    }
    setVerbose(environment.isDebug());
    runtimeData.lineNumber = 0;
    markers = 0;
    Terminal terminal = new Terminal("$");
    runtimeData.getTerminals().add(terminal);
    terminal.setCount(0);
    terminal.setToken(0);
    if (parse() == 0 || isError) {
      throw new ParsingException("Parser returned errors.  Please see messages from parser");
    }
    reviewDeclarations();
    computeRootSymbol();
    generateTopRecoveryTable();
    finalizeSymbols();
    finalizeRules();
    generateTokenDefinitions();
    runtimeData.setNumberOfErrors(errorCount);
    runtimeData.setFinalActions(finalActions);
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
  
  static final ReservedWord reservedWords[] = { 
    new ReservedWord("token", TERM),
    new ReservedWord("term", TERM), 
    new ReservedWord("left", LEFT), 
    new ReservedWord("nonassoc", BINARY),
    new ReservedWord("binary", BINARY),
    new ReservedWord("right", RIGHT), 
    new ReservedWord("prec", PREC),
    new ReservedWord("start", START), 
    new ReservedWord("type", TYPE), 
    new ReservedWord("symbol", TYPE), 
    new ReservedWord("declare", DECLARE), 
    new ReservedWord("union", UNION),
    new ReservedWord("stack", UNION), 
    new ReservedWord("class", UNION),
    new ReservedWord("struct", UNION),
    new ReservedWord("name", NAME), 
    new ReservedWord("error", ERRDEF), 
    new ReservedWord("lexer", LEXER)
  };
  
}