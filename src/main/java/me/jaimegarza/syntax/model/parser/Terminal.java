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
package me.jaimegarza.syntax.model.parser;

/**
 * <i>~pojo class</i><br><br>
 * 
 * Represents a terminal (lexical element) in the grammar (i.e. number, identifier, 
 * '+').  Symbols have the 
 * following hierarchy:
 * <pre>
 *  -+ {@link Symbol}
 *   |
 *   +--+ Terminal     - Lexical Symbol (i.e. number, id '+')
 *   |  |
 *   |  +-- {@link ErrorToken} - Lexical Symbol declared with <b>%error</b>
 *   |
 *   +-- <b>{@link NonTerminal}</b>   - Syntactical symbol (i.e. Expression, Statement)
 *   </pre>
 * 
 * @author jaimegarza@gmail.com
 *
 */
public class Terminal extends Symbol {

  /**
   * Construct one non terminal
   * @param name is the name of the non terminal
   */
  public Terminal(String name) {
    super(name);
  }

  /**
   * Convert a non-terminal to a terminal (as part of the grammar analysis,
   * when an element has not been declared, a warning will be raised and then
   * declared as a terminal.
   * @param nonTerminal is the existing non terminal.
   */
  public Terminal(NonTerminal nonTerminal) {
    super(nonTerminal.name);
    this.count = nonTerminal.count;
    this.associativity = nonTerminal.associativity;
    this.fullName = nonTerminal.fullName;
    this.id = nonTerminal.id;
    this.precedence = nonTerminal.precedence;
    this.token = nonTerminal.token;
    this.type = nonTerminal.type;
    this.variable = nonTerminal.variable;
  }

}
