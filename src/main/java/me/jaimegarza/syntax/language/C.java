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
import me.jaimegarza.syntax.definition.ErrorToken;
import me.jaimegarza.syntax.definition.GoTo;
import me.jaimegarza.syntax.definition.NonTerminal;
import me.jaimegarza.syntax.definition.Rule;
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
    environment.output.printf("\n")
                      .printf("/* Lexical Recognizer */\n")
                      .printf("\n")
                      .printf("char StxChar;")
                      .printf("\n")
                      .printf("int StxLexer()\n")
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
    environment.output.printf("\n#define RECOVERS %d\n\n"
                              + "/* Contains tokens in compact mode, and column in matrix */", numberOfErrorTokens);
    if (numberOfErrorTokens != 0) {
      environment.output.printf("\nint StxRecoverTable[RECOVERS] = {\n");
    } else {
      environment.output.printf("\nint StxRecoverTable[1] = {0};\n\n");
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
    environment.output.printf("\n#define TOKENS %d\n", terminals);
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
      if (id.getVariable().equals("_")) {
        if (first) {
          environment.include.printf("\n/* Token definitions */\n");
          first = false;
        }
        environment.include.printf("#define %s %d\n", id.getVariable(), id.getToken());
      }
    }
    environment.include.printf("\n");
  }

  @Override
  public void printCodeHeader() {
    environment.output.printf("\n" + "#define FINAL %d\n" + "#define SYMBS %5d\n\n",
        runtime.getStates().length, runtime.getTerminals().size() +
                                    runtime.getNonTerminals().size() -
                                    1);
    if (!environment.isPacked()) {
      environment.output.printf("/* Parsing Table */\n" + "int StxParsingTable[FINAL][SYMBS] = {\n");
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
      environment.output.printf("%4d", parserLine[index]);
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
  public void printParsingTableHeader() {
    environment.output.printf("\n"
        + "/* Parsing table */\n"
          + "typedef struct {\n"
          + "\tshort\tposition;\n"
          + "\tshort\tdefa;\n"
          + "\tshort\telements;\n"
          + "\tshort\tmsg;\n"
          + "} PARSER, *PPARSER;\n"
          + "\n");
    environment.output.printf("PARSER StxParsingTable[FINAL] = {\n");
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
    environment.output.printf("\t\"%s\"", runtime.getErrorMessages().get(error));
    if (error == runtime.getErrorMessages().size() - 1) {
      environment.output.printf("\n};\n");
    } else {
      environment.output.printf(",\n");
    }
  }

  @Override
  public void printActionHeader() {
    environment.output.printf("\n"
        + "/* Acction table */\n"
          + "typedef struct {\n"
          + "\tshort\tsymbol;\n"
          + "\tshort\tstate;\n"
          + "} ACTION, *PACTION;\n"
          + "\n");
    environment.output.printf("#define ACTIONS %d\n" + "\n" + "ACTION StxActionTable[ACTIONS] = {\n",
        runtime.getNumberOfActions());
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
    environment.output.printf("\n"
        + "/* Goto table */\n"
          + "typedef struct {\n"
          + "\tshort\torigin;\n"
          + "\tshort\tdestination;\n"
          + "} GOTOS, *PGOTOS;\n"
          + "\n");
    environment.output.printf("#define NUM_GOTOS %d\n" + "\n" + "GOTOS StxGotoTable[NUM_GOTOS] = {\n",
        runtime.getNumberOfGoTos());
  }

  @Override
  public void printGoTo(int numGotos, GoTo pGoto) {
    environment.output.printf("\t{%d, %d}", pGoto.getOrigin(), pGoto.getDestination());
    if (numGotos == runtime.getNumberOfGoTos() - 1) {
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
    environment.output.printf("\n"
        + "/* symbols y reductions table */\n"
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
      environment.output.printf("\n#define NON_TERMINALS %d\n", runtime.getNonTerminals().size());
      environment.output.printf("\nint StxNonTerminals[NON_TERMINALS] = {\n");
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
