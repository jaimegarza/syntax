/*"su
Syntax is distributed under the Revised, or 3-clause, BSD license
===================================================================
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
===================================================================
 */
package me.jaimegarza.syntax.generator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import me.jaimegarza.syntax.env.Environment;
import me.jaimegarza.syntax.exception.OutputException;
import me.jaimegarza.syntax.model.graph.Dfa;
import me.jaimegarza.syntax.model.graph.DfaNode;
import me.jaimegarza.syntax.model.graph.Transition;
import me.jaimegarza.syntax.model.graph.symbol.CharacterClass;
import me.jaimegarza.syntax.model.parser.Action;
import me.jaimegarza.syntax.model.parser.GoTo;
import me.jaimegarza.syntax.model.parser.NonTerminal;
import me.jaimegarza.syntax.model.parser.State;

/**
 * Phases:
 * 
 * <ol>
 *   <li>Code Parser
 *   <li>Structural Analysis
 *   <li>Table Generation
 *   <li><b>Writing Code</b> (This Phase)
 * </ol>
 *
 * @author jaimegarza@gmail.com
 *
 */
public class CodeWriter extends AbstractPhase {

  private static final char RESOURCE_SEPARATOR = '/';

  /**
   * Construct a code writer out of the shared environment
   * @param environment is the global environment
   */
  public CodeWriter(Environment environment) {
    super(environment);
  }

  /**
   * Open code with the declarations
   */
  private void printHeader() {
    environment.language.printCodeHeader();
  }

  /**
   * prints the code for an unpacked table row 
   * @param state is the state to print
   */
  private void printTableRow(State state) {
    int parserLine[] = state.getRow();
    int stateNumber = state.getId();

    if (environment.isPacked() == true) {
      return;
    }
    int symbolCounter = runtimeData.getTerminals().size() + runtimeData.getNonTerminals().size() - 2;
    environment.language.printTableRow(symbolCounter, parserLine, stateNumber);
  }

  /**
   * print multiple tables
   */
  private void printTables() {
    int stateNumber, action, error;

    if (environment.isPacked() == true) {
      environment.language.printActionHeader();
      action = 0;
      for (stateNumber = 0; stateNumber < runtimeData.getStates().length; stateNumber++) {
        if (runtimeData.getStates()[stateNumber].getPosition() >= action) {
          for (Action anAction : runtimeData.getStates()[stateNumber].getActions()) {
            environment.language.printAction(action, anAction);
            action++;
          }
        }
      }

      environment.language.printGoToTableHeader();
      int gotoIndex = 0;
      for (NonTerminal id : runtimeData.getNonTerminals()) {
        if (id.getGotos() != null && id.getGotos().size() > 0) {
          for (GoTo pGoto : id.getGotos()) {
            environment.language.printGoTo(gotoIndex++, pGoto);
          }
        }
      }
      
      environment.language.printParsingTableHeader();
      for (stateNumber = 0; stateNumber < runtimeData.getStates().length; stateNumber++) {
        environment.language.printPackedState(stateNumber);
      }
    }
    if (environment.isPacked() == false) {
      environment.language.printParserErrors();
    }
    environment.language.printErrorTableHeader();
    for (error = 0; error < runtimeData.getErrorMessages().size(); error++) {
      environment.language.printErrorEntry(error);
    }
    environment.language.printErrorFooter();
  }

  /**
   * Close the printing of code
   */
  private void printFooter() {
    printTables();
    environment.language.printGrammarTable();
  }
  
  /**
   * Given the defined arguments (and defaults) compute the skeleton name
   * @return the skeleton in the resources
   */
  private String getSkeletonResourceName() {
    StringBuilder builder = new StringBuilder();
    String parserStructure = environment.isPacked() ? "packed" : "tabular";
    builder.append(environment.getDriver().skeleton())
           .append(RESOURCE_SEPARATOR)
           .append(parserStructure)
           .append(RESOURCE_SEPARATOR)
           .append(parserStructure).append(environment.language.getExtensionSuffix());
    
    return builder.toString();
  }

  /**
   * Close the output by putting the remaining of the grammar file and the skeleton parser
   * @throws IOException
   */
  private void finishOutput() throws IOException {
    String filename;
    BufferedReader reader = null;
    boolean close = false;
    
    try {
      if (environment.skeleton != null) {
        reader = environment.skeleton;
        filename = environment.getSkeletonFile().getAbsolutePath();
        close = false;
      } else {
        filename = getSkeletonResourceName();
        ClassLoader loader = this.getClass().getClassLoader();
        InputStream is = loader.getResourceAsStream(filename);
        if (is != null) {
          reader = new BufferedReader(new InputStreamReader(is));
          close = true;
        } else {
          System.err.println("\n\nWarning: internal skeleton \"" + filename + "\" not found.  Table was generated.\n");
          environment.language.printMissingSkeleton(filename);
        }
      }
  
      if (reader != null) {
        if (environment.isVerbose()) {
          System.out.println("using skeleton " + filename);
        }
  
        environment.language.emitLine(1, filename);
        String line = reader.readLine();
        while (line != null) {
          environment.output.println(line);
          line = reader.readLine();
        }
      }
    } finally {
      if (close) {
        reader.close();
      }
    }
    
    if (runtimeData.hasFinalActions() == false) {
      return;
    }

    environment.output.println();
    environment.language.emitLine(runtimeData.lineNumber + 1);

    int c = environment.source.read();
    while (c != -1) {
      environment.output.print((char) c);
      c = environment.source.read();
    }
  }

  private void reportSummary() {
    environment.reportWriter.subHeading("Summary");
    environment.reportWriter.tableHead("summary", left("Property"), left("Value"));
    environment.reportWriter.tableRow(left("Source"), left(environment.getSourceFile()));
    environment.reportWriter.tableRow(left("Output"), left(environment.getOutputFile()));
    environment.reportWriter.tableRow(left("Include/Interface"), left("" + environment.getIncludeFile()));
    environment.reportWriter.tableRow(left("Algorithm"), left(environment.getAlgorithmType()));
    environment.reportWriter.tableRow(left("Language"), left(environment.getLanguageEnum()));
    environment.reportWriter.tableRow(left("Packed?"), left(environment.isPacked()));
    environment.reportWriter.tableRow(left("Tokens"), left(runtimeData.getTerminals().size()));
    environment.reportWriter.tableRow(left("Non Terminals"), left(runtimeData.getNonTerminals().size()));
    environment.reportWriter.tableRow(left("Types"), left(runtimeData.getTypes().size()));
    environment.reportWriter.tableRow(left("Rules"), left(runtimeData.getRules().size()));
    environment.reportWriter.tableRow(left("Errors"), left(runtimeData.getErrorMessages().size()));
    environment.reportWriter.tableRow(left("Actions"), left(runtimeData.getNumberOfActions()));
    environment.reportWriter.tableRow(left("Gotos"), left(runtimeData.getNumberOfGoTos()));
    environment.reportWriter.tableRow(left("Recoveries"), left(runtimeData.getNumberOfRecoveries()));
    environment.reportWriter.tableRow(left("States"), left(runtimeData.getStates().length));
    environment.reportWriter.tableEnd();
  }
  
  private void printEdges() {
    int tableSize = 0;
    int numberOfNodes = 0;
    for (Dfa dfa: runtimeData.getRegularExpressions()) {
      for (DfaNode node: dfa.getNodes()) {
        numberOfNodes++;
        tableSize += node.sizeof();
      }
    }

    environment.language.generateEdgeHeader(tableSize);
    int edgeIndex = 0;
    for (Dfa dfa: runtimeData.getRegularExpressions()) {
      environment.language.generateIntArrayComment("" + edgeIndex + " - start regexp : " +  dfa.toString());
      for (DfaNode node: dfa.getNodes()) {
        List<Transition> codeTransitions = node.getCodeTransitions();
        node.setEdgeIndex(edgeIndex);
        environment.language.generateIntArrayRow(codeTransitions.size(), "" + codeTransitions.size() + " transition" + (codeTransitions.size() == 1 ? "" : "s"), edgeIndex++, tableSize);
        for (Transition transition: codeTransitions) {
          int sign = 1;
          int size = transition.code();
          if (transition.getSymbol() instanceof CharacterClass && 
            ((CharacterClass) transition.getSymbol()).isNegate()) {
            sign = -1;
          }
          environment.language.generateIntArrayRow(transition.getTo().getId(), " <-- transition to this new vertex if match", edgeIndex++, tableSize);
          environment.language.generateIntArrayRow(sign * size, transition.toString(), edgeIndex++, tableSize);
          int [] code = transition.getCodeArray();
          for (int i = 0; i < code.length; i++) {
            environment.language.generateIntArrayRow(code[i], "" + (char)code[i], edgeIndex++, tableSize);
          }
        }
      }
    }
    environment.language.generateIntArrayFooter();

    environment.language.generateVertexHeader(numberOfNodes);
    int vertexIndex = 0;
    for (Dfa dfa: runtimeData.getRegularExpressions()) {
      for (DfaNode node: dfa.getNodes()) {
        int index = node.getEdgeIndex();
        if (node.isAccept()) {
          index = -index;
        }
        environment.language.generateIntArrayRow(index, node.toString(), vertexIndex++, numberOfNodes);
      }
    }
    environment.language.generateIntArrayFooter();
  }
  
  /**
   * Execute all the elements of this phase
   * @throws OutputException on error
   */
  public void execute() throws OutputException {
    try {
      // Regular Expressions
      printEdges();
      
      printHeader();
      if (environment.isPacked() == false) {
        for (int i = 0; i < runtimeData.getStates().length; i++) {
          printTableRow(runtimeData.getStates()[i]);
        }
      }
      printFooter();
      finishOutput();
      reportSummary();
    } catch (IOException e) {
      throw new OutputException("Error while creating output files", e);
    }
  }
}
