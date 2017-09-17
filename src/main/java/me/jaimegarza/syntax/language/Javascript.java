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

import me.jaimegarza.syntax.Lexer;
import me.jaimegarza.syntax.model.parser.Action;
import me.jaimegarza.syntax.model.parser.ErrorToken;
import me.jaimegarza.syntax.model.parser.GoTo;
import me.jaimegarza.syntax.model.parser.NonTerminal;
import me.jaimegarza.syntax.model.parser.Rule;
import me.jaimegarza.syntax.model.parser.State;
import me.jaimegarza.syntax.model.parser.Terminal;
import me.jaimegarza.syntax.util.FormattingPrintStream;

/**
 * Routines for supporting the javascript language
 *
 * @author jaimegarza@gmail.com
 *
 */
public class Javascript extends BaseLanguageSupport {

  @Override
  public String getLanguageCode() {
    return "javascript";
  }

  @Override
  public String getId() {
    return "js";
  }

  @Override
  public String getExtensionSuffix() {
    return ".js";
  }

  @Override
  public String getIncludeExtensionSuffix() {
    return "Intf.js";
  }
  
  @Override
  public int getDefaultIndent() {
    return 2; // JSLint
  }

  @Override
  public int getNumberOfSpacesPerIndent() {
    return 4;
  }

  @Override
  protected int getLexerCodeOffsetIndent() {
    return -1;
  }


@Override
  public int generateCaseStart(int lineNumber, String label, String comment) {
    indent(environment.output, environment.getIndent() + 1);
    environment.output.printf("// %s\n", comment);
    indent(environment.output, environment.getIndent() + 1);
    environment.output.printf("case %s:\n", label);
    String i = indent(environment.output, environment.getIndent() + 2);
    return i.length();
  }
  
  @Override
  public void generateCaseEnd() {
    environment.output.println();
    indent(environment.output, environment.getIndent() + 2);
    environment.output.printf("break;\n");
  }

  @Override
  public void generateLexerHeader(List<String> modes) {
    environment.output.println();
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("// LexicalRecognizer\n");
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("var parserElementMode = DEFAULT_LEXER_MODE;\n");
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("var currentChar;\n\n");
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("var recognized;\n\n");
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("function parserElement (initialize) {\n");
    indent(environment.output, environment.getIndent());
    environment.output.printf("if (initialize) {\n");
    indent(environment.output, environment.getIndent()+1);
    environment.output.printf("currentChar = getNextChar(true);\n");
    indent(environment.output, environment.getIndent());
    environment.output.printf("}\n\n");
    indent(environment.output, environment.getIndent());
    environment.output.printf("lexicalValue = new StackElement();\n\n");
    
    if (environment.lexerModes.size() > 1) {
      indent(environment.output, environment.getIndent());
      environment.output.println("switch (parserElementMode) {\n");
    } else {
      environment.output.println(environment.lexerModes.get("default").getWriter().toString());
    }
  }

  @Override
  public void generateLexerModeDefinition(String lexerMode, int index) {
    indent(environment.include, environment.getIndent() - 1);
    environment.include.println("var " + computeModeName(lexerMode).toUpperCase() + "_LEXER_MODE = " + index + ";");
  }

  @Override
  public void generateLexerModeCase(String lexerMode, int index) {
    indent(environment.output, environment.getIndent() + 1);
    environment.output.println("case " + computeModeName(lexerMode).toUpperCase() + "_LEXER_MODE:");
    indent(environment.output, environment.getIndent() + 2);
    environment.output.println("return parserElement_" + computeModeName(lexerMode) + "();");
    environment.output.println();
  }

  @Override
  public void generateLexerModeHeader(String lexerMode) {
    environment.output.println();
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("function parserElement_" + computeModeName(lexerMode) + " () {\n");
  }

  @Override
  public String getLexerModeRoutine(String lexerMode) {
    return "parserElement_" + computeModeName(lexerMode) + "()";
  }
    
  @Override
  public void generateLexerModeFooter(String lexerMode) {
    if (environment.lexerModes.size() > 1) {
      indent(environment.output, environment.getIndent());
      environment.output.printf("return 0; // UNKNOWN\n");
    } else {
      environment.output.println();
    }
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("}\n");
  }

  @Override
  public void generateLexerFooter() {
    if (environment.lexerModes.size() > 1) {
      indent(environment.output, environment.getIndent());
      environment.output.println("}");
      environment.output.println();
    }
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
    environment.output.printf("var STACK_DEPTH = 5000;\n");
    indent(environment.output, 1);
    environment.output.printf("var stack = new Array(STACK_DEPTH);\n");
    indent(environment.output, 1);
    environment.output.printf("var stackTop;\n\n");
    indent(environment.output, 1);
    environment.output.printf("function generateCode (rule) {\n");
    indent(environment.output, environment.getIndent());
    environment.output.printf("switch(rule){\n");
    environment.output.println();
  }

  @Override
  public void generateCodeGeneratorFooter() {
    indent(environment.output, environment.getIndent());
    environment.output.printf("}\n");
    indent(environment.output, environment.getIndent());
    environment.output.printf("return true; // OK\n");
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("}\n");
  }
  
  @Override
  public void generateVoidCodeGenerator() {
    environment.output.printf("\n");
    indent(environment.output, environment.getIndent());
    environment.output.printf("// Code Generator\n");
    indent(environment.output, environment.getIndent());
    environment.output.printf("var STACK_DEPTH = 5000;\n");
    indent(environment.output, environment.getIndent());
    environment.output.printf("var stack = [];\n");
    indent(environment.output, environment.getIndent());
    environment.output.printf("var stackTop;\n\n");
    indent(environment.output, environment.getIndent());
    environment.output.printf("function generateCode (rule) {\n");
    indent(environment.output, environment.getIndent() + 1);
    environment.output.printf("return true;\n");
    indent(environment.output, environment.getIndent());
    environment.output.printf("}\n\n");
  }

  @Override
  public void generateRecoveryTableHeader(int numberOfErrorTokens) {
        environment.output.printf("\n");
        indent(environment.output, environment.getIndent() - 1);
        environment.output.printf("var RECOVERS = %d;\n\n", numberOfErrorTokens);
        indent(environment.output, environment.getIndent() - 1);

        environment.output.printf("// Contains token ids\n");
        indent(environment.output, environment.getIndent() - 1);
        if (numberOfErrorTokens != 0) {
          environment.output.printf("var recoverTable = [\n");
        } else {
          environment.output.printf("var recoverTable = [0];\n\n");
        }
  }

  @Override
  public boolean generateStructure(Lexer lexer) {
    int level;

    indent(environment.include, environment.getIndent()-1);
    environment.include.printf("function StackElement()");
    level = 0;
    while (2 > 1) {
      if (lexer.getCurrentCharacter() == '\0') {
        environment.error(-1, "End of file processing \'%%union\'.");
        return false;
      }

      environment.include.print(lexer.getCurrentCharacter());
      switch (lexer.getCurrentCharacter()) {
        case '{':
          ++level;
          break;

        case '}':
          --level;
          if (level == 0) {
            environment.include.printf("\n\n");
            lexer.getNextCharacter();
            return true;
          }
        case '\n':
          indent(environment.include, environment.getIndent()-1);
          break;
      }
      lexer.getNextCharacter();
    }
  }

  @Override
  public void generateErrorToken(int recoveryToken, ErrorToken id, boolean isLast) {
    indent(environment.output, environment.getIndent());
    if (!isLast) {
      environment.output.printf("%d, // %s : \"%s\"\n", recoveryToken, id.getName(), id.getFullName());
    } else {
      environment.output.printf("%d // %s : \"%s\"\n", recoveryToken, id.getName(), id.getFullName());
      indent(environment.output, environment.getIndent() - 1);
      environment.output.printf("];\n\n");
    }
  }

  @Override
  public void generateTokensHeader(int terminals) {
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("var TOKENS = %d;\n\n", terminals);
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("var tokens = [\n");
  }

  @Override
  public void generateToken(Terminal id, boolean isLast) {
    indent(environment.output, environment.getIndent());
    if (isLast) {
      environment.output.printf("%d // %s (%s)\n", id.getToken(), id.getName(), id.getFullName());
      indent(environment.output, environment.getIndent() - 1);
      environment.output.printf("];\n\n");

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
        environment.include.printf("var %s=%d;\n", id.getVariable(), id.getToken());
      }
    }
    environment.include.printf("\n");
    indent(environment.include, environment.getIndent() - 1);
    environment.include.printf("var tokenDefs = [\n");
    int i = 0;
    for (Terminal id : runtime.getTerminals()) {
      indent(environment.include, environment.getIndent());
      if (!id.getVariable().equals("_")) {
        environment.include.printf("{token:%6d, reserved:true,  name:\"%s\", fullName:\"%s\"}", id.getToken(), id.getVariable(), escapeDoubleQuotes(id.getFullName()));
      } else {
        environment.include.printf("{token:%6d, reserved:false, name:\"%s\", fullName:\"%s\"}", id.getToken(), id.getName(), escapeDoubleQuotes(id.getFullName()));
      }
      i++;
      if (i < runtime.getTerminals().size()) {
        environment.include.print(",");
      }
      environment.include.println();
    }
    indent(environment.include, environment.getIndent() - 1);
    environment.include.printf("];\n\n");
  }

  @Override
  public void printCodeHeader() {
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("var FINAL = %d;\n", runtime.getStates().length);
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("var SYMBS = %d;\n\n", runtime.getTerminals().size() + runtime.getNonTerminals().size() - 1);
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("var ACCEPT= Number.MAX_SAFE_INTEGER;\n\n");
    if (environment.isPacked() == false) {
      indent(environment.output, environment.getIndent()-1);
      environment.output.printf("// Parsing Table\n");
      indent(environment.output, environment.getIndent()-1);
      environment.output.printf("var parsingTable = [\n");
      indent(environment.output, environment.getIndent()-1);
      environment.output.print("        //  ");
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
  public void printParserErrors() {
    if (environment.isPacked() == true) {
      return;
    }
    indent(environment.output, environment.getIndent()-1);
    environment.output.printf("\n// Parsing Errors\n");
    indent(environment.output, environment.getIndent()-1);
    environment.output.printf("var parsingError = [\n");
    int i = 0;
    for (State I : runtime.getStates()) {
      indent(environment.output, environment.getIndent());
      if (i == runtime.getStates().length - 1) {
        environment.output.printf(" /* %3d */ %s  // %s\n", i, I.getMessage(), getErrorMessage(I));
        indent(environment.output, environment.getIndent()-1);
        environment.output.printf("];\n");
      } else {
        environment.output.printf(" /* %3d */ %s, // %s\n", i, I.getMessage(), getErrorMessage(I));
      }
      i++;
    }
  }

  @Override
  public void printTableRow(int symbolCounter, int[] parserLine, int stateNumber) {
    indent(environment.output, environment.getIndent() );
    environment.output.printf(" /*%3d*/ [", stateNumber);
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
      environment.output.printf("]\n");
      indent(environment.output, environment.getIndent()-1);
      environment.output.printf("];\n");
    } else {
      environment.output.printf("],\n");
    }
  }

  @Override
  public void printParsingTableHeader() {
    environment.output.printf("\n");
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("// Parsing table\n");
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("var parsingTable = [\n");
  }

  @Override
  public void printPackedState(int stateNumber) {
    indent(environment.output, environment.getIndent());
    environment.output.printf("{position:%6d, defa:%6d, elements:%6d, msg:%6d}", runtime.getStates()[stateNumber].getPosition(),
        runtime.getStates()[stateNumber].getDefaultValue(), runtime.getStates()[stateNumber].getActionSize(),
        runtime.getStates()[stateNumber].getMessage());
    if (stateNumber == runtime.getStates().length - 1) {
      environment.output.printf("\n");
      indent(environment.output, environment.getIndent() - 1);
      environment.output.printf("];\n");
    } else {
      environment.output.printf(",\n");
    }
  }

  @Override
  public void printErrorTableHeader() {
    environment.output.printf("\n");
    indent(environment.output, environment.getIndent()-1);
    environment.output.printf("// Error Messages\n");
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("var errorTable = [\n");
  }

  @Override
  public void printErrorEntry(int error) {
    indent(environment.output, environment.getIndent());
    String errorMessage = runtime.getErrorMessages().get(error);
    if (environment.bundle == null) {
      environment.output.printf(" /* %d */ \"%s\"", error, escapeDoubleQuotes(errorMessage));
    } else {
      environment.output.printf(" \"%s.error%07d\" /* %d - %s */", environment.getBundleName(), error, error, errorMessage);
      environment.bundle.printf("%s.error%07d=%s\n", environment.getBundleName(), error, errorMessage);
    }

    if (error == runtime.getErrorMessages().size() - 1) {
      environment.output.printf("\n");
    } else {
      environment.output.printf(",\n");
    }
  }

  @Override
  public void printErrorFooter() {
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("];\n");
  }

  @Override
  public void printActionHeader() {
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("// Acction table\n");
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("var ACTIONS = %d;\n\n", runtime.getNumberOfActions());
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("var actionTable = [\n");
  }

  @Override
  public void printAction(int action, Action anAction) {
    indent(environment.output, environment.getIndent());
    environment.output.printf("{symbol:%6d, state:%6d}", anAction.getSymbol().getToken(), anAction.getStateNumber());
    if (action == runtime.getNumberOfActions() - 1) {
      environment.output.printf("\n");
      indent(environment.output, environment.getIndent() - 1);
      environment.output.printf("];\n");
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
    environment.output.printf("var NUM_GOTOS = %d;\n\n", runtime.getNumberOfGoTos());
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("var gotoTable = [\n");
  }

  @Override
  public void printGoTo(int gotoIndex, GoTo pGoto) {
    indent(environment.output, environment.getIndent());
    environment.output.printf("{origin:%6d, destination:%6d}", pGoto.getOrigin(), pGoto.getDestination());
    if (gotoIndex == runtime.getNumberOfGoTos() - 1) {
      environment.output.printf("\n");
      indent(environment.output, environment.getIndent() - 1);
      environment.output.printf("];\n");
    } else {
      environment.output.printf(",\n");
    }
  }

  @Override
  public void printMissingSkeleton(String filename) {
    environment.output.println("  // missing internal skeleton " + filename);
  }

  @Override
  public void printGrammarTable() {
    environment.output.printf("\n");
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("// symbols and reductions table\n");
    int numberOfRules = runtime.getRules().size();
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("var grammarTable = [\n");
    int index = 0;
    for (Rule stx : runtime.getRules()) {
      int itemSize = stx.getItems().size();
      indent(environment.output, environment.getIndent());
      environment.output.printf("/*Rule %3d */ {symbol:%6d, reductions:%6d}", stx.getRulenum(),
          environment.isPacked() ? stx.getLeftHand().getToken() : stx.getLeftHandId(), itemSize);
      if (++index == numberOfRules) {
        environment.output.printf("\n");
        indent(environment.output, environment.getIndent() - 1);
        environment.output.printf("];\n\n");
      } else {
        environment.output.printf(",\n");
      }
    }
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("var  NON_TERMINALS = %d;\n\n", runtime.getNonTerminals().size());
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("var nonTerminals = [\n");
    int i = 1;
    for (NonTerminal id : runtime.getNonTerminals()) {
      indent(environment.output, environment.getIndent());
      if (i == runtime.getNonTerminals().size()) {
        environment.output.printf("%d // %s\n", environment.isPacked()?id.getToken():id.getId(), id.getName());
        indent(environment.output, environment.getIndent() - 1);
        environment.output.printf("];\n\n");
      } else {
        environment.output.printf("%d,// %s\n", environment.isPacked()?id.getToken():id.getId(), id.getName());
      }
      i++;
    }
    if (!runtime.isStackTypeDefined()) {
      environment.output.printf("\n");
      indent(environment.output, environment.getIndent() - 1);
      environment.output.printf("function StackElement() {\n");
      indent(environment.output, environment.getIndent() - 1);
      environment.output.printf("}\n\n");
    }
  }

  @Override
  public void generateEdgeHeader(int size) {
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("var EDGES = %d;\n\n", size);
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("var edgeTable = [\n");
  }

  @Override
  public void generateVertexHeader(int size) {
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("var VERTICES = %d;\n\n", size);
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("var vertexTable = [\n");
  }

  @Override
  public void generateIntArrayRow(int i, String comment, int index, int maxSize) {
    indent(environment.output, environment.getIndent());
    environment.output.printf("%5d%c%s\n", i, index < maxSize - 1 ? ',' : ' ', comment == null ? "": " // " + comment);
  }

  @Override
  public void generateIntArrayComment(String comment) {
    indent(environment.output, environment.getIndent());
    environment.output.printf("%s\n",  comment == null ? "": "// " + comment);
  }

  @Override
  public void generateIntArrayFooter() {
    indent(environment.output, environment.getIndent() - 1);
    environment.output.printf("];\n\n");
  }

  @Override
  public void generateRegexMatch(FormattingPrintStream output, int dfaNode) {
    indent(output, environment.getIndent());
    output.printf("if (matchesRegex(%d)) {", dfaNode);
  }

  @Override
  public void generateRegexReturn(FormattingPrintStream output, Terminal token) {
    output.printf("\n");
    indent(output, environment.getIndent() + 1);
    output.printf("return %s\n;", token.getName());
  }

  @Override
  public void generateRegexEnd(FormattingPrintStream output) {
    indent(output, environment.getIndent());
    output.printf("}\n\n");
  }
}
