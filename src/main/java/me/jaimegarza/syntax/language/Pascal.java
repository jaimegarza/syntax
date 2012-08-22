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
package me.jaimegarza.syntax.language;

import java.io.IOException;

import me.jaimegarza.syntax.EmbeddedCodeProcessor;
import me.jaimegarza.syntax.Lexer;
import me.jaimegarza.syntax.definition.Action;
import me.jaimegarza.syntax.definition.ErrorToken;
import me.jaimegarza.syntax.definition.GoTo;
import me.jaimegarza.syntax.definition.NonTerminal;
import me.jaimegarza.syntax.definition.Rule;
import me.jaimegarza.syntax.definition.Terminal;

/**
 * Routines for supporting the pascal language
 *
 * @author jaimegarza@gmail.com
 *
 */
public class Pascal extends BaseLanguageSupport {
  
  @Override
  public String getLanguageCode() {
    return "pascal";
  }

  @Override
  public String getId() {
    return "p";
  }

  @Override
  public String getExtensionSuffix() {
    return ".pas";
  }

  @Override
  public String getIncludeExtensionSuffix() {
    return ".inc";
  }
  
  @Override
  public void generateCaseStart(int lineNumber, String label) {
    indent(environment.output, environment.getIndent());
    environment.output.printf("%s: Begin\n", label);
    indent(environment.output, environment.getIndent() + 1);
  }
  
  @Override
  public void generateCaseEnd() {
    environment.output.println();
    indent(environment.output, environment.getIndent() + 1);
    environment.output.printf("end;\n");
  }

  @Override
  public boolean generateRuleCode(Lexer lexer, EmbeddedCodeProcessor processor, int elementCount, String nonTerminalId) throws IOException {
    boolean end = false;

    while (!end) {
      switch (runtime.currentCharacter) {
        case '{': /* COMMENT in PAS */
          environment.output.print(runtime.currentCharacter);
          while ((lexer.getCharacter()) != '}') {
            environment.output.print(runtime.currentCharacter);
          }
          break;

        case '%':
        case '\\': /* finish an action in PAS */
          end = true;
          lexer.getCharacter();
          continue;

        case '(': /* possible comment in PAS */
          environment.output.print(runtime.currentCharacter);
          lexer.getCharacter();
          if (runtime.currentCharacter != '*') {
            continue;
          }

          if (!processor.skipAndOutputCompositeComment(lexer, '*', ')')) {
            return false; 
          }
          environment.output.print(runtime.currentCharacter);
          continue;

        case '\'': /* constant */
        case '"': /* string */
          processor.generateConstant(lexer, runtime.currentCharacter);
          break;

        case '\n':
          environment.output.print(runtime.currentCharacter);
          lexer.getCharacter();
          indent(environment.output, environment.getIndent() + 1);
          continue;

        case 0:
          environment.error(-1, "Unfinished action detected.");
          return false;

        case '$':
          int command = manageDollar(lexer, processor, elementCount, nonTerminalId);
          if (command == 0) {
            return false;
          } else if (command > 0) {
            continue;
          }
          break;
      }
      environment.output.print(runtime.currentCharacter);
      lexer.getCharacter();
    }
    return true;
  }

  @Override
  public void generateLexerHeader() {
    environment.output.printf("\n")
                      .printf("{ Lexical Analyzer }\n")
                      .printf("\n")
                      .printf("VAR StxChar:char;\n")
                      .printf("\n")
                      .printf("function StxLexer():int;\n")
                      .printf("begin\n");
  }

  @Override
  public void generateLexerFooter() {
    environment.output.println();
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("StxLexer := 0;\n");
    environment.output.printf("END;(* StxLexer *)\n");
  }

  @Override
  public void generateCodeGeneratorHeader() {
    environment.output.printf("\n")
                      .printf("{ Code generator }\n")
                      .printf("\n")
                      .printf("Var\n")
                      .printf("  {$define STXCODE_DEFINED}\n")
                      .printf("  StxStack : Array [0..512] of TStack;\n")
                      .printf("  pStxStack: Integer;\n")
                      .printf("\n")
                      .printf("function StxCode(rule:integer):boolean;\n")
                      .printf("begin\n");
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("Case rule Of\n");
  }
  
  @Override
  public void generateCodeGeneratorFooter() {
    indent(environment.output, environment.getIndent());
    environment.output.printf("END;(* CASE *)\n");
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("StxCode := true;\n");
    environment.output.printf("END;(* StxCode *)\n");
  }

  @Override
  public void generateRecoveryTableHeader(int numberOfErrorTokens) {
        environment.output.printf("\nConst\n  RECOVERS = %d;\n"
                                  + "{ Contains tokens in compact mode, and column in matrix }", numberOfErrorTokens - 1);
        if (numberOfErrorTokens != 0) {
          environment.output.printf("\n  StxRecoverTable : array [0..RECOVERS] of INTEGER = (\n");
        } else {
          environment.output.printf("\n  StxRecoverTable : array [0..0] of INTEGER = (0);\n\n");
        }
  }

  @Override
  public boolean generateStructure(Lexer lexer) throws IOException {
    int level;
    boolean hasCharacters;

    environment.output.printf("Type\n"
                              + "  {$define TSTACK_DEFINED}\n"
                              + "  PTStack = ^TStack;\n"
                              + "  TStack = Record\n"
                              + "    case integer of");
    level = 0;
    hasCharacters = false;
    while (runtime.currentCharacter != '%' && runtime.currentCharacter != '\\') {
      if (runtime.currentCharacter == '\n') {
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
      environment.output.print(runtime.currentCharacter);
      lexer.getCharacter();
    }
    lexer.getCharacter();
    environment.output.printf("  end;\n");

    return true;
  }

  @Override
  public void generateErrorToken(int recoveryToken, ErrorToken id, boolean isLast) {
    if (!isLast) {
      environment.output.printf("\t%d, (* %s *)\n", recoveryToken, id.getName());
    } else {
      environment.output.printf("\t%d (* %s *) );\n\n", recoveryToken, id.getName());
    }
  }

  @Override
  public void generateTokensHeader(int terminals) {
    environment.output.printf("\nConst\n  TOKENS = %d;\n", terminals - 1);
    environment.output.printf("\n  StxTokens : array [0..TOKENS] of Integer = (\n");
  }

  @Override
  public void generateToken(Terminal id, boolean isLast) {
    if (isLast) {
      environment.output.printf("    %d\n (* %s (%s) *) );\n", id.getToken(), id.getName(), id.getFullName());
    } else {
      environment.output.printf("    %d,\n (* %s (%s) *)", id.getToken(), id.getName(), id.getFullName());
    }
  }

  @Override
  public void generateTokenDefinitions() {
    boolean first = true;
    for (Terminal id : runtime.getTerminals()) {
      id.computeVariable();
      if (!id.getVariable().equals("_")) {
        if (first) {
          environment.include.printf("\n(* Token definitions *)\n");
          environment.include.printf("\nConst\n");
          first = false;
        }
        environment.include.printf("  %s = %d;\n", id.getVariable(), id.getToken());
      }
    }
    environment.include.printf("\n");
  }

  @Override
  public boolean generateLexerCode(Lexer lexer) throws IOException {
    boolean end = false;
    boolean bStart = true;

    while (!end) {
      switch (runtime.currentCharacter) {
        case '$':
          if (lexerDollar(lexer)) {
            continue;
          }
          break;

        case '{': /* COMMENT in PAS */
          environment.output.print(runtime.currentCharacter);
          while ((lexer.getCharacter()) != '}') {
            environment.output.print(runtime.currentCharacter);
          }
          break;

        case '%':
        case '\\': /* finact in PAS y ASM */
          end = true;
          lexer.getCharacter();
          continue;

        case '(': /* possible comment in PAS */
          if(!lexerComment(lexer, ')')) {
            return false;
          }
          continue;

        case '\'': /* constant */
        case '"': /* string */
          if(!lexerString(lexer, runtime.currentCharacter)) {
            return false;
          }
          break;

        case '\n':
          environment.output.print(runtime.currentCharacter);
          lexer.getCharacter();
          indent(environment.output, environment.getIndent() + 1);
          continue;

        case 0:
          environment.error(-1, "Unfinished action detected.");
          return false;

      }
      if (!bStart) {
        environment.output.print(runtime.currentCharacter);
      }
      if (runtime.currentCharacter > ' ') {
        bStart = false;
      }
      lexer.getCharacter();
    }
    return true;
  }

  @Override
  public void printCodeHeader() {
    if (environment.isPacked()) {
      environment.output.printf("\n"
                                + "Type\n"
                                  + "  PACTION = ^ACTION;\n"
                                  + "  ACTION = RECORD\n"
                                  + "    symbol:SmallInt;\n"
                                  + "    state:SmallInt;\n"
                                  + "  end;\n"
                                  + "\n");
    }
    environment.output.printf("  PGOTOS = ^GOTOS;\n"
                              + "  GOTOS = RECORD\n"
                                + "    origin:SmallInt;\n"
                                + "    destination:SmallInt;\n"
                                + "  end;\n"
                                + "\n");
    environment.output.printf("  PPARSER = ^PARSER;\n"
                              + "  PARSER = RECORD\n"
                                + "    position:SmallInt;\n"
                                + "    defa:SmallInt;\n"
                                + "    elements:SmallInt;\n"
                                + "    msg:SmallInt;\n"
                                + "  end;\n"
                                + "\n");
    environment.output.printf("  PGRAMMAR = ^GRAMMAR;\n"
                              + "  GRAMMAR = RECORD\n"
                                + "    symbol:SmallInt;\n"
                                + "    reductions:SmallInt;\n"
                                + "  end;\n");
    // reserve a place for where the size of the table will be written
    // fPos = ftell(environment.output);
    environment.output.printf("\nConst\n  FINAL = %d;\n" + "  SYMBS = %5d;\n", runtime.getStates().length,
        runtime.getTerminals().size() + runtime.getNonTerminals().size() - 2);
    environment.output.printf("Type\n" + "  TABLEROWS = 0..FINAL;\n" + "  TABLECOLS = 0..SYMBS;\n" + "\n");
    if (!environment.isPacked()) {
      environment.output.printf("{Parsing Table}\n"
                                + "    StxParsingTable : array [TABLEROWS,TABLECOLS] of Integer = (\n");
    }
  }

  @Override
  public void printTableRow(int symbolCounter, int[] parserLine, int stateNumber) {
    environment.output.print("        (");
    int index;
    for (int column = index = 0; index <= symbolCounter; ++index) {
      // If this column does not fit, cut with \n
      if ((column + 1) * 5 + 9 > environment.getMargin()) {
        environment.output.printf("\n         ");
        column = 0;
      }
      column++;
      environment.output.printf("%4d", parserLine[index]);
      if (index < symbolCounter) {
        environment.output.printf(",");
      }
    }
    if (stateNumber == runtime.getStates().length - 1) {
      environment.output.printf("));\n");
    } else {
      environment.output.printf("),\n");
    }
  }

  @Override
  public void printParsingTableHeader() {
    environment.output.printf("\n" + "  StxParsingTable : array [TABLEROWS] of PARSER = (\n");
  }

  @Override
  public void printPackedState(int stateNumber) {
    environment.output.printf("    (position:%d; defa:%d; elements:%d; msg:%d)",
        runtime.getStates()[stateNumber].getPosition(), runtime.getStates()[stateNumber].getDefaultValue(),
        runtime.getStates()[stateNumber].getActionSize(), runtime.getStates()[stateNumber].getMessage());
    if (stateNumber == runtime.getStates().length - 1) {
      environment.output.printf(");\n");
    } else {
      environment.output.printf(",\n");
    }
  }

  @Override
  public void printErrorTableHeader() {
    environment.output.printf("\n" + "  StxErrorTable : array [0..%d] of String = (\n", runtime.getErrorMessages().size() - 1);
  }

  @Override
  public void printErrorEntry(int error) {
    environment.output.printf("    \'");
    String errorMessage = runtime.getErrorMessages().get(error);
    for (int i = 0; i < errorMessage.length(); i++) {
      char c = errorMessage.charAt(i);
      if (c == '\'') {
        environment.output.printf("\'");
      }
      environment.output.printf("%c", c);
    }
    environment.output.printf("\'");
    if (error != runtime.getErrorMessages().size() - 1) {
      environment.output.printf(",\n");
    }
  }

  @Override
  public void printErrorFooter() {
    environment.output.printf(");\n");
  }

  @Override
  public void printActionHeader() {
    environment.output.printf("Const\n"
        + "  NUM_GOTOS = %d;\n"
          + "  ACTIONS = %d;\n\n"
          + "  StxActionTable : Array [0..ACTIONS-1] of ACTION = (\n",
          runtime.getNumberOfGoTos(), runtime.getNumberOfActions());
  }

  @Override
  public void printAction(int action, Action anAction) {
    environment.output.printf("    (symbol:%d; state:%d)", anAction.getSymbol().getToken(), anAction.getStateNumber());
    if (action == runtime.getNumberOfActions() - 1) {
      environment.output.printf(");\n");
    } else {
      environment.output.printf(",\n");
    }
  }

  @Override
  public void printGoToTableHeader() {
    environment.output.printf("\n" + "  StxGotoTable : array [0..NUM_GOTOS-1] of GOTOS = (\n");
  }

  @Override
  public void printGoTo(int gotoIndex, GoTo pGoto) {
    environment.output.printf("    (origin:%d; destination:%d)", pGoto.getOrigin(), pGoto.getDestination());
    if (gotoIndex == runtime.getNumberOfGoTos() - 1) {
      environment.output.printf(");\n");
    } else {
      environment.output.printf(",\n");
    }
  }

  @Override
  public void printMissingSkeleton(String filename) {
    environment.output.println("  (* missing internal skeleton " + filename + " *)");
  }

  @Override
  public void printGrammarTable() {
    environment.output.printf("\n");
    int numberOfRules = runtime.getRules().size();
    environment.output.printf("  StxGrammarTable : Array [0..%d] of GRAMMAR = (\n", numberOfRules - 1);
    int index = 0;
    for (Rule stx : runtime.getRules()) {
      int itemSize = stx.getItems().size();
      environment.output.printf("    (symbol:%d; reductions:%d)", environment.isPacked() ? stx.getLeftHand()
          .getToken() : stx.getLeftHandId(), itemSize);
      if (++index == numberOfRules) {
        environment.output.printf(");\n");
      } else {
        environment.output.printf(",\n");
      }
    }
    if (environment.isPacked()) {
      environment.output.printf("\nConst\n  NON_TERMINALS = %d;\n", runtime.getNonTerminals().size() - 1);
      environment.output.printf("\n  StxNonTerminals : array [0..NON_TERMINALS] of INTEGER = (\n");
      int i = 1;
      for (NonTerminal id : runtime.getNonTerminals()) {
        if (i == runtime.getNonTerminals().size()) {
          environment.output.printf("  %d, (* %s *));\n\n", id.getToken(), id.getName());
        } else {
          environment.output.printf("  %d, (* %s *)\n", id.getToken(), id.getName());
        }
        i++;
      }
    }
  }
}
