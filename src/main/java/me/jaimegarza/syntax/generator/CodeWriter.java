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

import java.util.LinkedList;
import java.util.List;

import me.jaimegarza.syntax.definition.Action;
import me.jaimegarza.syntax.definition.GoTo;
import me.jaimegarza.syntax.definition.NonTerminal;
import me.jaimegarza.syntax.definition.Rule;
import me.jaimegarza.syntax.definition.Terminal;

public class CodeWriter extends AbstractPhase {

  private int nGotos;

  public void Header() {
    switch (environment.getLanguage()) {
      case pascal:
        if (environment.isPacked()) {
          environment.output.printf("\n" + "Type\n" + "  PACTION = ^ACTION;\n" + "  ACTION = RECORD\n"
              + "    symbol:SmallInt;\n" + "    state:SmallInt;\n" + "  end;\n" + "\n");
        }
        environment.output.printf("  PGOTOS = ^GOTOS;\n" + "  GOTOS = RECORD\n" + "    origin:SmallInt;\n"
            + "    destination:SmallInt;\n" + "  end;\n" + "\n");
        environment.output.printf("  PPARSER = ^PARSER;\n" + "  PARSER = RECORD\n" + "    position:SmallInt;\n"
            + "    defa:SmallInt;\n" + "    elements:SmallInt;\n" + "    msg:SmallInt;\n" + "  end;\n" + "\n");
        environment.output.printf("  PGRAMMAR = ^GRAMMAR;\n" + "  GRAMMAR = RECORD\n" + "    symbol:SmallInt;\n"
            + "    reductions:SmallInt;\n" + "  end;\n");
        // reserve a place for where the size of the table will be written
        // fPos = ftell(environment.output);
        environment.output.printf("\nConst\n  FINAL = 00000;\n" + "  SYMBS = %5d;\n", runtimeData.getTerminals().size()
            + runtimeData.getNonTerminals().size() - 2);
        environment.output.printf("Type\n" + "  TABLEROWS = 0..FINAL;\n" + "  TABLECOLS = 0..SYMBS;\n" + "\n");
        if (!environment.isPacked()) {
          environment.output.printf("{Parsing Table}\n"
              + "    StxParsingTable : array [TABLEROWS,TABLECOLS] of Integer = (\n");
        }
        break;
      case C:
        // reserve a place for where the size of the table will be written
        // fPos = ftell(environment.output);
        environment.output.printf("\n" + "#define FINAL 00000\n" + "#define SYMBS %5d\n\n", runtimeData.getTerminals()
            .size() + runtimeData.getNonTerminals().size() - 1);
        if (!environment.isPacked()) {
          environment.output.printf("/* Parsing Table */\n" + "int StxParsingTable[FINAL][SYMBS] = {\n");
        }
        break;
      case java:
        // reserve a place for where the size of the table will be written
        Tabea(environment.output, environment.getIndent() - 1);
        environment.output.printf("private static final int FINAL=");
        // fPos = ftell(environment.output);
        environment.output.printf("00000;\n");
        Tabea(environment.output, environment.getIndent() - 1);
        environment.output.printf("private static final int SYMBS=%5d;\n\n", runtimeData.getTerminals().size()
            + runtimeData.getNonTerminals().size() - 1);
        if (!environment.isPacked()) {
          Tabea(environment.output, environment.getIndent());
          environment.output.printf("// Parsing Table\n");
          Tabea(environment.output, environment.getIndent());
          environment.output.printf("private int parsingTable[][] = {\n");
        }
        break;
    }
  }

  public void Footer() {
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
              environment.output.printf("  %d);\n\n", id.getToken());
            } else {
              environment.output.printf("  %d,\n", id.getToken());
            }
            i++;
          }
        }
        // fseek(environment.output,fPos+19l,SEEK_SET);
        // environment.output.printf("%5d",runtimeData.getStates().length);
        break;
      case C:
        environment.output.printf("\n" + "/* symbols y reductions table */\n" + "typedef struct {\n"
            + "\tshort\tsymbol;\n" + "\tshort\treductions;\n" + "} GRAMMAR, *PGRAMMAR;\n" + "\n");
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
              environment.output.printf("\t%d\n};\n\n", id.getToken());
            } else {
              environment.output.printf("\t%d,\n", id.getToken());
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
            environment.output.printf("%d\n", id.getToken());
            Tabea(environment.output, environment.getIndent() - 1);
            environment.output.printf("};\n\n");
          } else {
            environment.output.printf("%d,", id.getToken());
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

  private int AccionDefault(
      int     Renglon[],
      int     nElems)
  {
      int     iDefa = 0, iDefaCount = 0;
      int     i, j, count;

      for(i = 0; i<nElems; i++) {
        if(Renglon[i] < 0 && Renglon[i] != iDefa){
            for(j = count = 0; j <nElems; j++) {
              if(Renglon[j] == Renglon[i]) {
                count++;
              }
            }
            if(count > iDefaCount){
                iDefaCount = count;
                iDefa = Renglon[i];
            }
        }
      }

      return iDefa;
  }

  private List<Action> CompactaAcciones(
      int     Renglon[],
      int     nElems,
      int     iDefa)
  {
      Action pAct;
      List<Action> acciones = new LinkedList<Action>();

      for(int i = 0; i < nElems; i++)
          if(Renglon[i] != 0 && Renglon[i] != iDefa){
              Terminal terminal = runtimeData.findTerminalById(i);
              pAct = new Action(terminal, Renglon[i]);
              acciones.add(pAct);
          }
      return acciones;
  }

  private boolean AccionIgual(List<Action> a, List<Action> b)
  {
    if (a == null && b == null) {
      return true;
    } else if (a == null || b == null) {
      return false;
    } else if (a.size() != b.size()) {
      return false;
    }
    for (int i = 0; i < a.size(); i++) {
      Action aa = a.get(i);
      Action ba = b.get(i);
      if (!aa.equals(ba)) {
        return false;
      }
    }
    return true;
  }

  private int BuscaAccion(
      int     estado,
      int     elems,
      List<Action> actions
      /*int   encontrado*/)
  {
      for(int i = 0; i < estado; i++)
          if(runtimeData.getStates()[i].getHowMany() == elems && AccionIgual(runtimeData.getStates()[i].getActions(), actions)){
              return i;
          }
      return -1;
  }

  private int AgregaGoto(NonTerminal id, int origen, int destino)
  {
    GoTo goTo = new GoTo(origen, destino);
    id.addGoTo(goTo);
      nGotos++;
      return 1;
  }

  private int QuitaGoto(NonTerminal id, int iDefa)
  {
    for (int i = 0; i < id.getGotos().size(); i++) {
      if (id.getGotos().get(i).getDestination() == iDefa) {
        id.getGotos().remove(i);
        nGotos--;
      }
    }
    return id.getGotos().size();
  }

  private int GotoDefault(
      NonTerminal  id)
  {
      int     iDefa = 0, iDefaCount = 0;
      int     count;

      for(GoTo pGoto : id.getGotos()) {
        if(pGoto.getDestination() != iDefa){
            count = 0;
            for(GoTo pAux : id.getGotos()) {
              if(pAux.getDestination() == pGoto.getDestination()) {
                count++;
              }
            }
            if(count > iDefaCount){
                iDefaCount = count;
                iDefa = pGoto.getDestination();
            }
        }
      }

      return iDefa;
  }

  void CompactaGotos()
  {
      int     iDefa;
      int     nElems, iPosicion;

      iPosicion = 0;
      for(NonTerminal id : runtimeData.getNonTerminals()) {
        iDefa = GotoDefault(id);
          if(iDefa != 0){
              nElems = QuitaGoto(id, iDefa);
              id.setToken(iPosicion);
              AgregaGoto(id, -1, iDefa);
              iPosicion += nElems+1;
          }
      }
  }

  void ImprimeTablas()
  {
      int estado, accion, num_gotos, err;
      //PACTION pAct;
      //PIDENT  id;
      //PGOTO   pGoto;
      String pmsg;

      CompactaGotos();
      // Tabla de action
      switch(environment.getLanguage()){
          case pascal:
              environment.output.printf(
                  "Const\n" +
                  "  NUM_GOTOS = %d;\n" +
                  "  ACTIONS = %d;\n\n" +
                  "  StxActionTable : Array [0..ACTIONS-1] of ACTION = (\n", nGotos, nAcciones);
              break;
          case C:
              environment.output.printf(
                      "\n" +
                      "/* Acction table */\n" +
                      "typedef struct {\n" +
                      "\tshort\tsymbol;\n" +
                      "\tshort\tstate;\n" +
                      "} ACTION, *PACTION;\n" +
                      "\n");
              environment.output.printf(
                  "#define ACTIONS %d\n" +
                  "\n" +
                  "ACTION StxActionTable[ACTIONS] = {\n", nAcciones);
              break;
          case java:
        Tabea(environment.output, nTabs-1);
              environment.output.printf( "// Acction table\n");
        Tabea(environment.output, nTabs-1);
              environment.output.printf( "private class Action {\n");
        Tabea(environment.output, nTabs);
              environment.output.printf( "private int symbol;\n");
        Tabea(environment.output, nTabs);
              environment.output.printf( "private int state;\n\n");
        Tabea(environment.output, nTabs);
              environment.output.printf( "Action(int symbol, int state) {\n");
        Tabea(environment.output, nTabs+1);
              environment.output.printf( "this.symbol = symbol;\n");
        Tabea(environment.output, nTabs+1);
              environment.output.printf( "this.state = state;\n");
        Tabea(environment.output, nTabs);
              environment.output.printf( "}\n");
        Tabea(environment.output, nTabs-1);
              environment.output.printf( "}\n\n");
        Tabea(environment.output, nTabs-1);
              environment.output.printf( "private static final int ACTIONS=%d;\n\n", nAcciones);
        Tabea(environment.output, nTabs-1);
              environment.output.printf( "private Action actionTable[] = {\n");
              break;
      }
      accion = 0;
      for(estado=0; estado <= nFinal; estado++)
          if(I[estado].posicion >= accion)
              for(pAct = I[estado].acciones; pAct; pAct=pAct->next){
                  switch(environment.getLanguage()){
                      case C:
                          environment.output.printf(
                              "\t{%d, %d}", pAct->simbolo->token, pAct->estado);
                          if(accion == nAcciones-1)
                              environment.output.printf( "\n};\n");
                          else
                              environment.output.printf( ",\n");
                          break;
                      case java:
              Tabea(environment.output, nTabs);
                          environment.output.printf(
                              "new Action(%d, %d)", pAct->simbolo->token, pAct->estado);
                          if(accion == nAcciones-1) {
                              environment.output.printf( "\n");
                  Tabea(environment.output, nTabs-1);
                environment.output.printf( "};\n");
              } else
                              environment.output.printf( ",\n");
                          break;
                      case pascal:
                          environment.output.printf(
                              "    (symbol:%d; state:%d)", pAct->simbolo->token,
                                  pAct->estado);
                          if(accion == nAcciones-1)
                              environment.output.printf( ");\n");
                          else
                              environment.output.printf( ",\n");
                          break;
                  }
                  accion++;
              }

      // tabla de goto
      num_gotos = 0;
      switch(environment.getLanguage()){
          case C:
              environment.output.printf(
                      "\n" +
                      "/* Goto table */\n" +
                      "typedef struct {\n" +
                      "\tshort\torigin;\n" +
                      "\tshort\tdestination;\n" +
                      "} GOTOS, *PGOTOS;\n" +
                      "\n");
              environment.output.printf(
                  "#define NUM_GOTOS %d\n" +
                  "\n" +
                  "GOTOS StxGotoTable[NUM_GOTOS] = {\n", nGotos);
              break;
          case java:
              environment.output.printf( "\n");
              Tabea(environment.output, nTabs-1);
              environment.output.printf( "// Goto table\n");
              Tabea(environment.output, nTabs-1);
              environment.output.printf( "private class Goto {\n");
              Tabea(environment.output, nTabs);
              environment.output.printf( "int origin;\n");
              Tabea(environment.output, nTabs);
              environment.output.printf( "int destination;\n\n");
              Tabea(environment.output, nTabs);
              environment.output.printf( "Goto(int origin, int destination) {\n");
              Tabea(environment.output, nTabs+1);
              environment.output.printf( "this.origin = origin;\n");
              Tabea(environment.output, nTabs+1);
              environment.output.printf( "this.destination = destination;\n");
              Tabea(environment.output, nTabs);
              environment.output.printf( "}\n");
              Tabea(environment.output, nTabs-1);
              environment.output.printf( "}\n\n");
              Tabea(environment.output, nTabs-1);
              environment.output.printf( "private static final int NUM_GOTOS=%d;\n", nGotos);
              Tabea(environment.output, nTabs-1);
              environment.output.printf( "private Goto gotoTable[] = {\n");
              break;
          case pascal:
              environment.output.printf(
                  "\n" +
                  "  StxGotoTable : array [0..NUM_GOTOS-1] of GOTOS = (\n");
              break;
      }
      for(NonTerminal id = runtimeData.getNonTerminals())
          if(id.getpGoto)
              for(pGoto = id->pGoto; pGoto; pGoto = pGoto->next){
                  switch(environment.getLanguage()){
                      case C:
                          environment.output.printf(
                              "\t{%d, %d}", pGoto->origen, pGoto->destino);
                          if(num_gotos == nGotos-1)
                              environment.output.printf( "\n};\n");
                          else
                              environment.output.printf( ",\n");
                          break;
                      case java:
              Tabea(environment.output, nTabs);
                          environment.output.printf(
                              "new Goto(%d, %d)", pGoto->origen, pGoto->destino);
                          if(num_gotos == nGotos-1) {
                environment.output.printf( "\n");
                Tabea(environment.output, nTabs-1);
                              environment.output.printf( "};\n");
                          } else
                              environment.output.printf( ",\n");
                          break;
                      case pascal:
                          environment.output.printf(
                              "    (origin:%d; destination:%d)", pGoto->origen,
                                  pGoto->destino);
                          if(num_gotos == nGotos-1)
                              environment.output.printf( ");\n");
                          else
                              environment.output.printf( ",\n");
                          break;
                  }
              num_gotos++;
              }
      // tabla de parsing
      switch(environment.getLanguage()){
          case C:
              environment.output.printf(
                      "\n" +
                      "/* Parsing table */\n" +
                      "typedef struct {\n" +
                      "\tshort\tposition;\n" +
                      "\tshort\tdefa;\n" +
                      "\tshort\telements;\n" +
                      "\tshort\tmsg;\n" +
                      "} PARSER, *PPARSER;\n" +
                      "\n");
              environment.output.printf(
                  "PARSER StxParsingTable[FINAL] = {\n");
              break;
          case java:
              environment.output.printf( "\n");
              Tabea(environment.output, nTabs-1);
              environment.output.printf( "// Parsing table\n");
              Tabea(environment.output, nTabs-1);
              environment.output.printf( "private class Parser {\n");
              Tabea(environment.output, nTabs);
              environment.output.printf( "int position;\n");
              Tabea(environment.output, nTabs);
              environment.output.printf( "int defa;\n");
              Tabea(environment.output, nTabs);
              environment.output.printf( "int elements;\n");
              Tabea(environment.output, nTabs);
              environment.output.printf( "int msg;\n\n");
              Tabea(environment.output, nTabs);
              environment.output.printf( "Parser(int position, int defa, int elements, int msg) {\n");
              Tabea(environment.output, nTabs+1);
              environment.output.printf( "this.position = position;\n");
              Tabea(environment.output, nTabs+1);
              environment.output.printf( "this.defa = defa;\n");
              Tabea(environment.output, nTabs+1);
              environment.output.printf( "this.elements = elements;\n");
              Tabea(environment.output, nTabs+1);
              environment.output.printf( "this.msg = msg;\n");
              Tabea(environment.output, nTabs);
              environment.output.printf( "}\n");
              Tabea(environment.output, nTabs-1);
              environment.output.printf( "}\n\n");
              Tabea(environment.output, nTabs-1);
              environment.output.printf( "private Parser parsingTable[] = {\n");
              break;
          case pascal:
              environment.output.printf(
                  "\n"
                  "  StxParsingTable : array [TABLEROWS] of PARSER = (\n");
              break;
      }
      for(estado=0; estado <= nFinal; estado++)
          switch(environment.getLanguage()){
              case C:
                  environment.output.printf(
                      "\t{%d, %d, %d, %d}", I[estado].posicion, I[estado].defa,
                          I[estado].cuantas, I[estado].mensaje);
                  if(estado == nFinal)
                          environment.output.printf( "\n};\n");
                      else
                          environment.output.printf( ",\n");
                  break;
              case java:
                  Tabea(environment.output, nTabs);
                  environment.output.printf( "new Parser(%d, %d, %d, %d)", I[estado].posicion, I[estado].defa, I[estado].cuantas, I[estado].mensaje);
                  if(estado == nFinal) {
                          environment.output.printf( "\n");
                          Tabea(environment.output, nTabs-1);
                          environment.output.printf( "};\n");
          } else
                          environment.output.printf( ",\n");
                  break;
              case pascal:
                  environment.output.printf(
                      "    (position:%d; defa:%d; elements:%d; msg:%d)",
                          I[estado].posicion,
                          I[estado].defa, I[estado].cuantas,
                          I[estado].mensaje);
                  if(estado == nFinal)
                      environment.output.printf( ");\n");
                  else
                      environment.output.printf( ",\n");
                  break;
          }
      // tabla de mensaje de errores
      switch(environment.getLanguage()){
          case C:
              fprintf(fileParErr,
                  "\nchar * StxErrorTable[] = {\n");
              break;
          case java:
        fprintf(fileParErr, "\n");
        Tabea(fileParErr, nTabs-1);
              fprintf(fileParErr, "private String errorTable[] = {\n");
              break;
          case pascal:
              fprintf(fileParErr,
                  "\n" +
                  "  StxErrorTable : array [0..%d] of String = (\n", nEMsgs-1);
              break;
      }
      for (err = 0; err < nEMsgs; err++)
          switch (environment.getLanguage()) {
              case C:
                  fprintf(fileParErr,
                      "\t\"%s\"", EMsgs[err]);
                  if(err == nEMsgs-1)
                      fprintf(fileParErr, "\n};\n");
                  else
                      fprintf(fileParErr, ",\n");
                  break;
              case java:
                  Tabea(fileParErr, nTabs);
                  fprintf(fileParErr, "\"%s\"", EMsgs[err]);
                  if(err == nEMsgs-1) {
                      fprintf(fileParErr, "\n");
                      Tabea(fileParErr, nTabs-1);
                      fprintf(fileParErr, "};\n");
          } else
                      fprintf(fileParErr, ",\n");
                  break;
              case pascal:
                  fprintf(fileParErr, "    \'");
                  for (pmsg = EMsgs[err]; *pmsg; pmsg++) {
                      if (*pmsg == '\'')
                          fprintf(fileParErr, "\'");
                      fprintf(fileParErr, "%c", *pmsg);
                  }
                  fprintf(fileParErr, "\'");
                  if(err == nEMsgs-1)
                      fprintf(fileParErr, ");\n");
                  else
                      fprintf(fileParErr, ",\n");
                  break;
          }
  }
}
