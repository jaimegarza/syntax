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

import me.jaimegarza.syntax.EmbeddedCodeProcessor;
import me.jaimegarza.syntax.Lexer;
import me.jaimegarza.syntax.model.parser.Action;
import me.jaimegarza.syntax.model.parser.ErrorToken;
import me.jaimegarza.syntax.model.parser.GoTo;
import me.jaimegarza.syntax.model.parser.Terminal;
import me.jaimegarza.syntax.util.FormattingPrintStream;

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
   * @return the language code as a string
   */
  String getLanguageCode();
  
  /**
   * get the short, one-letter, or two-letter identifier for this language.  Used in cmd line
   * options.
   * @return the id of the language
   */
  String getId();
  
  /**
   * get the source extension
   * @return the extension suffix for the language
   */
  String getExtensionSuffix();
  
  /**
   * @return include file extension
   */
  String getIncludeExtensionSuffix();
  
  /**
   * tells me the default value for include file generation
   * @return the default setting
   */
  boolean getDefaultIncludeFlag();
  
  /**
   * emit #line in C
   * @param line is the line number
   */
  void emitLine(int line);
  
  /**
   * emit #line in C
   * @param line is the line number
   * @param filename is the filename
   */
  void emitLine(int line, String filename);
  
  /**
   * number of spaces per indent
   * @return the number of spaces per indent
   */
  int getNumberOfSpacesPerIndent();
  
  /**
   * Indents a number of separators, based on the number of spaces per indent.
   * @param out is the output stream.  it can be null
   * @param n is the number of indentations
   * 
   * @return the string with all the indents
   */
  String indent(FormattingPrintStream out, int n);
  
  /**
   * Indents a number of separators, returning the string
   * @param n is the number of indentations
   * 
   * @return the string with all the indents
   */
  String indent(int n);
  
  /**
   * emit a case start
   * @param lineNumber the line number on the source
   * @param label the label of the case statement
   * @param comment is the comment of the case
   * @return for indentation purposes the column where the case ended
   */
  int generateCaseStart(int lineNumber, String label, String comment);
  
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
   * @param processor is the processor for rules
   * @param elementCount the number of elements in the rule
   * @param nonTerminalId the non terminal id for the rule
   * @param sourceColumn is the column number
   * @return true if OK
   */
  boolean generateRuleCode(Lexer lexer, EmbeddedCodeProcessor processor, int elementCount, String nonTerminalId, int sourceColumn);

  /**
   * Emit the header of the lexer as needed.
   * @param modes is the list of forward modes
   */
  void generateLexerHeader(List<String> modes);
  
  /**
   * Emit a lexer function header by mode
   * @param mode for the lexer mode
   */
  void generateLexerModeHeader(String mode);

  /**
   * Emit a lexer function footer by mode
   * @param mode for the lexer mode
   */
  void generateLexerModeFooter(String mode);

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
   * there was no code generated.  Generate a good-for-nothing code
   * generator routine.
   */
  void generateVoidCodeGenerator();
  
  /**
   * The recovery table deals with tokens that can be used to recognize
   * syntax context and can recover from errors.
   * @param numberOfErrorTokens is the number of errors
   */
  void generateRecoveryTableHeader(int numberOfErrorTokens);
  
  /**
   * For yacc compatibility this is called the union, but it is
   * really a structure
   * @param lexer is the lexical analyzer
   * @return true if OK
   */
  boolean generateStructure(Lexer lexer);

  /**
   * Generates one entry for an error token
   * @param recoveryToken the recovery id
   * @param id the {@link ErrorToken}
   * @param isLast to check if this is the last
   */
  void generateErrorToken(int recoveryToken, ErrorToken id, boolean isLast);

  /**
   * Generates the top part of the token definition
   * @param terminals is the number of terminal symbols
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
   * @param output is the stream to write to
   * @param lexer is the lexer to obtain more characters
   * @param token is the current token for which generation is attempted
   * @param additionalIndent is the wanted indentation delta
   * @return true if OK
   */
  boolean generateLexerCode(FormattingPrintStream output, String lexerMode, Lexer lexer, Terminal token, int additionalIndent);

  /**
   * print the declarations for the rest of the code
   */
  void printCodeHeader();

  /**
   * print one row of the parsing table
   * @param symbolCounter is the number of symbols
   * @param parserLine is the array of lines
   * @param stateNumber is the state number
   */
  void printTableRow(int symbolCounter, int[] parserLine, int stateNumber);

  /**
   * Declare the parsing table
   */
  void printParsingTableHeader();

  /**
   * print a state in packed fashion
   * @param stateNumber is the id of the state
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

  /**
   * Close the error table
   */
  void printErrorFooter();

  /**
   * emit the header of the action table
   */
  void printActionHeader();
  
  /**
   * Emit one action
   * @param action the action id
   * @param anAction the actual action
   */
  void printAction(int action, Action anAction);

  /**
   * emit the goto table header
   */
  void printGoToTableHeader();

  /**
   * Emit one go to
   * @param gotoIndex the number of gotos
   * @param pGoto the actual goto
   */
  void printGoTo(int gotoIndex, GoTo pGoto);

  /**
   * The skeleton was not found,  say so.
   * @param filename the missing skeleton
   */
  void printMissingSkeleton(String filename);

  /**
   * Output the whole grammar table
   */
  void printGrammarTable();

  /**
   * Display the errors of the parser
   */
  void printParserErrors();

  /**
   * Generate the constant or 'define" for a given lexer mode
   * @param mode is the mode of the lexer
   * @param i is the index of the definition
   */
  void generateLexerModeDefinition(String mode, int i);

  /**
   * Generate the constant or 'define" for a given lexer mode
   * @param mode is the mode of the lexer
   * @param i is the index of the definition
   */
  void generateLexerModeCase(String mode, int i);

  /**
   * Generate the code for the edge table beginning
   * @param tableSize is the size of the table
   */
  void generateEdgeHeader(int tableSize);

  /**
   * Generate the vertex header
   * @param size is the number of nodes
   */

  void generateVertexHeader(int size);
  /**
   * Output a row in an integer array
   * @param i is the integer to output
   * @param comment, a comment to add, if any
   * @param index is the row of this item
   * @param maxRows is the size of the array. Used to determine commas, etc.
   */
  void generateIntArrayRow(int i, String comment, int index, int maxRows);
  
  /**
   * Output a comment in an array
   * @param comment is the comment to output
   */
  void generateIntArrayComment(String comment);

  /** 
   * Output the end of an int array
   */
  public void generateIntArrayFooter();

  /**
   * Generate if statement for a regular expression match
   * @param dfaNode is the starting dfa node to begin the match
   */
  void generateRegexMatch(FormattingPrintStream output, int dfaNode);

  /**
   * Generate the default return token for a regexp
   * @param token is the token that it returns
   */
  void generateRegexReturn(FormattingPrintStream output, Terminal token);

  /**
   * Generate the bottom of a regex match in the scanner
   * @param output 
   */
  void generateRegexEnd(FormattingPrintStream output);

}
