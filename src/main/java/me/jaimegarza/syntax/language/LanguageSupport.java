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
package me.jaimegarza.syntax.language;

import java.io.IOException;
import java.io.PrintStream;

import me.jaimegarza.syntax.EmbeddedCodeProcessor;
import me.jaimegarza.syntax.Lexer;
import me.jaimegarza.syntax.definition.Action;
import me.jaimegarza.syntax.definition.ErrorToken;
import me.jaimegarza.syntax.definition.GoTo;
import me.jaimegarza.syntax.definition.Terminal;

/**
 * Define the methods that are provided for each language.  These
 * methods are business-kind methods, and as such they are expected to
 * have specific business-logic.
 *
 * @author jaimegarza@gmail.com
 *
 */
public interface LanguageSupport {
  /**
   * get the string that identifies the language.  Used for 
   * bundling and perhaps other stuff (i.e. titles, etc.)
   */
  String getLanguageCode();
  
  /**
   * get the short, one-letter, or two-letter identifier for this language.  Used in cmd line
   * options.
   */
  String getId();
  
  /**
   * get the source extension
   */
  String getExtensionSuffix();
  
  /**
   * Get include file extension
   */
  String getIncludeExtensionSuffix();
  
  /**
   * emit #line in C
   */
  void emitLine(int line);
  
  /**
   * emit #line in C
   */
  void emitLine(int line, String filename);
  
  /**
   * number of spaces per indent
   */
  int getNumberOfSpacesPerIndent();
  
  /**
   * Indents a number of separators, based on the number of spaces per indent.
   * @param out is the output stream.  it can be null
   * @param n is the number of indentations
   * 
   * @return the string with all the indents
   */
  String indent(PrintStream out, int n);
  
  /**
   * emit a case start
   * @param lineNumber the line number on the source
   * @param label the label of the case statement
   */
  void generateCaseStart(int lineNumber, String label); 
  /**
   * emit a case end statement
   */
  void generateCaseEnd();
  
  /**
   * Parsing logic
   * Scan the input stream until a rule set of actions end.
   * The format for ending rules is language dependent
   * 
   * @param lexer the element that will give me the lexical logic
   * @param elementCount the number of elements in the rule
   * @param nonTerminalId the non terminal id for the rule
   */
  boolean generateRuleCode(Lexer lexer, EmbeddedCodeProcessor processor, int elementCount, String nonTerminalId) throws IOException;

  /**
   * Emit the header of the lexer as needed.
   */
  void generateLexerHeader();

  /**
   * Generate the bottom of the lexer
   */
  void generateLexerFooter();
  
  /**
   * Output the top of the rules if needed
   */
  void generateCodeGeneratorHeader();
  
  /**
   * Generate the ending portion of the code generation
   */
  void generateCodeGeneratorFooter();

  /**
   * The recovery table deals with tokens that can be used to recognize
   * syntax context and can recover from errors.
   */
  void generateRecoveryTableHeader(int numberOfErrorTokens);
  
  /**
   * For yacc compatibility this is called the union, but it is
   * really a structure
   */
  boolean generateStructure(Lexer lexer) throws IOException;

  /**
   * Generates one entry for an error token
   * @param recoveryToken the recovery id
   * @param id the {@link ErrorToken}
   * @param isLast to check if this is the last
   */
  void generateErrorToken(int recoveryToken, ErrorToken id, boolean isLast);

  /**
   * Generates the top part of the token definition
   * @param terminals
   */
  void generateTokensHeader(int terminals);

  /**
   * Generate the code for a given token
   * @param id is the token itself
   * @param isLast to determine the way to emit the last token
   */
  void generateToken(Terminal id, boolean isLast);

  /**
   * Declare all the tokens
   */
  void generateTokenDefinitions();

  /**
   * Emit the code for the lexical part of the grammar
   * @param lexer is the lexer to obtain more characters
   * @throws IOException 
   */
  boolean generateLexerCode(Lexer lexer) throws IOException;

  /**
   * print the declarations for the rest of the code
   */
  void printCodeHeader();

  /**
   * print one row of the parsing table
   * @param symbolCounter
   * @param parserLine 
   * @param stateNumber 
   */
  void printTableRow(int symbolCounter, int[] parserLine, int stateNumber);

  /**
   * Declare the parsing table
   */
  void printParsingTableHeader();

  /**
   * print a state in packed fashion
   * @param stateNumber
   */
  void printPackedState(int stateNumber);

  /**
   * declare the error table
   */
  void printErrorTableHeader();

  /**
   * Add errors to the table
   * @param error is the error number
   */
  void printErrorEntry(int error);

  void printConstants();

  void printActionHeader();

  void printAction(int action, Action anAction);

  void printGoToTableHeader();

  void printGoTo(int numGotos, GoTo pGoto);

  void printMissingSkeleton(String filename);

  void printGrammarTable();
}
