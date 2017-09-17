/*
 Syntax is distributed under the Revised, or 3-clause BSD license
 ===============================================================================
 Copyright (c) 1985, 2012, 2016, Jaime Garza
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
     * Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.
     * Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.
     * Neither the name of the copyright holder nor the
       names of its contributors may be used to endorse or promote products
       derived from this software without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE FOR ANY
 DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ===============================================================================
*/
package me.jaimegarza.syntax.language;

import java.util.List;

import me.jaimegarza.syntax.EmbeddedCodeProcessor;
import me.jaimegarza.syntax.Lexer;
import me.jaimegarza.syntax.code.Fragments;
import me.jaimegarza.syntax.model.parser.Action;
import me.jaimegarza.syntax.model.parser.Driver;
import me.jaimegarza.syntax.model.parser.ErrorToken;
import me.jaimegarza.syntax.model.parser.GoTo;
import me.jaimegarza.syntax.model.parser.NonTerminal;
import me.jaimegarza.syntax.model.parser.Rule;
import me.jaimegarza.syntax.model.parser.State;
import me.jaimegarza.syntax.model.parser.Terminal;
import me.jaimegarza.syntax.util.FormattingPrintStream;

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
  public boolean getDefaultIncludeFlag() {
    return true;
  }
  
  @Override
  public int  generateCaseStart(int lineNumber, String label, String comment) {
    indent(environment.output, environment.getIndent());
    environment.output.printf("//%s\n", comment);
    indent(environment.output, environment.getIndent());
    environment.output.printf("%s: Begin\n", label);
    String i = indent(environment.output, environment.getIndent() + 1);
    return i.length();
  }
  
  @Override
  public void generateCaseEnd() {
    environment.output.println();
    indent(environment.output, environment.getIndent() + 1);
    environment.output.printf("end;\n");
  }

  @Override
  public boolean generateRuleCode(Lexer lexer, EmbeddedCodeProcessor processor, int elementCount, String nonTerminalId, int sourceColumn) {
    boolean end = false;

    while (!end) {
      switch (lexer.getCurrentCharacter()) {
        case '{': /* COMMENT in PAS */
          environment.output.print(lexer.getCurrentCharacter());
          while ((lexer.getNextCharacter()) != '}') {
            environment.output.print(lexer.getCurrentCharacter());
          }
          break;

        case '%':
        case '\\': /* finish an action in PAS */
          end = true;
          lexer.getNextCharacter();
          continue;

        case '(': /* possible comment in PAS */
          environment.output.print(lexer.getCurrentCharacter());
          lexer.getNextCharacter();
          if (lexer.getCurrentCharacter() != '*') {
            continue;
          }

          if (!processor.skipAndOutputCompositeComment(lexer, '*', ')')) {
            return false; 
          }
          environment.output.print(lexer.getCurrentCharacter());
          continue;

        case '\'': /* constant */
        case '"': /* string */
          processor.generateConstant(lexer, lexer.getCurrentCharacter());
          break;

        case '\n':
          environment.output.print(lexer.getCurrentCharacter());
          lexer.getNextCharacter();
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
      environment.output.print(lexer.getCurrentCharacter());
      lexer.getNextCharacter();
    }
    return true;
  }

  @Override
  public void generateLexerHeader(List<String> modes) {
    environment.include.println();
    if (environment.getDriver() == Driver.PARSER) {
      environment.include.println("{$DEFINE PARSER_MODE}");
      environment.include.println("{$DEFINE ACCEPTED:=1}");
      environment.include.println("{$DEFINE INTERNAL_ERROR:=0}");;
    } else {
      environment.include.println("{$DEFINE SCANNER_MODE}");
      environment.include.println("{$DEFINE ACCEPTED:=1}");
      environment.include.println("{$DEFINE SHIFTED:=2}");
      environment.include.println("{$DEFINE PARSING_ERROR:=3}");
      environment.include.println("{$DEFINE INTERNAL_ERROR:=0}");;
    }
    if (environment.isPacked()) {
      environment.include.println("{$DEFINE PACKED_TABLES}");
    } else {
      environment.include.println("{$DEFINE MATRIX_TABLES}");
    }
    if (!runtime.isStackTypeDefined()) {
      environment.include.println("TYPE");
      environment.include.println("  TSTACK = integer;");
      environment.include.println("  PTSTACK = ^TSTACK;");
    }
    environment.output.printf("\n")
                      .printf("{ Lexical Analyzer }\n")
                      .printf("\n")
                      .printf("function StxNextChar:char; forward;\n")
                      .printf("procedure StxUngetChar(c:char); forward;\n")
                      .printf("function StxMatchesRegex(vertex:integer):boolean; forward;\n");
    
    if (modes.size() > 1) {
      for (String mode: modes) {
        environment.output.printf("function StxLexer_" + computeModeName(mode) + ":longint; forward;\n");
      }
    }

    environment.output.printf("\n")
                      .printf("VAR\n")
                      .printf("  StxChar:char;\n")
                      .printf("  StxValue:TSTACK;\n")
                      .printf("  StxLexerMode:integer = DEFAULT_LEXER_MODE;\n")
                      .printf("  StxRecognized:String;\n")
                      .printf("\n")
                      .printf("function StxLexer:longint;\n")
                      .printf("begin\n");
    if (environment.lexerModes.size() > 1) {
      indent(environment.output, environment.getIndent());
      environment.output.println("case StxLexerMode of\n");
    } else {
      environment.output.println(environment.lexerModes.get("default").getWriter().toString());
    }
  }

  @Override
  public void generateLexerModeHeader(String lexerMode) {
    environment.output.printf("\n")
                      .printf("function StxLexer_" + computeModeName(lexerMode) + ":longint;\n")
                      .printf("begin\n");
  }

  @Override
  public String getLexerModeRoutine(String lexerMode) {
    return "function StxLexer_" + computeModeName(lexerMode) + ":longint";
  }
    
  @Override
  public void generateLexerModeFooter(String lexerMode) {
    environment.output.println();
    indent(environment.output, environment.getIndent());
    environment.output.printf("StxLexer_" + computeModeName(lexerMode) + " := 0;\n");
    environment.output.printf("end;(* " + computeModeName(lexerMode) + " *)\n");
  }

  @Override
  public void generateLexerFooter() {
    if (environment.lexerModes.size() > 1) {
      indent(environment.output, environment.getIndent());
      environment.output.printf("end;(* case *)\n");
      environment.output.println();
    }
    indent(environment.output, environment.getIndent());
    environment.output.printf("StxLexer := 0;\n");
    environment.output.printf("end;(* StxLexer *)\n");
  }

  @Override
  public void generateLexerModeDefinition(String lexerMode, int index) {
    if (index == 0) {
      environment.include.println("{$MACRO ON}");
    }
    environment.include.println("{$DEFINE " + computeModeName(lexerMode).toUpperCase() + "_LEXER_MODE:=" + index + "}");
  }

  @Override
  public void generateLexerModeCase(String lexerMode, int index) {
    indent(environment.output, environment.getIndent()+1);
    environment.output.println(computeModeName(lexerMode).toUpperCase() + "_LEXER_MODE: begin");
    indent(environment.output, environment.getIndent() + 2);
    environment.output.println("exit(StxLexer_" + computeModeName(lexerMode) + "());");
    indent(environment.output, environment.getIndent() + 2);
    environment.output.println("end;");
    environment.output.println();
  }

  @Override
  public void generateCodeGeneratorHeader() {
    environment.output.printf("\n")
     
                      .printf("{ Code generator }\n")
                      .printf("\n")
                      .printf("CONST\n")
                      .printf("    STACK_DEPTH = 5000;\n")
                      .printf("\n")
                      .printf("VAR\n")
                      .printf("    StxStack : Array [0..STACK_DEPTH] of TStack;\n")
                      .printf("    pStxStack: Integer;\n")
                      .printf("\n")
                      .printf("function StxCode(rule:integer):boolean;\n")
                      .printf("begin\n");
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("case rule of\n");
  }
  
  @Override
  public void generateCodeGeneratorFooter() {
    indent(environment.output, environment.getIndent());
    environment.output.printf("end;(* CASE *)\n");
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("StxCode := true;\n");
    environment.output.printf("end;(* StxCode *)\n");
  }

  @Override
  public void generateVoidCodeGenerator() {
    environment.output.printf("\n")
    .printf("{ Code generator }\n")
    .printf("\n")
    .printf("CONST\n")
    .printf("    STACK_DEPTH = 5000;\n")
    .printf("\n")
    .printf("VAR\n")
    .printf("  StxStack : Array [0..512] of TStack;\n")
    .printf("  pStxStack: Integer;\n")
    .printf("\n")
    .printf("function StxCode(rule:integer):boolean;\n")
    .printf("begin\n");
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("StxCode := true;\n");
    environment.output.printf("end;(* StxCode *)\n");
  }
  
  @Override
  public void generateRecoveryTableHeader(int numberOfErrorTokens) {
        environment.output.printf("\nConst\n  RECOVERS = %d;\n"
                                  + "{ Contains token ids }", numberOfErrorTokens - 1);
        if (numberOfErrorTokens != 0) {
          environment.output.printf("\n  StxRecoverTable : array [0..RECOVERS] of LongInt = (\n");
        } else {
          environment.output.printf("\n  StxRecoverTable : array [0..0] of LongInt = (0);\n\n");
        }
  }

  @Override
  public boolean generateStructure(Lexer lexer) {
    int level;
    boolean hasCharacters;

    environment.output.printf("Type\n"
                              + "  {$define TSTACK_DEFINED}\n"
                              + "  PTStack = ^TStack;\n"
                              + "  TStack = Record\n"
                              + "    case integer of\n");
    level = 0;
    hasCharacters = false;
    while (lexer.getCurrentCharacter() != '%' && lexer.getCurrentCharacter() != '\\') {
      if (lexer.getCurrentCharacter() == '\r') {
        lexer.getNextCharacter();
        continue;
      }
      if (lexer.getCurrentCharacter() == '\n') {
        if (hasCharacters) {
          environment.output.printf(");\n");
        }
        hasCharacters = false;
      } else {
        if (hasCharacters == false) {
          environment.output.printf("      %d:(", level);
          level++;
        }
        hasCharacters = true;
        environment.output.print(lexer.getCurrentCharacter());
      }
      lexer.getNextCharacter();
    }
    lexer.getNextCharacter();
    environment.output.printf("  end;\n");

    return true;
  }

  @Override
  public void generateErrorToken(int recoveryToken, ErrorToken id, boolean isLast) {
    if (!isLast) {
      environment.output.printf("\t%d, (* %s : \"%s\" *)\n", recoveryToken, id.getName(), id.getFullName());
    } else {
      environment.output.printf("\t%d (* %s : \"%s\" *) );\n\n", recoveryToken, id.getName(), id.getFullName());
    }
  }

  @Override
  public void generateTokensHeader(int terminals) {
    environment.include.printf("\nConst\n  TOKENS = %d;\n", terminals);
    environment.output.printf("\nVar\n  StxTokens : array [0..TOKENS-1] of Longint = (\n");
  }

  @Override
  public void generateToken(Terminal id, boolean isLast) {
    if (isLast) {
      environment.output.printf("    %d); // %s (%s)\n", id.getToken(), id.getName(), id.getFullName());
    } else {
      environment.output.printf("    %d, // %s (%s)\n", id.getToken(), id.getName(), id.getFullName());
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
    environment.include.printf("{Token information structure}\n");
    environment.include.printf("TYPE\n");
    environment.include.printf("  PTOKENDEF = ^TOKENDEF;\n");
    environment.include.printf("  TOKENDEF = RECORD\n");
    environment.include.printf("    name:STRING;\n");
    environment.include.printf("    fullName:STRING;\n");
    environment.include.printf("    token:LONGINT;\n");
    environment.include.printf("    reserved:BOOLEAN;\n");
    environment.include.printf("  END;\n\n");
    environment.output.printf("\nVAR\n");
    environment.output.printf("  StxTokenDefs: array [0..%d] of TOKENDEF = (\n", runtime.getTerminals().size()-1);
    int i = 0;
    for (Terminal id : runtime.getTerminals()) {
      if (!id.getVariable().equals("_")) {
        environment.output.printf("  {%3d}  (name:'%s'; fullName:'%s'; token:%d; reserved:TRUE)", i, id.getVariable(), escapeDoubleQuotes(id.getFullName()), id.getToken());
      } else {
        environment.output.printf("  {%3d}  (name:'%s'; fullName:'%s'; token:%d; reserved:FALSE)", i, id.getName(), escapeDoubleQuotes(id.getFullName()), id.getToken());
      }
      i++;
      if (i < runtime.getTerminals().size()) {
        environment.output.print(",");
      }
      environment.output.println();
    }
    environment.output.printf(");\n");
  }

  @Override
  protected String escapeDoubleQuotes(String error) {
    return error.replaceAll("\\'", "''");
  }

  @Override
  protected boolean lexerDollar(FormattingPrintStream output, String lexerMode, Lexer lexer, Terminal token) {
    lexer.getNextCharacter();
    if (lexer.getCurrentCharacter() == '+') {
      lexer.getNextCharacter();
      output.printFragment("getc");
      return true;
    } else if (lexer.getCurrentCharacter() == 'c') {
      lexer.getNextCharacter();
      output.printFragment("currentChar");
      return true;
    } else if (lexer.getCurrentCharacter() == 'l') {
      lexer.getNextCharacter();
      output.printFragment("lexerMode");
      return true;
    } else if (lexer.getCurrentCharacter() == 'v') {
      lexer.getNextCharacter();
      output.printFragment(Fragments.LEXICAL_VALUE);
      return true;
    } else if (lexer.getCurrentCharacter() == 't') {
      lexer.getNextCharacter();
      output.printFragment(Fragments.TOKEN, token.getName());
      return true;
    } else if (lexer.getCurrentCharacter() == 'm') {
      lexer.getNextCharacter();
      output.printFragment(Fragments.LEXER_FUNCTION_NAME, lexerMode);
      return true;
    } else if (lexer.getCurrentCharacter() == 'r') {
      lexer.getNextCharacter();
      output.printFragment(Fragments.RECOGNIZED);
      return true;
    } else if (lexer.getCurrentCharacter() == 'x') {
      lexerReturnValue(output, lexerMode, lexer);
      return true;
    }
    output.print('$');
    return false; 
  }

  protected void lexerReturnValue(FormattingPrintStream output, String lexerMode, Lexer lexer) {
    String follows = "";
    lexer.getNextCharacter();
    while (lexer.getCurrentCharacter() == ' ') {
      follows = lexerAccumulateCurrentCharacter(lexer, follows);
    }
    if (lexer.getCurrentCharacter() == '(') {
      String returnValue = "";
      int level = 0;
      lexer.getNextCharacter();
      while ((lexer.getCurrentCharacter() != ')' || level > 0) && lexer.getCurrentCharacter() != 0) {
        if (lexer.getCurrentCharacter() == '\'') {
          returnValue = readLexerString(lexer, returnValue, '\'');
        } else if (lexer.getCurrentCharacter() == '"') {
          returnValue  = readLexerString(lexer, returnValue, '"');
        } else if (lexer.getCurrentCharacter() == '(') {
          returnValue = lexerAccumulateCurrentCharacter(lexer, returnValue);
          level++;
        } else if (lexer.getCurrentCharacter() == ')') {
          if (level > 0) {
            returnValue = lexerAccumulateCurrentCharacter(lexer, returnValue);
          }
          level--;
        } else {
          returnValue = lexerAccumulateCurrentCharacter(lexer, returnValue);
        }
      }
      if (lexer.getCurrentCharacter() == ')') {
        lexer.getNextCharacter();
      } else {
        environment.error(runtime.lineNumber, "Unfinished return value.  Recognized %s.", returnValue);
      }
      output.printFragment(Fragments.RETURN, returnValue);
    } else {
      output.printf("exit(%s)", follows);
    }
  }

  private String readLexerString(Lexer lexer, String s, char separator) {
    s = lexerAccumulateCurrentCharacter(lexer, s);
    while (lexer.getCurrentCharacter() != separator && lexer.getCurrentCharacter() != 0) {
      s = lexerAccumulateCurrentCharacter(lexer, s);
    }
    if (lexer.getCurrentCharacter() == separator) {
      s = lexerAccumulateCurrentCharacter(lexer, s);
    }
    return s;
  }

  private String lexerAccumulateCurrentCharacter(Lexer lexer, String s) {
    s = s + lexer.getCurrentCharacter();
    lexer.getNextCharacter();
    return s;
  }
  
  @Override
  public boolean generateLexerCode(FormattingPrintStream output, String lexerMode, Lexer lexer, Terminal token, int additionalIndent) {
    boolean end = false;
    boolean bStart = true;

    while (!end) {
      switch (lexer.getCurrentCharacter()) {
        case '$':
          if (lexerDollar(output, lexerMode, lexer, token)) {
            continue;
          }
          break;

        case '{': /* COMMENT in PAS */
          output.print(lexer.getCurrentCharacter());
          while ((lexer.getNextCharacter()) != '}') {
            output.print(lexer.getCurrentCharacter());
          }
          break;

        case '%':
        case '\\': /* finact in PAS y ASM */
          end = true;
          lexer.getNextCharacter();
          continue;

        case '(': /* possible comment in PAS */
          if(!lexerComment(output, lexer, ')')) {
            return false;
          }
          continue;

        case '\'': /* constant */
        case '"': /* string */
          if(!lexerString(output, lexer, lexer.getCurrentCharacter())) {
            return false;
          }
          break;

        case '\n':
          output.printf("%c", lexer.getCurrentCharacter());
          lexer.getNextCharacter();
          indent(output, environment.getIndent() + additionalIndent);
          continue;

        case 0:
          environment.error(-1, "Unfinished action detected.");
          return false;

      }
      if (!bStart || lexer.getCurrentCharacter() != '{') {
        output.print(lexer.getCurrentCharacter());
      }
      if (lexer.getCurrentCharacter() > ' ') {
        bStart = false;
      }
      lexer.getNextCharacter();
    }
    return true;
  }

  @Override
  public void printCodeHeader() {
    environment.output.printf("\nCONST\n")
                      .printf("  ACCEPT = %d;\n", Integer.MAX_VALUE)
                      .printf("\n");
    environment.output.printf("\n"
                            + "TYPE\n");
    if (environment.isPacked()) {
      environment.output.printf(  "  PACTION = ^ACTION;\n"
                                  + "  ACTION = RECORD\n"
                                  + "    symbol:LongInt;\n"
                                  + "    state:LongInt;\n"
                                  + "  end;\n"
                                  + "\n");
    }
    environment.output.printf("  PGOTOS = ^GOTOS;\n"
                              + "  GOTOS = RECORD\n"
                                + "    origin:LongInt;\n"
                                + "    destination:LongInt;\n"
                                + "  end;\n"
                                + "\n");
    environment.output.printf("  PPARSER = ^PARSER;\n"
                              + "  PARSER = RECORD\n"
                                + "    position:LongInt;\n"
                                + "    defa:LongInt;\n"
                                + "    elements:LongInt;\n"
                                + "    msg:LongInt;\n"
                                + "  end;\n"
                                + "\n");
    environment.output.printf("  PGRAMMAR = ^GRAMMAR;\n"
                              + "  GRAMMAR = RECORD\n"
                                + "    symbol:LongInt;\n"
                                + "    reductions:LongInt;\n"
                                + "  end;\n");
    // reserve a place for where the size of the table will be written
    // fPos = ftell(environment.output);
    environment.include.printf("\nConst\n  FINAL = %d;\n" + "  SYMBS = %d;\n", runtime.getStates().length,
        runtime.getTerminals().size() + runtime.getNonTerminals().size() - 1);
    environment.output.printf("\n  TABLEROWS = 0..FINAL-1;\n" + "  TABLECOLS = 0..SYMBS-1;\n" + "\n");
    if (!environment.isPacked()) {
      environment.output.printf("VAR\n  {Parsing Table}\n"
                                + "  StxParsingTable : array [TABLEROWS,TABLECOLS] of LongInt = (\n");
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
      if (parserLine[index] == Integer.MAX_VALUE) {
        environment.output.print("ACCEPT");
      } else {
        environment.output.printf("%6d", parserLine[index]);
      }      
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
  public void printParserErrors() {
    if (environment.isPacked() == true) {
      return;
    }
    environment.output.println();
    indent(environment.output, environment.getIndent()-1);
    environment.output.printf("{Parsing Errors}\n");
    indent(environment.output, environment.getIndent()-1);
    environment.output.printf("StxParsingError : array [TABLEROWS] of Integer = (\n");
    int i = 0;
    for (State I : runtime.getStates()) {
      indent(environment.output, environment.getIndent());
      if (i == runtime.getStates().length - 1) {
        environment.output.printf(" {%3d} %s  (* %s *)\n", i, I.getMessage(), getErrorMessage(I));
        indent(environment.output, environment.getIndent()-1);
        environment.output.printf(");\n\n");
      } else {
        environment.output.printf(" {%3d} %s, (* %s *)\n", i, I.getMessage(), getErrorMessage(I));
      }
      i++;
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
    indent(environment.output, environment.getIndent()-1);
    environment.output.printf("\n{Error Messages}\n");
    indent(environment.output, environment.getIndent()-1);
    environment.output.printf("StxErrorTable : array [0..%d] of String = (\n", runtime.getErrorMessages().size() - 1);
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
    environment.output.printf("\n{Grammar Table}\n");
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
    environment.output.printf("\nConst\n  NON_TERMINALS = %d;\n", runtime.getNonTerminals().size() - 1);
    environment.output.printf("\n  StxNonTerminals : array [0..NON_TERMINALS] of INTEGER = (\n");
    int i = 1;
    for (NonTerminal id : runtime.getNonTerminals()) {
      if (i == runtime.getNonTerminals().size()) {
        environment.output.printf("  %d  (* %s *));\n\n", id.getToken(), id.getName());
      } else {
        environment.output.printf("  %d, (* %s *)\n", id.getToken(), id.getName());
      }
      i++;
    }
    environment.output.println();
  }
  @Override
  public void generateEdgeHeader(int size) {
    environment.include.printf("\nConst\n  EDGES = %d;\n", size);
    if (size > 0){
      environment.output.printf("\n  StxEdges : array [0..EDGES-1] of INTEGER = (\n", runtime.getNonTerminals().size());
    } else {
      environment.output.printf("\n  StxEdges : array [0..0] of INTEGER = (0);\n", runtime.getNonTerminals().size());
    }
  }

  @Override
  public void generateVertexHeader(int size) {
    environment.include.printf("\nConst\n  VERTICES = %d;\n", size);
    if (size > 0){
      environment.output.printf("\n  StxVertices : array [0..VERTICES-1] of INTEGER = (\n", runtime.getNonTerminals().size());
    } else {
      environment.output.printf("\n  StxVertices : array [0..0] of INTEGER = (0);\n", runtime.getNonTerminals().size());
    }
  }
  
  @Override
  public void generateIntArrayRow(int i, String comment, int index, int maxSize) {
    indent(environment.output, environment.getIndent());
    environment.output.printf("%5d%s%s\n", i, index < maxSize - 1 ? "," : ");", comment == null ? "": " (* " + comment + " *)", index, maxSize);
  }

  @Override
  public void generateIntArrayComment(String comment) {
    indent(environment.output, environment.getIndent());
    environment.output.printf("%s\n",  comment == null ? "": "(* " + comment + " *)");
  }

  @Override
  public void generateIntArrayFooter() {
  }

  @Override
  public void generateRegexMatch(FormattingPrintStream output, int dfaNode) {
    output.printf("  if   StxMatchesRegex(%d)\n", dfaNode);
    output.printf("  then begin\n", dfaNode);
  }

  @Override
  public void generateRegexReturn(FormattingPrintStream output, Terminal token) {
    indent(output, environment.getIndent() - 1);
    output.printf("    StxLexer := %s;\n", token.getName());
  }

  @Override
  public void generateRegexEnd(FormattingPrintStream output) {
    indent(output, environment.getIndent() - 1);
    output.printf("  end;\n\n");
  }
}
