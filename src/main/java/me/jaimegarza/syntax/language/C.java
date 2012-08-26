/*
Syntax is distibuted under the Revised, or 3-clause BSD license
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

import me.jaimegarza.syntax.Lexer;
import me.jaimegarza.syntax.definition.Action;
import me.jaimegarza.syntax.definition.Driver;
import me.jaimegarza.syntax.definition.ErrorToken;
import me.jaimegarza.syntax.definition.GoTo;
import me.jaimegarza.syntax.definition.NonTerminal;
import me.jaimegarza.syntax.definition.Rule;
import me.jaimegarza.syntax.definition.State;
import me.jaimegarza.syntax.definition.Terminal;


/**
 * Routines for supporting C language
 * 
 * @author jaimegarza@gmail.com
 *
 */
public class C extends BaseLanguageSupport {

  @Override
  public String getLanguageCode() {
    return "c";
  }

  @Override
  public String getId() {
    return "c";
  }

  @Override
  public String getExtensionSuffix() {
    return ".c";
  }

  @Override
  public String getIncludeExtensionSuffix() {
    return ".h";
  }
  
  @Override
  public void emitLine(int lineNumber) {
    emitLine(lineNumber, environment.getSourceFile().toString());
  }
  
  @Override
  public void emitLine(int lineNumber, String filename) {
    if (environment.isEmitLine()) {
      filename = filename.replaceAll("\\\\", "/");
      environment.output.printf("#line %d \"%s\"\n", lineNumber, filename);
    }
  }
  
  @Override
  public int getNumberOfSpacesPerIndent() {
    return 4;
  }

  @Override
  public void generateCaseStart(int lineNumber, String label) {
    indent(environment.output, environment.getIndent());
    environment.output.printf("case %s:\n", label);
    indent(environment.output, environment.getIndent() + 1);
    if (environment.isEmitLine()) {
      emitLine(lineNumber + 1);
      indent(environment.output, environment.getIndent() + 1);
    }
  }
  
  @Override
  public void generateCaseEnd() {
    environment.output.println();
    indent(environment.output, environment.getIndent() + 1);
    environment.output.printf("break;\n");    
  }

  @Override
  public void generateLexerHeader() {
    if (environment.getDriver() == Driver.PARSER) {
      environment.include.println("#define PARSER_MODE");
    } else {
      environment.include.println("#define SCANNER_MODE");
      environment.include.println("#define ACCEPTED 1");
      environment.include.println("#define SHIFTED 2");
      environment.include.println("#define PARSING_ERROR 3");
      environment.include.println("#define INTERNAL_ERROR 4");;
    }
    if (!runtime.isStackTypeDefined()) {
      environment.include.println("typedef int tstack, *ptstack");
      environment.include.printf("#define TSTACK tstack\n#define PTSTACK ptstack\n");
      environment.include.println("typedef int tstack, *ptstack");
    }
    environment.include
                      .printf("/* Lexical Recognizer */\n")
                      .printf("\n")
                      .printf("extern char   StxChar;\n")
                      .printf("extern TSTACK StxValue;\n")
                      .printf("char          StxNextChar();\n");
    environment.output.printf("\n")
                      .printf("unsigned long int StxLexer()\n")
                      .printf("{\n");
  }

  @Override
  public void generateLexerFooter() {
    environment.output.println();
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("return 0; /* UNKNOWN */\n");
    environment.output.printf("}/* End of StxLexer */\n");
  }

  @Override
  public void generateCodeGeneratorHeader() {
    environment.output.printf("\n")
                      .printf("/* Code Generator */\n")
                      .printf("\n")
                      .printf("#ifndef TSTACK\n")
                      .printf("#define TSTACK int\n")
                      .printf("#endif\n")
                      .printf("\n")
                      .printf("TSTACK StxStack[150];\n")
                      .printf("\n")
                      .printf("int pStxStack;\n")
                      .printf("\n")
                      .printf("#define STXCODE_DEFINED\n")
                      .printf("\n")
                      .printf("int StxCode(int rule)\n")
                      .printf("{\n");
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("switch(rule){\n");
    environment.output.println();
  }
  
  @Override
  public void generateCodeGeneratorFooter() {
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("}/* End of switch */\n");
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("return 1; /* OK */\n");
    environment.output.printf("}/* End of StxCode */\n");
  }
  
  @Override
  public void generateRecoveryTableHeader(int numberOfErrorTokens) {
    environment.include.printf("\n#define RECOVERS %d\n", numberOfErrorTokens);
    environment.output.printf("\n"
                              + "/* Contains tokens in compact mode, and column in matrix */");
    if (numberOfErrorTokens != 0) {
      environment.output.printf("\nint StxRecoverTable[RECOVERS] = {\n");
    } else {
      environment.output.printf("\nint StxRecoverTable[1] = {0};\n");
    }
  }

  @Override
  public boolean generateStructure(Lexer lexer) throws IOException {
    int level;

    environment.include.printf("typedef union");
    level = 0;
    while (2 > 1) {
      if (runtime.currentCharacter == '\0') {
        environment.error(-1, "End of file processing \'%%union\'.");
        return false;
      }

      environment.include.print(runtime.currentCharacter);
      switch (runtime.currentCharacter) {
        case '{':
          ++level;
          break;

        case '}':
          --level;
          if (level == 0) {
            environment.include.printf(" tstack, *ptstack;\n\n");
            environment.include.printf("#define TSTACK tstack\n" + "#define PTSTACK ptstack\n\n");
            lexer.getCharacter();
            return true;
          }
      }
      lexer.getCharacter();
    }
  }

  @Override
  public void generateErrorToken(int recoveryToken, ErrorToken id, boolean isLast) {
    if (!isLast) {
      environment.output.printf("\t%d /* %s */,\n", recoveryToken, id.getName());
    } else {
      environment.output.printf("\t%d /* %s */\n};\n\n", recoveryToken, id.getName());
    }
  }

  @Override
  public void generateTokensHeader(int terminals) {
    environment.include.printf("\n#define TOKENS %d\n", terminals);
    environment.output.printf("\nint StxTokens[TOKENS] = {\n");
  }

  @Override
  public void generateToken(Terminal id, boolean isLast) {
    if (isLast) {
      environment.output.printf("\t%d /* %s (%s)*/\n};\n\n", id.getToken(), id.getName(), id.getFullName());
    } else {
      environment.output.printf("\t%d, /* %s (%s) */\n", id.getToken(), id.getName(), id.getFullName());
    }
  }

  @Override
  public void generateTokenDefinitions() {
    boolean first = true;
    for (Terminal id : runtime.getTerminals()) {
      id.computeVariable();
      if (!id.getVariable().equals("_")) {
        if (first) {
          environment.include.printf("\n/* Token definitions */\n");
          first = false;
        }
        environment.include.printf("#define %s %d\n", id.getVariable(), id.getToken());
      }
    }
    environment.include.printf("\n");
    environment.include.printf("/* Token information structure */\n");
    environment.include.printf("typedef struct {\n");
    environment.include.printf("    char * name;\n");
    environment.include.printf("    char * fullName;\n");
    environment.include.printf("    int token;\n");
    environment.include.printf("    int reserved;\n");
    environment.include.printf("} TOKENDEF, *PTOKENDEF;\n\n");
    environment.output.printf("TOKENDEF StxTokenDefs[] = {\n");
    int i = 0;
    for (Terminal id : runtime.getTerminals()) {
      if (!id.getVariable().equals("_")) {
        environment.output.printf("    {\"%s\", \"%s\", %d, 1}", id.getVariable(), escapeDoubleQuotes(id.getFullName()), id.getToken());
      } else {
        environment.output.printf("    {\"%s\", \"%s\", %d, 0}", id.getName(), escapeDoubleQuotes(id.getFullName()), id.getToken());
      }
      i++;
      if (i < runtime.getTerminals().size()) {
        environment.output.print(",");
      }
      environment.output.println();
    }
    environment.output.printf("};\n");
  }

  @Override
  public void printCodeHeader() {
    environment.include.printf("#define FINAL %d\n" + "#define SYMBS %d\n\n",
        runtime.getStates().length, runtime.getTerminals().size() +
                                    runtime.getNonTerminals().size() -
                                    1);
    environment.include.printf("#define ACCEPT %d\n\n", Integer.MAX_VALUE);
    if (!environment.isPacked()) {
      environment.output.printf("/* Parsing Table */\n" + "int StxParsingTable[FINAL][SYMBS] = {\n");
      environment.output.print("   //");
      for (Terminal t : runtime.getTerminals()) {
        String name = getShortSymbolName(t);
        environment.output.printf("%6s ", name);
      }
      for (NonTerminal nt : runtime.getNonTerminals()) {
        if (nt == runtime.getRoot()) {
          continue;
        }
        String name = getShortSymbolName(nt);
        environment.output.printf("%6s ", name);
      }
      environment.output.println();
    }
  }

  @Override
  public void printTableRow(int symbolCounter, int parserLine[], int stateNumber) {
    environment.output.printf("    {");
    int index;
    for (int column = index = 0; index <= symbolCounter; ++index) {
      // If this column does not fit, cut with \n
      if ((column + 1) * 5 + 5 > environment.getMargin()) {
        environment.output.printf("\n     ");
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
      environment.output.printf("}\n};\n");
    } else {
      environment.output.printf("},\n");
    }
  }

  @Override
  public void printParserErrors() {
    if (environment.isPacked() == true) {
      return;
    }
    indent(environment.output, environment.getIndent()-1);
    environment.output.printf("// Parsing Errors\n");
    indent(environment.output, environment.getIndent()-1);
    environment.output.printf("int StxParsingError[FINAL] = {\n");
    int i = 0;
    for (State I : runtime.getStates()) {
      if (i == runtime.getStates().length - 1) {
        environment.output.printf("    /* State %3d */ %s  // %s\n", i, I.getMessage(), getErrorMessage(I));
        environment.output.printf("};\n\n");
      } else {
        environment.output.printf("    /* State %3d */ %s, // %s\n", i, I.getMessage(), getErrorMessage(I));
      }
      i++;
    }
  }  
  
  @Override
  public void printParsingTableHeader() {
    environment.include.printf(
          "/* Parsing Table definition */"
          + "typedef struct {\n"
          + "\tshort\tposition;\n"
          + "\tshort\tdefa;\n"
          + "\tshort\telements;\n"
          + "\tshort\tmsg;\n"
          + "} PARSER, *PPARSER;\n"
          + "\n");
    environment.output.printf("/* Parsing table */\nPARSER StxParsingTable[FINAL] = {\n");
  }

  @Override
  public void printPackedState(int stateNumber) {
    environment.output.printf("\t{%d, %d, %d, %d}", runtime.getStates()[stateNumber].getPosition(),
        runtime.getStates()[stateNumber].getDefaultValue(), runtime.getStates()[stateNumber].getActionSize(),
        runtime.getStates()[stateNumber].getMessage());
    if (stateNumber == runtime.getStates().length - 1) {
      environment.output.printf("\n};\n");
    } else {
      environment.output.printf(",\n");
    }
  }

  @Override
  public void printErrorTableHeader() {
    environment.output.printf("\nchar * StxErrorTable[] = {\n");
  }

  @Override
  public void printErrorEntry(int error) {
    environment.output.printf("\t\"%s\"", escapeDoubleQuotes(runtime.getErrorMessages().get(error)));
    if (error == runtime.getErrorMessages().size() - 1) {
      environment.output.printf("\n");
    } else {
      environment.output.printf(",\n");
    }
  }

  @Override
  public void printErrorFooter() {
    environment.output.printf("};\n\n");
  }

  @Override
  public void printActionHeader() {
    environment.include.printf(
          "/* Action table */\n"
          + "typedef struct {\n"
          + "\tunsigned long int\tsymbol;\n"
          + "\tunsigned long int\tstate;\n"
          + "} ACTION, *PACTION;\n"
          + "\n");
    environment.include.printf("#define ACTIONS %d\n\n", runtime.getNumberOfActions());
    environment.output.printf("\n" + "ACTION StxActionTable[ACTIONS] = {\n");
  }

  @Override
  public void printAction(int action, Action anAction) {
    environment.output.printf("\t{%d, %d}", anAction.getSymbol().getToken(), anAction.getStateNumber());
    if (action == runtime.getNumberOfActions() - 1) {
      environment.output.printf("\n};\n");
    } else {
      environment.output.printf(",\n");
    }
  }

  @Override
  public void printGoToTableHeader() {
    environment.include.printf(
          "/* Goto table */\n"
          + "typedef struct {\n"
          + "\tshort\torigin;\n"
          + "\tshort\tdestination;\n"
          + "} GOTOS, *PGOTOS;\n\n"
          + "#define NUM_GOTOS %d\n\n", runtime.getNumberOfGoTos());
    environment.output.printf("" + "\n" + "GOTOS StxGotoTable[NUM_GOTOS] = {\n");
  }

  @Override
  public void printGoTo(int gotoIndex, GoTo pGoto) {
    environment.output.printf("\t{%d, %d}", pGoto.getOrigin(), pGoto.getDestination());
    if (gotoIndex == runtime.getNumberOfGoTos() - 1) {
      environment.output.printf("\n};\n");
    } else {
      environment.output.printf(",\n");
    }
  }

  @Override
  public void printMissingSkeleton(String filename) {
    environment.output.println("  /* missing internal skeleton " + filename + " */");
  }

  @Override
  public void printGrammarTable() {
    environment.include.printf(
          "/* symbols y reductions table */\n"
          + "typedef struct {\n"
          + "\tshort\tsymbol;\n"
          + "\tshort\treductions;\n"
          + "} GRAMMAR, *PGRAMMAR;\n"
          + "\n");
    int numberOfRules = runtime.getRules().size();
    environment.output.printf("GRAMMAR StxGrammarTable[%d]={\n", numberOfRules);
    int index = 0;
    for (Rule stx : runtime.getRules()) {
      int itemSize = stx.getItems().size();
      environment.output.printf("\t{%d, %d}", environment.isPacked() ? stx.getLeftHand().getToken() : stx.getLeftHandId(), itemSize);
      if (++index == numberOfRules) {
        environment.output.printf("\n};\n\n");
      } else {
        environment.output.printf(",\n");
      }
    }
    if (environment.isPacked()) {
      environment.include.printf("#define NON_TERMINALS %d\n", runtime.getNonTerminals().size());
      environment.output.printf("int StxNonTerminals[NON_TERMINALS] = {\n");
      int i = 1;
      for (NonTerminal id : runtime.getNonTerminals()) {
        if (i == runtime.getNonTerminals().size()) {
          environment.output.printf("\t%d /* %s */\n};\n\n", id.getToken(), id.getName());
        } else {
          environment.output.printf("\t%d, /* %s */\n", id.getToken(), id.getName());
        }
        i++;
      }
    }
  }
}
