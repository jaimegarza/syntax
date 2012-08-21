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

import me.jaimegarza.syntax.Lexer;
import me.jaimegarza.syntax.definition.Action;
import me.jaimegarza.syntax.definition.ErrorToken;
import me.jaimegarza.syntax.definition.GoTo;
import me.jaimegarza.syntax.definition.NonTerminal;
import me.jaimegarza.syntax.definition.Rule;
import me.jaimegarza.syntax.definition.Terminal;

/**
 * Routines for supporting the java language
 *
 * @author jaimegarza@gmail.com
 *
 */
public class Java extends BaseLanguageSupport {

  @Override
  public String getLanguageCode() {
    return "java";
  }

  @Override
  public String getId() {
    return "j";
  }

  @Override
  public String getExtensionSuffix() {
    return ".java";
  }

  @Override
  public String getIncludeExtensionSuffix() {
    return "Intf.java";
  }
  
  @Override
  public boolean getDefaultIncludeFlag() {
    return false;
  }

  @Override
  public void generateCaseStart(int lineNumber, String label) {
    indent(environment.output, environment.getIndent() + 1);
    environment.output.printf("case %s: ", label);
    indent(environment.output, environment.getIndent() + 1);
  }
  
  @Override
  public void generateCaseEnd() {
    environment.output.println();
    indent(environment.output, environment.getIndent() + 2);
    environment.output.printf("break;\n");
  }

  @Override
  public void generateLexerHeader() {
    environment.output.printf("\n");
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("// LexicalRecognizer\n");
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("private char currentChar;\n\n");
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("int parserElement(boolean initialize) {\n");
    indent(environment.output, environment.getIndent());
    environment.output.printf("lexicalValue = new LexicalValue();\n\n");
  }

  @Override
  public void generateLexerFooter() {
    environment.output.println();
    indent(environment.output, environment.getIndent());
    environment.output.printf("return 0; // UNKNOWN\n");
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("}\n");
  }

  @Override
  public void generateCodeGeneratorHeader() {
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
  }

  @Override
  public void generateCodeGeneratorFooter() {
    indent(environment.output, environment.getIndent());
    environment.output.printf("}\n");
    indent(environment.output, environment.getIndent());
    environment.output.printf("return 1; // OK\n");
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("}\n");
  }

  @Override
  public void generateRecoveryTableHeader(int numberOfErrorTokens) {
        environment.output.printf("\n");
        indent(environment.output, environment.getIndent() - 1);
        environment.output.printf("private static final int RECOVERS=%d;\n\n", numberOfErrorTokens);
        indent(environment.output, environment.getIndent() - 1);

        environment.output.printf("// Contains tokens in compact mode, and column in matrix\n");
        indent(environment.output, environment.getIndent() - 1);
        if (numberOfErrorTokens != 0) {
          environment.output.printf("int recoverTable[] = {\n");
        } else {
          environment.output.printf("int recoverTable[] = {0};\n\n");
        }
  }

  @Override
  public boolean generateStructure(Lexer lexer) throws IOException {
    int level;

    indent(environment.include, 1);
    environment.include.printf("private class LexicalValue");
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
            environment.include.printf("\n\n");
            lexer.getCharacter();
            return true;
          }
        case '\n':
          indent(environment.include, 1);
          break;
      }
      lexer.getCharacter();
    }
  }

  @Override
  public void generateErrorToken(int recoveryToken, ErrorToken id, boolean isLast) {
    indent(environment.output, environment.getIndent());
    if (!isLast) {
      environment.output.printf("%d, // %s\n", recoveryToken, id.getName());
    } else {
      environment.output.printf("%d // %s\n", recoveryToken, id.getName());
      indent(environment.output, environment.getIndent() - 1);
      environment.output.printf("};\n\n");
    }
  }

  @Override
  public void generateTokensHeader(int terminals) {
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("private static int TOKENS=%d;\n", terminals);
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("private static int tokens[] = {\n");
  }

  @Override
  public void generateToken(Terminal id, boolean isLast) {
    indent(environment.output, environment.getIndent());
    if (isLast) {
      environment.output.printf("%d // %s (%s)\n", id.getToken(), id.getName(), id.getFullName());
      indent(environment.output, environment.getIndent() - 1);
      environment.output.printf("};\n\n");

    } else {
      environment.output.printf("%d, // %s (%s)\n", id.getToken(), id.getName(), id.getFullName());
    }
  }

  @Override
  public void generateTokenDefinitions() {
    indent(environment.include, environment.getIndent() - 1);
    environment.include.printf("// Token definitions\n");
    for (Terminal id : runtime.getTerminals()) {
      id.computeVariable();
      if (!id.getVariable().equals("_")) {
        indent(environment.include, environment.getIndent() - 1);
        environment.include.printf("private static final int %s=%d;\n", id.getVariable(), id.getToken());
      }
    }
    environment.include.printf("\n");
    indent(environment.include, environment.getIndent() - 1);
    environment.include.printf("private class TokenDef {\n");
    indent(environment.output, environment.getIndent());
    environment.include.printf("int token;\n");
    indent(environment.include, environment.getIndent());
    environment.include.printf("String name;\n");
    indent(environment.include, environment.getIndent());
    environment.include.printf("String fullName;\n");
    indent(environment.include, environment.getIndent());
    environment.include.printf("boolean reserved;\n\n");
    indent(environment.include, environment.getIndent());
    environment.include.printf("TokenDef(String name, String fullName, int token, boolean reserved) {\n");
    indent(environment.include, environment.getIndent() + 1);
    environment.include.printf("this.name = name;\n");
    indent(environment.include, environment.getIndent() + 1);
    environment.include.printf("this.fullName = fullName;\n");
    indent(environment.include, environment.getIndent() + 1);
    environment.include.printf("this.token = token;\n");
    indent(environment.include, environment.getIndent() + 1);
    environment.include.printf("this.reserved = reserved;\n");
    indent(environment.include, environment.getIndent());
    environment.include.printf("}\n");
    indent(environment.include, environment.getIndent() - 1);
    environment.include.printf("}\n\n");
    indent(environment.include, environment.getIndent() - 1);
    environment.include.printf("private TokenDef tokenDefs[] = {\n");
    int i = 0;
    for (Terminal id : runtime.getTerminals()) {
      indent(environment.include, environment.getIndent());
      if (!id.getVariable().equals("_")) {
        environment.include.printf("new TokenDef(\"%s\", \"%s\", %d, true)", id.getVariable(), id.getFullName(), id.getToken());
      } else {
        environment.include.printf("new TokenDef(\"%s\", \"%s\", %d, false)", id.getName(), id.getFullName(), id.getToken());
      }
      i++;
      if (i < runtime.getTerminals().size()) {
        environment.include.print(",");
      }
      environment.include.println();
    }
    indent(environment.include, environment.getIndent() - 1);
    environment.include.printf("};\n\n");
  }

  @Override
  public void printCodeHeader() {
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("private static final int FINAL=%d;\n", runtime.getStates().length);
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("private static final int SYMBS=%d;\n\n", runtime.getTerminals().size() +
                                                                         runtime.getNonTerminals().size() -
                                                                           1);
    if (!environment.isPacked()) {
      indent(environment.output, environment.getIndent());
      environment.output.printf("// Parsing Table\n");
      indent(environment.output, environment.getIndent());
      environment.output.printf("private int parsingTable[][] = {\n");
    }
  }

  @Override
  public void printTableRow(int symbolCounter, int[] parserLine, int stateNumber) {
  }

  @Override
  public void printParsingTableHeader() {
    environment.output.printf("\n");
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("// Parsing table\n");
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("private class Parser {\n");
    indent(environment.output, environment.getIndent());
    environment.output.printf("int position;\n");
    indent(environment.output, environment.getIndent());
    environment.output.printf("int defa;\n");
    indent(environment.output, environment.getIndent());
    environment.output.printf("int elements;\n");
    indent(environment.output, environment.getIndent());
    environment.output.printf("int msg;\n\n");
    indent(environment.output, environment.getIndent());
    environment.output.printf("Parser(int position, int defa, int elements, int msg) {\n");
    indent(environment.output, environment.getIndent() + 1);
    environment.output.printf("this.position = position;\n");
    indent(environment.output, environment.getIndent() + 1);
    environment.output.printf("this.defa = defa;\n");
    indent(environment.output, environment.getIndent() + 1);
    environment.output.printf("this.elements = elements;\n");
    indent(environment.output, environment.getIndent() + 1);
    environment.output.printf("this.msg = msg;\n");
    indent(environment.output, environment.getIndent());
    environment.output.printf("}\n");
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("}\n\n");
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("private Parser parsingTable[] = {\n");
  }

  @Override
  public void printPackedState(int stateNumber) {
    indent(environment.output, environment.getIndent());
    environment.output.printf("new Parser(%d, %d, %d, %d)", runtime.getStates()[stateNumber].getPosition(),
        runtime.getStates()[stateNumber].getDefaultValue(), runtime.getStates()[stateNumber].getActionSize(),
        runtime.getStates()[stateNumber].getMessage());
    if (stateNumber == runtime.getStates().length - 1) {
      environment.output.printf("\n");
      indent(environment.output, environment.getIndent() - 1);
      environment.output.printf("};\n");
    } else {
      environment.output.printf(",\n");
    }
  }

  @Override
  public void printErrorTableHeader() {
    environment.output.printf("\n");
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("private String errorTable[] = {\n");
  }

  @Override
  public void printErrorEntry(int error) {
    indent(environment.output, environment.getIndent());
    environment.output.printf("\"%s\"", runtime.getErrorMessages().get(error));
    if (error == runtime.getErrorMessages().size() - 1) {
      environment.output.printf("\n");
      indent(environment.output, environment.getIndent() - 1);
      environment.output.printf("};\n");
    } else {
      environment.output.printf(",\n");
    }
  }

  @Override
  public void printActionHeader() {
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("// Acction table\n");
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("private class Action {\n");
    indent(environment.output, environment.getIndent());
    environment.output.printf("private int symbol;\n");
    indent(environment.output, environment.getIndent());
    environment.output.printf("private int state;\n\n");
    indent(environment.output, environment.getIndent());
    environment.output.printf("Action(int symbol, int state) {\n");
    indent(environment.output, environment.getIndent() + 1);
    environment.output.printf("this.symbol = symbol;\n");
    indent(environment.output, environment.getIndent() + 1);
    environment.output.printf("this.state = state;\n");
    indent(environment.output, environment.getIndent());
    environment.output.printf("}\n");
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("}\n\n");
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("private static final int ACTIONS=%d;\n\n", runtime.getNumberOfActions());
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("private Action actionTable[] = {\n");
  }

  @Override
  public void printAction(int action, Action anAction) {
    indent(environment.output, environment.getIndent());
    environment.output.printf("new Action(%d, %d)", anAction.getSymbol().getToken(), anAction.getStateNumber());
    if (action == runtime.getNumberOfActions() - 1) {
      environment.output.printf("\n");
      indent(environment.output, environment.getIndent() - 1);
      environment.output.printf("};\n");
    } else {
      environment.output.printf(",\n");
    }
  }

  @Override
  public void printGoToTableHeader() {
    environment.output.printf("\n");
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("// Goto table\n");
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("private class Goto {\n");
    indent(environment.output, environment.getIndent());
    environment.output.printf("int origin;\n");
    indent(environment.output, environment.getIndent());
    environment.output.printf("int destination;\n\n");
    indent(environment.output, environment.getIndent());
    environment.output.printf("Goto(int origin, int destination) {\n");
    indent(environment.output, environment.getIndent() + 1);
    environment.output.printf("this.origin = origin;\n");
    indent(environment.output, environment.getIndent() + 1);
    environment.output.printf("this.destination = destination;\n");
    indent(environment.output, environment.getIndent());
    environment.output.printf("}\n");
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("}\n\n");
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("private static final int NUM_GOTOS=%d;\n", runtime.getNumberOfGoTos());
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("private Goto gotoTable[] = {\n");
  }

  @Override
  public void printGoTo(int gotoIndex, GoTo pGoto) {
    indent(environment.output, environment.getIndent());
    environment.output.printf("new Goto(%d, %d)", pGoto.getOrigin(), pGoto.getDestination());
    if (gotoIndex == runtime.getNumberOfGoTos() - 1) {
      environment.output.printf("\n");
      indent(environment.output, environment.getIndent() - 1);
      environment.output.printf("};\n");
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
    environment.output.printf("\n");
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("// symbols and reductions table\n");
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("private class Grammar {\n");
    indent(environment.output, environment.getIndent());
    environment.output.printf("int symbol;\n");
    indent(environment.output, environment.getIndent());
    environment.output.printf("int reductions;\n\n");
    indent(environment.output, environment.getIndent());
    environment.output.printf("Grammar(int symbol, int reductions) {\n");
    indent(environment.output, environment.getIndent() + 1);
    environment.output.printf("this.symbol = symbol;\n");
    indent(environment.output, environment.getIndent() + 1);
    environment.output.printf("this.reductions = reductions;\n");
    indent(environment.output, environment.getIndent());
    environment.output.printf("}\n");
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("}\n\n");
    int numberOfRules = runtime.getRules().size();
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("private Grammar grammarTable[]={\n");
    int index = 0;
    for (Rule stx : runtime.getRules()) {
      int itemSize = stx.getItems().size();
      indent(environment.output, environment.getIndent());
      environment.output.printf("new Grammar(%d, %d)",
          environment.isPacked() ? stx.getLeftHand().getToken() : stx.getLeftHandId(), itemSize);
      if (++index == numberOfRules) {
        environment.output.printf("\n");
        indent(environment.output, environment.getIndent() - 1);
        environment.output.printf("};\n\n");
      } else {
        environment.output.printf(",\n");
      }
    }
    if (environment.isPacked()) {
      indent(environment.output, environment.getIndent() - 1);
      environment.output.printf("private final int NON_TERMINALS=%d;\n", runtime.getNonTerminals().size());
      indent(environment.output, environment.getIndent() - 1);
      environment.output.printf("private final int nonTerminals[] = {\n");
      int i = 1;
      for (NonTerminal id : runtime.getNonTerminals()) {
        indent(environment.output, environment.getIndent());
        if (i == runtime.getNonTerminals().size()) {
          environment.output.printf("\n");
          indent(environment.output, environment.getIndent());
          environment.output.printf("%d // %s\n", id.getToken(), id.getName());
          indent(environment.output, environment.getIndent() - 1);
          environment.output.printf("};\n\n");
        } else {
          environment.output.printf("%d // %s,", id.getToken(), id.getName());
        }
        i++;
      }
    }
    if (!runtime.isStackTypeDefined()) {
      environment.output.printf("\n");
      indent(environment.output, environment.getIndent() - 1);
      environment.output.printf("private class LexicalValue {\n");
      indent(environment.output, environment.getIndent() - 1);
      environment.output.printf("}\n\n");
    }
    if (runtime.getRules().size() == 0) {
      /* header */
      environment.output.printf("\n");
      indent(environment.output, environment.getIndent());
      environment.output.printf("// Code Generator\n");
      indent(environment.output, environment.getIndent());
      environment.output.printf("private static final int STACK_DEPTH = 5000;\n");
      indent(environment.output, environment.getIndent());
      environment.output.printf("LexicalValue stack[] = new LexicalValue[STACK_DEPTH];\n");
      indent(environment.output, environment.getIndent());
      environment.output.printf("int stackTop;\n\n");
      indent(environment.output, environment.getIndent());
      environment.output.printf("int generateCode(int rule) {\n");
      indent(environment.output, environment.getIndent() + 1);
      environment.output.printf("return 1;\n");
      indent(environment.output, environment.getIndent());
      environment.output.printf("}\n\n");
    }
  }
}
