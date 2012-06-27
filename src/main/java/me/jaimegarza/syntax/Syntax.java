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
 ===============================================================================
*/
package me.jaimegarza.syntax;

import me.jaimegarza.syntax.cli.Environment;
import me.jaimegarza.syntax.generator.CodeParser;
import me.jaimegarza.syntax.generator.CodeWriter;
import me.jaimegarza.syntax.generator.RuntimeData;
import me.jaimegarza.syntax.generator.StructuralAnalyzer;
import me.jaimegarza.syntax.generator.TableGenerator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Main entry point to syntax
 *
 * Syntax is a compiler compiler written as a tool for my students in college for 
 * the compiler construction course.
 * 
 * I thought that having a yacc like syntax would help the introduction of 
 * LR parsers.  Over time I have added:
 * 
 * - Code to be generated into a lexical analizer (non-regex)
 * - Error messages
 * - Output in java
 * - Output in C
 * - Output for Delphi Pascal
 * - Translated to JAVA from its 1985 C codebase.
 * 
 * - Compile with LALR (yacc) or SLR, more compact, a little more restrictive
 * - Eject the output table in a compressed mode (yacc) or a matrix, for readability.
 * - Unlike yacc, the output is properly formated and readable.
 * 
 * @author jaimegarza@gmail.com
 *
 */
public class Syntax {

  private Environment environment;
  private RuntimeData runtimeData = new RuntimeData();

  public final Log LOG = LogFactory.getLog(this.getClass());

  /**
   * Initialize syntax with the environment.
   * 
   * @param environment
   */
  public Syntax(Environment environment) {
    this.environment = environment;
  }

  /**
   * Execute the phases as follows:
   * 
   * 1. Parse the file and create the rule structure
   * 2. Analyze and compute first and (for SLR) follow symbols
   * 3. Generate the states pertinent to the rules
   * 4. Output the components of the resulting code.
   */
  private void execute() {
    CodeParser parser = new CodeParser(environment, runtimeData);
    StructuralAnalyzer analyzer = new StructuralAnalyzer(environment, runtimeData);
    TableGenerator generator = new TableGenerator(environment, runtimeData);
    CodeWriter writer = new CodeWriter(environment, runtimeData);
    try {
      parser.execute();
      analyzer.execute();
      generator.execute();
      writer.execute();
    } catch (AnalysisException e) {
      LOG.error("Internal error: " + e.getMessage(), e);
    } catch (ParsingException e) {
      LOG.error("Parsing error: " + e.getMessage(), e);
    } catch (OutputException e) {
      LOG.error("Output error: " + e.getMessage(), e);
    }
  }

  /**
   * Entry point 
   * @param args command line arguments
   */
  public static void main(String args[]) {
    Environment environment = new Environment("Syntax", args);
    if (environment.isDebug()) {
      System.out.println("environment\n" + environment);
    }

    try {
      if (environment.isVerbose()) {
        System.out.println("Syntax");
      }
      Syntax syntaxTool = new Syntax(environment);
      syntaxTool.execute();
    } finally {
      if (environment.isVerbose()) {
        System.out.println("Done\n");
      }
      environment.release();
    }
  }

}
