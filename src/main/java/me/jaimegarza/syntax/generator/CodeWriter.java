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
        Tabea(environment.output, environment.getIndent() - 1);
        environment.output.printf("private static final int FINAL=%d;\n", runtimeData.getStates().length);
        // fPos = ftell(environment.output);
        Tabea(environment.output, environment.getIndent() - 1);
        environment.output.printf("private static final int SYMBS=%5d;\n\n", runtimeData.getTerminals().size() +
                                                                             runtimeData.getNonTerminals().size() -
                                                                               1);
        if (!environment.isPacked()) {
          Tabea(environment.output, environment.getIndent());
          environment.output.printf("// Parsing Table\n");
          Tabea(environment.output, environment.getIndent());
          environment.output.printf("private int parsingTable[][] = {\n");
        }
        break;
    }
  }

  private void printTableRow(State state) {
    int parserLine[] = state.getRow();
    int estado = state.getId();
    int col;
    int t2;

    if (environment.isPacked()) {
      return;
    }
    int t1 = runtimeData.getTerminals().size() + runtimeData.getNonTerminals().size() - 2;
    switch (environment.getLanguage()) {
      case pascal:
        environment.output.print("        (");
        for (col = t2 = 0; t2 <= t1; ++t2) {
          // If this column does not fit, cut with \n
          if ((col + 1) * 5 + 9 > environment.getMargin()) {
            environment.output.printf("\n         ");
            col = 0;
          }
          col++;
          environment.output.printf("%4d", parserLine[t2]);
          if (t2 < t1) {
            environment.output.printf(",");
          }
        }
        if (estado == runtimeData.getStates().length - 1) {
          environment.output.printf("));\n");
        } else {
          environment.output.printf("),\n");
        }
        break;
      case C:
        environment.output.printf("    {");
        for (col = t2 = 0; t2 <= t1; ++t2) {
          // If this column does not fit, cut with \n
          if ((col + 1) * 5 + 5 > environment.getMargin()) {
            environment.output.printf("\n     ");
            col = 0;
          }
          col++;
          environment.output.printf("%4d", parserLine[t2]);
          if (t2 < t1) {
            environment.output.printf(",");
          }
        }
        if (estado == runtimeData.getStates().length - 1) {
          environment.output.printf("}\n};\n");
        } else {
          environment.output.printf("},\n");
        }
        break;
    }
  }

  private void printFooter() {
    int c;
    int t1;
    int t2;
    int i;

    if (environment.isPacked()) {
      ImprimeTablas();
    }

    switch (environment.getLanguage()) {
      case pascal:
        environment.output.printf("\n");
        c = runtimeData.getRules().size();
        environment.output.printf("  StxGrammarTable : Array [0..%d] of GRAMMAR = (\n", c - 1);
        t1 = 0;
        for (Rule stx : runtimeData.getRules()) {
          t2 = stx.getItems().size();
          environment.output.printf("    (symbol:%d; reductions:%d)", environment.isPacked() ? stx.getLeftHand()
              .getToken() : stx.getLeftHandId(), t2);
          if (++t1 == c) {
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
        // fseek(environment.output,fPos+19l,SEEK_SET);
        // environment.output.printf("%5d",runtimeData.getStates().length);
        break;
      case C:
        environment.output.printf("\n"
                                  + "/* symbols y reductions table */\n"
                                    + "typedef struct {\n"
                                    + "\tshort\tsymbol;\n"
                                    + "\tshort\treductions;\n"
                                    + "} GRAMMAR, *PGRAMMAR;\n"
                                    + "\n");
        c = runtimeData.getRules().size();
        environment.output.printf("GRAMMAR StxGrammarTable[%d]={\n", c);
        t1 = 0;
        for (Rule stx : runtimeData.getRules()) {
          t2 = stx.getItems().size();
          environment.output.printf("\t{%d, %d}",
              environment.isPacked() ? stx.getLeftHand().getToken() : stx.getLeftHandId(), t2);
          if (++t1 == c) {
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
        // fseek(environment.output,fPos+16l,SEEK_SET);
        // environment.output.printf("%5d",runtimeData.getStates().length + 1);
        break;
      case java:
        environment.output.printf("\n");
        Tabea(environment.output, environment.getIndent() - 1);
        environment.output.printf("// symbols and reductions table\n");
        Tabea(environment.output, environment.getIndent() - 1);
        environment.output.printf("private class Grammar {\n");
        Tabea(environment.output, environment.getIndent());
        environment.output.printf("int symbol;\n");
        Tabea(environment.output, environment.getIndent());
        environment.output.printf("int reductions;\n\n");
        Tabea(environment.output, environment.getIndent());
        environment.output.printf("Grammar(int symbol, int reductions) {\n");
        Tabea(environment.output, environment.getIndent() + 1);
        environment.output.printf("this.symbol = symbol;\n");
        Tabea(environment.output, environment.getIndent() + 1);
        environment.output.printf("this.reductions = reductions;\n");
        Tabea(environment.output, environment.getIndent());
        environment.output.printf("}\n");
        Tabea(environment.output, environment.getIndent() - 1);
        environment.output.printf("}\n\n");
        c = runtimeData.getRules().size();
        Tabea(environment.output, environment.getIndent() - 1);
        environment.output.printf("private Grammar grammarTable[]={\n");
        t1 = 0;
        for (Rule stx : runtimeData.getRules()) {
          t2 = stx.getItems().size();
          Tabea(environment.output, environment.getIndent());
          environment.output.printf("new Grammar(%d, %d)",
              environment.isPacked() ? stx.getLeftHand().getToken() : stx.getLeftHandId(), t2);
          if (++t1 == c) {
            environment.output.printf("\n");
            Tabea(environment.output, environment.getIndent() - 1);
            environment.output.printf("};\n\n");
          } else {
            environment.output.printf(",\n");
          }
        }
        if (environment.isPacked()) {
          Tabea(environment.output, environment.getIndent() - 1);
          environment.output.printf("private final int NON_TERMINALS=%d;\n", runtimeData.getNonTerminals().size());
          Tabea(environment.output, environment.getIndent() - 1);
          environment.output.printf("private final int nonTerminals[] = {\n");
          i = 1;
          for (NonTerminal id : runtimeData.getNonTerminals()) {
            Tabea(environment.output, environment.getIndent());
            if (i == runtimeData.getNonTerminals().size()) {
              environment.output.printf("\n");
              Tabea(environment.output, environment.getIndent());
              environment.output.printf("%d // %s\n", id.getToken(), id.getName());
              Tabea(environment.output, environment.getIndent() - 1);
              environment.output.printf("};\n\n");
            } else {
              environment.output.printf("%d // %s,", id.getToken(), id.getName());
            }
            i++;
          }
        }
        if (!runtimeData.isStackTypeDefined()) {
          environment.output.printf("\n");
          Tabea(environment.output, environment.getIndent() - 1);
          environment.output.printf("private class LexicalValue {\n");
          Tabea(environment.output, environment.getIndent() - 1);
          environment.output.printf("}\n\n");
        }
        if (runtimeData.getRules().size() == 0) {
          /* header */
          switch (environment.getLanguage()) {

            case java:
              environment.output.printf("\n");
              Tabea(environment.output, environment.getIndent());
              environment.output.printf("// Code Generator\n");
              Tabea(environment.output, environment.getIndent());
              environment.output.printf("private static final int STACK_DEPTH = 5000;\n");
              Tabea(environment.output, environment.getIndent());
              environment.output.printf("LexicalValue stack[] = new LexicalValue[STACK_DEPTH];\n");
              Tabea(environment.output, environment.getIndent());
              environment.output.printf("int stackTop;\n\n");
              Tabea(environment.output, environment.getIndent());
              environment.output.printf("int generateCode(int rule) {\n");
              Tabea(environment.output, environment.getIndent() + 1);
              environment.output.printf("return 1;\n");
              Tabea(environment.output, environment.getIndent());
              environment.output.printf("}\n\n");
              break;
          }
        }

        // fseek(environment.output,fPos,SEEK_SET);
        // environment.output.printf("%5d",runtimeData.getStates().length + 1);
        break;
    }
  }

  private void ImprimeTablas() {
    int estado, accion, num_gotos, err;

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
        Tabea(environment.output, environment.getIndent() - 1);
        environment.output.printf("// Acction table\n");
        Tabea(environment.output, environment.getIndent() - 1);
        environment.output.printf("private class Action {\n");
        Tabea(environment.output, environment.getIndent());
        environment.output.printf("private int symbol;\n");
        Tabea(environment.output, environment.getIndent());
        environment.output.printf("private int state;\n\n");
        Tabea(environment.output, environment.getIndent());
        environment.output.printf("Action(int symbol, int state) {\n");
        Tabea(environment.output, environment.getIndent() + 1);
        environment.output.printf("this.symbol = symbol;\n");
        Tabea(environment.output, environment.getIndent() + 1);
        environment.output.printf("this.state = state;\n");
        Tabea(environment.output, environment.getIndent());
        environment.output.printf("}\n");
        Tabea(environment.output, environment.getIndent() - 1);
        environment.output.printf("}\n\n");
        Tabea(environment.output, environment.getIndent() - 1);
        environment.output.printf("private static final int ACTIONS=%d;\n\n", runtimeData.getNumberOfActions());
        Tabea(environment.output, environment.getIndent() - 1);
        environment.output.printf("private Action actionTable[] = {\n");
        break;
    }
    accion = 0;
    for (estado = 0; estado < runtimeData.getStates().length; estado++) {
      if (runtimeData.getStates()[estado].getPosition() >= accion) {
        for (Action pAct : runtimeData.getStates()[estado].getActions()) {
          switch (environment.getLanguage()) {
            case C:
              environment.output.printf("\t{%d, %d}", pAct.getSymbol().getToken(), pAct.getStateNumber());
              if (accion == runtimeData.getNumberOfActions() - 1) {
                environment.output.printf("\n};\n");
              } else {
                environment.output.printf(",\n");
              }
              break;
            case java:
              Tabea(environment.output, environment.getIndent());
              environment.output.printf("new Action(%d, %d)", pAct.getSymbol().getToken(), pAct.getStateNumber());
              if (accion == runtimeData.getNumberOfActions() - 1) {
                environment.output.printf("\n");
                Tabea(environment.output, environment.getIndent() - 1);
                environment.output.printf("};\n");
              } else {
                environment.output.printf(",\n");
              }
              break;
            case pascal:
              environment.output
                  .printf("    (symbol:%d; state:%d)", pAct.getSymbol().getToken(), pAct.getStateNumber());
              if (accion == runtimeData.getNumberOfActions() - 1) {
                environment.output.printf(");\n");
              } else {
                environment.output.printf(",\n");
              }
              break;
          }
          accion++;
        }
      }
    }

    // tabla de goto
    num_gotos = 0;
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
        Tabea(environment.output, environment.getIndent() - 1);
        environment.output.printf("// Goto table\n");
        Tabea(environment.output, environment.getIndent() - 1);
        environment.output.printf("private class Goto {\n");
        Tabea(environment.output, environment.getIndent());
        environment.output.printf("int origin;\n");
        Tabea(environment.output, environment.getIndent());
        environment.output.printf("int destination;\n\n");
        Tabea(environment.output, environment.getIndent());
        environment.output.printf("Goto(int origin, int destination) {\n");
        Tabea(environment.output, environment.getIndent() + 1);
        environment.output.printf("this.origin = origin;\n");
        Tabea(environment.output, environment.getIndent() + 1);
        environment.output.printf("this.destination = destination;\n");
        Tabea(environment.output, environment.getIndent());
        environment.output.printf("}\n");
        Tabea(environment.output, environment.getIndent() - 1);
        environment.output.printf("}\n\n");
        Tabea(environment.output, environment.getIndent() - 1);
        environment.output.printf("private static final int NUM_GOTOS=%d;\n", runtimeData.getNumberOfGoTos());
        Tabea(environment.output, environment.getIndent() - 1);
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
              if (num_gotos == runtimeData.getNumberOfGoTos() - 1) {
                environment.output.printf("\n};\n");
              } else {
                environment.output.printf(",\n");
              }
              break;
            case java:
              Tabea(environment.output, environment.getIndent());
              environment.output.printf("new Goto(%d, %d)", pGoto.getOrigin(), pGoto.getDestination());
              if (num_gotos == runtimeData.getNumberOfGoTos() - 1) {
                environment.output.printf("\n");
                Tabea(environment.output, environment.getIndent() - 1);
                environment.output.printf("};\n");
              } else {
                environment.output.printf(",\n");
              }
              break;
            case pascal:
              environment.output.printf("    (origin:%d; destination:%d)", pGoto.getOrigin(), pGoto.getDestination());
              if (num_gotos == runtimeData.getNumberOfGoTos() - 1) {
                environment.output.printf(");\n");
              } else {
                environment.output.printf(",\n");
              }
              break;
          }
          num_gotos++;
        }
      }
    }
    // tabla de parsing
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
        Tabea(environment.output, environment.getIndent() - 1);
        environment.output.printf("// Parsing table\n");
        Tabea(environment.output, environment.getIndent() - 1);
        environment.output.printf("private class Parser {\n");
        Tabea(environment.output, environment.getIndent());
        environment.output.printf("int position;\n");
        Tabea(environment.output, environment.getIndent());
        environment.output.printf("int defa;\n");
        Tabea(environment.output, environment.getIndent());
        environment.output.printf("int elements;\n");
        Tabea(environment.output, environment.getIndent());
        environment.output.printf("int msg;\n\n");
        Tabea(environment.output, environment.getIndent());
        environment.output.printf("Parser(int position, int defa, int elements, int msg) {\n");
        Tabea(environment.output, environment.getIndent() + 1);
        environment.output.printf("this.position = position;\n");
        Tabea(environment.output, environment.getIndent() + 1);
        environment.output.printf("this.defa = defa;\n");
        Tabea(environment.output, environment.getIndent() + 1);
        environment.output.printf("this.elements = elements;\n");
        Tabea(environment.output, environment.getIndent() + 1);
        environment.output.printf("this.msg = msg;\n");
        Tabea(environment.output, environment.getIndent());
        environment.output.printf("}\n");
        Tabea(environment.output, environment.getIndent() - 1);
        environment.output.printf("}\n\n");
        Tabea(environment.output, environment.getIndent() - 1);
        environment.output.printf("private Parser parsingTable[] = {\n");
        break;
      case pascal:
        environment.output.printf("\n" + "  StxParsingTable : array [TABLEROWS] of PARSER = (\n");
        break;
    }
    for (estado = 0; estado < runtimeData.getStates().length; estado++) {
      switch (environment.getLanguage()) {
        case C:
          environment.output.printf("\t{%d, %d, %d, %d}", runtimeData.getStates()[estado].getPosition(),
              runtimeData.getStates()[estado].getDefaultValue(), runtimeData.getStates()[estado].getHowMany(),
              runtimeData.getStates()[estado].getMessage());
          if (estado == runtimeData.getStates().length - 1) {
            environment.output.printf("\n};\n");
          } else {
            environment.output.printf(",\n");
          }
          break;
        case java:
          Tabea(environment.output, environment.getIndent());
          environment.output.printf("new Parser(%d, %d, %d, %d)", runtimeData.getStates()[estado].getPosition(),
              runtimeData.getStates()[estado].getDefaultValue(), runtimeData.getStates()[estado].getHowMany(),
              runtimeData.getStates()[estado].getMessage());
          if (estado == runtimeData.getStates().length - 1) {
            environment.output.printf("\n");
            Tabea(environment.output, environment.getIndent() - 1);
            environment.output.printf("};\n");
          } else {
            environment.output.printf(",\n");
          }
          break;
        case pascal:
          environment.output.printf("    (position:%d; defa:%d; elements:%d; msg:%d)",
              runtimeData.getStates()[estado].getPosition(), runtimeData.getStates()[estado].getDefaultValue(),
              runtimeData.getStates()[estado].getHowMany(), runtimeData.getStates()[estado].getMessage());
          if (estado == runtimeData.getStates().length - 1) {
            environment.output.printf(");\n");
          } else {
            environment.output.printf(",\n");
          }
          break;
      }
    }
    // tabla de mensaje de errores
    switch (environment.getLanguage()) {
      case C:
        environment.output.printf("\nchar * StxErrorTable[] = {\n");
        break;
      case java:
        environment.output.printf("\n");
        Tabea(environment.output, environment.getIndent() - 1);
        environment.output.printf("private String errorTable[] = {\n");
        break;
      case pascal:
        environment.output.printf("\n" + "  StxErrorTable : array [0..%d] of String = (\n", runtimeData
            .getErrorMessages().size() - 1);
        break;
    }
    for (err = 0; err < runtimeData.getErrorMessages().size(); err++) {
      switch (environment.getLanguage()) {
        case C:
          environment.output.printf("\t\"%s\"", runtimeData.getErrorMessages().get(err));
          if (err == runtimeData.getErrorMessages().size() - 1) {
            environment.output.printf("\n};\n");
          } else {
            environment.output.printf(",\n");
          }
          break;
        case java:
          Tabea(environment.output, environment.getIndent());
          environment.output.printf("\"%s\"", runtimeData.getErrorMessages().get(err));
          if (err == runtimeData.getErrorMessages().size() - 1) {
            environment.output.printf("\n");
            Tabea(environment.output, environment.getIndent() - 1);
            environment.output.printf("};\n");
          } else {
            environment.output.printf(",\n");
          }
          break;
        case pascal:
          environment.output.printf("    \'");
          String errorMessage = runtimeData.getErrorMessages().get(err);
          for (int i = 0; i < errorMessage.length(); i++) {
            char c = errorMessage.charAt(i);
            if (c == '\'') {
              environment.output.printf("\'");
            }
            environment.output.printf("%c", c);
          }
          environment.output.printf("\'");
          if (err == runtimeData.getErrorMessages().size() - 1) {
            environment.output.printf(");\n");
          } else {
            environment.output.printf(",\n");
          }
          break;
      }
    }
  }

  private void finishOutput() throws IOException {
    // char * buffer = NULL;
    // int readed;
    String filename;
    // int fh;
    // int tam;

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
      environment.output.printf("#line %d \"%s\"\n", runtimeData.nLine + 1, environment.getSourceFile().toString());
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
