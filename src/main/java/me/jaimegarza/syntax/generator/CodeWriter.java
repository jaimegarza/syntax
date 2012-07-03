/*
Syntax is distibuted under the Revised, or 3-clause, BSD license
===================================================================
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
===================================================================
 */
package me.jaimegarza.syntax.generator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import me.jaimegarza.syntax.OutputException;
import me.jaimegarza.syntax.cli.Environment;
import me.jaimegarza.syntax.cli.Language;
import me.jaimegarza.syntax.definition.Action;
import me.jaimegarza.syntax.definition.GoTo;
import me.jaimegarza.syntax.definition.NonTerminal;
import me.jaimegarza.syntax.definition.Rule;
import me.jaimegarza.syntax.definition.State;

public class CodeWriter extends AbstractPhase {

  public CodeWriter(Environment environment, RuntimeData runtimeData) {
    super();
    this.environment = environment;
    this.runtimeData = runtimeData;
  }

  private void printHeader() {
    switch (environment.getLanguage()) {
      case pascal:
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
        environment.output.printf("\nConst\n  FINAL = %d;\n" + "  SYMBS = %5d;\n", runtimeData.getStates().length,
            runtimeData.getTerminals().size() + runtimeData.getNonTerminals().size() - 2);
        environment.output.printf("Type\n" + "  TABLEROWS = 0..FINAL;\n" + "  TABLECOLS = 0..SYMBS;\n" + "\n");
        if (!environment.isPacked()) {
          environment.output.printf("{Parsing Table}\n"
                                    + "    StxParsingTable : array [TABLEROWS,TABLECOLS] of Integer = (\n");
        }
        break;
      case C:
        // reserve a place for where the size of the table will be written
        // fPos = ftell(environment.output);
        environment.output.printf("\n" + "#define FINAL %d\n" + "#define SYMBS %5d\n\n",
            runtimeData.getStates().length, runtimeData.getTerminals().size() +
                                            runtimeData.getNonTerminals().size() -
                                              1);
        if (!environment.isPacked()) {
          environment.output.printf("/* Parsing Table */\n" + "int StxParsingTable[FINAL][SYMBS] = {\n");
        }
        break;
      case java:
        // reserve a place for where the size of the table will be written
        indent(environment.output, environment.getIndent() - 1);
        environment.output.printf("private static final int FINAL=%d;\n", runtimeData.getStates().length);
        // fPos = ftell(environment.output);
        indent(environment.output, environment.getIndent() - 1);
        environment.output.printf("private static final int SYMBS=%5d;\n\n", runtimeData.getTerminals().size() +
                                                                             runtimeData.getNonTerminals().size() -
                                                                               1);
        if (!environment.isPacked()) {
          indent(environment.output, environment.getIndent());
          environment.output.printf("// Parsing Table\n");
          indent(environment.output, environment.getIndent());
          environment.output.printf("private int parsingTable[][] = {\n");
        }
        break;
    }
  }

  private void printTableRow(State state) {
    int parserLine[] = state.getRow();
    int stateNumber = state.getId();
    int column;
    int index;

    if (environment.isPacked()) {
      return;
    }
    int symbolCounter = runtimeData.getTerminals().size() + runtimeData.getNonTerminals().size() - 2;
    switch (environment.getLanguage()) {
      case pascal:
        environment.output.print("        (");
        for (column = index = 0; index <= symbolCounter; ++index) {
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
        if (stateNumber == runtimeData.getStates().length - 1) {
          environment.output.printf("));\n");
        } else {
          environment.output.printf("),\n");
        }
        break;
      case C:
        environment.output.printf("    {");
        for (column = index = 0; index <= symbolCounter; ++index) {
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
        if (stateNumber == runtimeData.getStates().length - 1) {
          environment.output.printf("}\n};\n");
        } else {
          environment.output.printf("},\n");
        }
        break;
    }
  }

  private void printFooter() {
    int numberOfRules;
    int index;
    int itemSize;
    int i;

    if (environment.isPacked()) {
      printTables();
    }

    switch (environment.getLanguage()) {
      case pascal:
        environment.output.printf("\n");
        numberOfRules = runtimeData.getRules().size();
        environment.output.printf("  StxGrammarTable : Array [0..%d] of GRAMMAR = (\n", numberOfRules - 1);
        index = 0;
        for (Rule stx : runtimeData.getRules()) {
          itemSize = stx.getItems().size();
          environment.output.printf("    (symbol:%d; reductions:%d)", environment.isPacked() ? stx.getLeftHand()
              .getToken() : stx.getLeftHandId(), itemSize);
          if (++index == numberOfRules) {
            environment.output.printf(");\n");
          } else {
            environment.output.printf(",\n");
          }
        }
        if (environment.isPacked()) {
          environment.output.printf("\nConst\n  NON_TERMINALS = %d;\n", runtimeData.getNonTerminals().size() - 1);
          environment.output.printf("\n  StxNonTerminals : array [0..NON_TERMINALS] of INTEGER = (\n");
          i = 1;
          for (NonTerminal id : runtimeData.getNonTerminals()) {
            if (i == runtimeData.getNonTerminals().size()) {
              environment.output.printf("  %d, (* %s *));\n\n", id.getToken(), id.getName());
            } else {
              environment.output.printf("  %d, (* %s *)\n", id.getToken(), id.getName());
            }
            i++;
          }
        }
        break;
      case C:
        environment.output.printf("\n"
                                  + "/* symbols y reductions table */\n"
                                    + "typedef struct {\n"
                                    + "\tshort\tsymbol;\n"
                                    + "\tshort\treductions;\n"
                                    + "} GRAMMAR, *PGRAMMAR;\n"
                                    + "\n");
        numberOfRules = runtimeData.getRules().size();
        environment.output.printf("GRAMMAR StxGrammarTable[%d]={\n", numberOfRules);
        index = 0;
        for (Rule stx : runtimeData.getRules()) {
          itemSize = stx.getItems().size();
          environment.output.printf("\t{%d, %d}",
              environment.isPacked() ? stx.getLeftHand().getToken() : stx.getLeftHandId(), itemSize);
          if (++index == numberOfRules) {
            environment.output.printf("\n};\n\n");
          } else {
            environment.output.printf(",\n");
          }
        }
        if (environment.isPacked()) {
          environment.output.printf("\n#define NON_TERMINALS %d\n", runtimeData.getNonTerminals().size());
          environment.output.printf("\nint StxNonTerminals[NON_TERMINALS] = {\n");
          i = 1;
          for (NonTerminal id : runtimeData.getNonTerminals()) {
            if (i == runtimeData.getNonTerminals().size()) {
              environment.output.printf("\t%d /* %s */\n};\n\n", id.getToken(), id.getName());
            } else {
              environment.output.printf("\t%d, /* %s */\n", id.getToken(), id.getName());
            }
            i++;
          }
        }
        break;
      case java:
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
        numberOfRules = runtimeData.getRules().size();
        indent(environment.output, environment.getIndent() - 1);
        environment.output.printf("private Grammar grammarTable[]={\n");
        index = 0;
        for (Rule stx : runtimeData.getRules()) {
          itemSize = stx.getItems().size();
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
          environment.output.printf("private final int NON_TERMINALS=%d;\n", runtimeData.getNonTerminals().size());
          indent(environment.output, environment.getIndent() - 1);
          environment.output.printf("private final int nonTerminals[] = {\n");
          i = 1;
          for (NonTerminal id : runtimeData.getNonTerminals()) {
            indent(environment.output, environment.getIndent());
            if (i == runtimeData.getNonTerminals().size()) {
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
        if (!runtimeData.isStackTypeDefined()) {
          environment.output.printf("\n");
          indent(environment.output, environment.getIndent() - 1);
          environment.output.printf("private class LexicalValue {\n");
          indent(environment.output, environment.getIndent() - 1);
          environment.output.printf("}\n\n");
        }
        if (runtimeData.getRules().size() == 0) {
          /* header */
          switch (environment.getLanguage()) {

            case java:
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
              break;
          }
        }
        break;
    }
  }

  private void printTables() {
    int stateNumber, action, numGotos, error;

    switch (environment.getLanguage()) {
      case pascal:
        environment.output.printf("Const\n"
                                  + "  NUM_GOTOS = %d;\n"
                                    + "  ACTIONS = %d;\n\n"
                                    + "  StxActionTable : Array [0..ACTIONS-1] of ACTION = (\n",
            runtimeData.getNumberOfGoTos(), runtimeData.getNumberOfActions());
        break;
      case C:
        environment.output.printf("\n"
                                  + "/* Acction table */\n"
                                    + "typedef struct {\n"
                                    + "\tshort\tsymbol;\n"
                                    + "\tshort\tstate;\n"
                                    + "} ACTION, *PACTION;\n"
                                    + "\n");
        environment.output.printf("#define ACTIONS %d\n" + "\n" + "ACTION StxActionTable[ACTIONS] = {\n",
            runtimeData.getNumberOfActions());
        break;
      case java:
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
        environment.output.printf("private static final int ACTIONS=%d;\n\n", runtimeData.getNumberOfActions());
        indent(environment.output, environment.getIndent() - 1);
        environment.output.printf("private Action actionTable[] = {\n");
        break;
    }
    action = 0;
    for (stateNumber = 0; stateNumber < runtimeData.getStates().length; stateNumber++) {
      if (runtimeData.getStates()[stateNumber].getPosition() >= action) {
        for (Action anAction : runtimeData.getStates()[stateNumber].getActions()) {
          switch (environment.getLanguage()) {
            case C:
              environment.output.printf("\t{%d, %d}", anAction.getSymbol().getToken(), anAction.getStateNumber());
              if (action == runtimeData.getNumberOfActions() - 1) {
                environment.output.printf("\n};\n");
              } else {
                environment.output.printf(",\n");
              }
              break;
            case java:
              indent(environment.output, environment.getIndent());
              environment.output.printf("new Action(%d, %d)", anAction.getSymbol().getToken(), anAction.getStateNumber());
              if (action == runtimeData.getNumberOfActions() - 1) {
                environment.output.printf("\n");
                indent(environment.output, environment.getIndent() - 1);
                environment.output.printf("};\n");
              } else {
                environment.output.printf(",\n");
              }
              break;
            case pascal:
              environment.output
                  .printf("    (symbol:%d; state:%d)", anAction.getSymbol().getToken(), anAction.getStateNumber());
              if (action == runtimeData.getNumberOfActions() - 1) {
                environment.output.printf(");\n");
              } else {
                environment.output.printf(",\n");
              }
              break;
          }
          action++;
        }
      }
    }

    numGotos = 0;
    switch (environment.getLanguage()) {
      case C:
        environment.output.printf("\n"
                                  + "/* Goto table */\n"
                                    + "typedef struct {\n"
                                    + "\tshort\torigin;\n"
                                    + "\tshort\tdestination;\n"
                                    + "} GOTOS, *PGOTOS;\n"
                                    + "\n");
        environment.output.printf("#define NUM_GOTOS %d\n" + "\n" + "GOTOS StxGotoTable[NUM_GOTOS] = {\n",
            runtimeData.getNumberOfGoTos());
        break;
      case java:
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
        environment.output.printf("private static final int NUM_GOTOS=%d;\n", runtimeData.getNumberOfGoTos());
        indent(environment.output, environment.getIndent() - 1);
        environment.output.printf("private Goto gotoTable[] = {\n");
        break;
      case pascal:
        environment.output.printf("\n" + "  StxGotoTable : array [0..NUM_GOTOS-1] of GOTOS = (\n");
        break;
    }
    for (NonTerminal id : runtimeData.getNonTerminals()) {
      if (id.getGotos() != null && id.getGotos().size() > 0) {
        for (GoTo pGoto : id.getGotos()) {
          switch (environment.getLanguage()) {
            case C:
              environment.output.printf("\t{%d, %d}", pGoto.getOrigin(), pGoto.getDestination());
              if (numGotos == runtimeData.getNumberOfGoTos() - 1) {
                environment.output.printf("\n};\n");
              } else {
                environment.output.printf(",\n");
              }
              break;
            case java:
              indent(environment.output, environment.getIndent());
              environment.output.printf("new Goto(%d, %d)", pGoto.getOrigin(), pGoto.getDestination());
              if (numGotos == runtimeData.getNumberOfGoTos() - 1) {
                environment.output.printf("\n");
                indent(environment.output, environment.getIndent() - 1);
                environment.output.printf("};\n");
              } else {
                environment.output.printf(",\n");
              }
              break;
            case pascal:
              environment.output.printf("    (origin:%d; destination:%d)", pGoto.getOrigin(), pGoto.getDestination());
              if (numGotos == runtimeData.getNumberOfGoTos() - 1) {
                environment.output.printf(");\n");
              } else {
                environment.output.printf(",\n");
              }
              break;
          }
          numGotos++;
        }
      }
    }
    switch (environment.getLanguage()) {
      case C:
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
        break;
      case java:
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
        break;
      case pascal:
        environment.output.printf("\n" + "  StxParsingTable : array [TABLEROWS] of PARSER = (\n");
        break;
    }
    for (stateNumber = 0; stateNumber < runtimeData.getStates().length; stateNumber++) {
      switch (environment.getLanguage()) {
        case C:
          environment.output.printf("\t{%d, %d, %d, %d}", runtimeData.getStates()[stateNumber].getPosition(),
              runtimeData.getStates()[stateNumber].getDefaultValue(), runtimeData.getStates()[stateNumber].getActionSize(),
              runtimeData.getStates()[stateNumber].getMessage());
          if (stateNumber == runtimeData.getStates().length - 1) {
            environment.output.printf("\n};\n");
          } else {
            environment.output.printf(",\n");
          }
          break;
        case java:
          indent(environment.output, environment.getIndent());
          environment.output.printf("new Parser(%d, %d, %d, %d)", runtimeData.getStates()[stateNumber].getPosition(),
              runtimeData.getStates()[stateNumber].getDefaultValue(), runtimeData.getStates()[stateNumber].getActionSize(),
              runtimeData.getStates()[stateNumber].getMessage());
          if (stateNumber == runtimeData.getStates().length - 1) {
            environment.output.printf("\n");
            indent(environment.output, environment.getIndent() - 1);
            environment.output.printf("};\n");
          } else {
            environment.output.printf(",\n");
          }
          break;
        case pascal:
          environment.output.printf("    (position:%d; defa:%d; elements:%d; msg:%d)",
              runtimeData.getStates()[stateNumber].getPosition(), runtimeData.getStates()[stateNumber].getDefaultValue(),
              runtimeData.getStates()[stateNumber].getActionSize(), runtimeData.getStates()[stateNumber].getMessage());
          if (stateNumber == runtimeData.getStates().length - 1) {
            environment.output.printf(");\n");
          } else {
            environment.output.printf(",\n");
          }
          break;
      }
    }
    switch (environment.getLanguage()) {
      case C:
        environment.output.printf("\nchar * StxErrorTable[] = {\n");
        break;
      case java:
        environment.output.printf("\n");
        indent(environment.output, environment.getIndent() - 1);
        environment.output.printf("private String errorTable[] = {\n");
        break;
      case pascal:
        environment.output.printf("\n" + "  StxErrorTable : array [0..%d] of String = (\n", runtimeData
            .getErrorMessages().size() - 1);
        break;
    }
    for (error = 0; error < runtimeData.getErrorMessages().size(); error++) {
      switch (environment.getLanguage()) {
        case C:
          environment.output.printf("\t\"%s\"", runtimeData.getErrorMessages().get(error));
          if (error == runtimeData.getErrorMessages().size() - 1) {
            environment.output.printf("\n};\n");
          } else {
            environment.output.printf(",\n");
          }
          break;
        case java:
          indent(environment.output, environment.getIndent());
          environment.output.printf("\"%s\"", runtimeData.getErrorMessages().get(error));
          if (error == runtimeData.getErrorMessages().size() - 1) {
            environment.output.printf("\n");
            indent(environment.output, environment.getIndent() - 1);
            environment.output.printf("};\n");
          } else {
            environment.output.printf(",\n");
          }
          break;
        case pascal:
          environment.output.printf("    \'");
          String errorMessage = runtimeData.getErrorMessages().get(error);
          for (int i = 0; i < errorMessage.length(); i++) {
            char c = errorMessage.charAt(i);
            if (c == '\'') {
              environment.output.printf("\'");
            }
            environment.output.printf("%c", c);
          }
          environment.output.printf("\'");
          if (error == runtimeData.getErrorMessages().size() - 1) {
            environment.output.printf(");\n");
          } else {
            environment.output.printf(",\n");
          }
          break;
      }
    }
  }

  /**
   * TODO - add all the parser skeletons in a nice way.
   * TODO - make sure we have lexic driven parsers together with the run-once parsers
   * @throws IOException
   */
  private void finishOutput() throws IOException {
    String filename;

    filename = "skeletons/parser-skeleton-" +
               (environment.isPacked() ? "packed" : "expanded") +
                 environment.getLanguage().extension();
    ClassLoader loader = this.getClass().getClassLoader();
    InputStream is = loader.getResourceAsStream("classpath:" + filename);
    if (is != null) {
      try {
        if (environment.isVerbose()) {
          System.out.println("using skeleton " + filename);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        if (environment.getLanguage() == Language.C && environment.isEmitLine()) {
          environment.output.printf("#line 1 \"%s\"\n", filename);
        }
        String line = reader.readLine();
        while (line != null) {
          line = reader.readLine();
          environment.output.println(line);
        }
      } finally {
        is.close();
      }
    } else {
      System.err.println("\n\nWarning: internal skeleton \"" + filename + "\" not found.  Table was generated.\n");
      switch (environment.getLanguage()) {
        case C:
          environment.output.println("  /* missing internal skeleton " + filename + " */");
          break;
        case java:
          environment.output.println("  /* missing internal skeleton " + filename + " */");
          break;
        case pascal:
          environment.output.println("  (* missing internal skeleton " + filename + " *)");
          break;
      }
    }

    if (runtimeData.hasFinalActions() == false) {
      return;
    }

    environment.output.println();

    if (environment.getLanguage() == Language.C && environment.isEmitLine()) {
      environment.output.printf("#line %d \"%s\"\n", runtimeData.sourceLineNumber + 1, environment.getSourceFile().toString());
    }

    int c = environment.source.read();
    while (c != -1) {
      environment.output.print((char) c);
      c = environment.source.read();
    }
  }

  public void execute() throws OutputException {
    try {
      printHeader();
      if (environment.isPacked() == false) {
        for (int i = 0; i < runtimeData.getStates().length; i++) {
          printTableRow(runtimeData.getStates()[i]);
        }
      }
      printFooter();
      finishOutput();
    } catch (IOException e) {
      throw new OutputException("Error while creating output files", e);
    }
  }
}
