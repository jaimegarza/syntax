/*
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
 * Represents an error non terminal in the grammar . Error tokens are defined 
 * with <b>%error</b>.  They are used by the grammar to generate an error
 * recovery table.  When an error is found during the resulting code's parsing phase,
 * the recover table is used to try to recover the engine to a valid state where
 * compilation could continue so that the parsing does not stop at the first
 * error.<p>
 * 
 * The error recovery system is based on an algorithm that drops stack states until one
 * with a proper error token transition is found.<p>
 * 
 * Other than that, error tokens are just another non-terminal symbol.<p>
 * 
 * Symbols have the following hierarchy:
 * <pre>
 *  -+ {@link Symbol}
 *   |
 *   +--+ {@link Terminal}     - Lexical Symbol (i.e. number, id '+')
 *   |  |
 *   |  +-- ErrorToken - Lexical Symbol declared with <b>%error</b>
 *   |
 *   +-- <b>{@link NonTerminal}</b>   - Syntactical symbol (i.e. Expression, Statement)
 *   </pre>
 *   
 *
 * @author jaimegarza@gmail.com
 *
 */
public class ErrorToken extends Terminal {

  public ErrorToken(String name) {
    super(name);
  }

}
