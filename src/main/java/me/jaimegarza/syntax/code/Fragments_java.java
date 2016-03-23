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
package me.jaimegarza.syntax.code;

import java.util.ListResourceBundle;

/**
 * Fragments of code to be used for code generation
 * when the language is java
 *
 * Please be advised that {1} is interpreted to be the
 * indentation string required.  Always.  This is a
 * contract.
 *
 * @author jaimegarza@gmail.com
 *
 */
public class Fragments_java extends ListResourceBundle {

  @Override
  protected Object[][] getContents() {
    return contents;
  }
  
  /**
   * The fragments
   */
  Object[] contents [] = {
      {Fragments.HELLO,"Jello Jorld"}, // keep, for unit testing
      {Fragments.STXSTACK, "stack[stackTop{0}]"},
      {Fragments.CURRENT_CHAR, "currentChar"},
      {Fragments.LEXICAL_VALUE, "lexicalValue"},
      {Fragments.GETC, "currentChar = getNextChar(false)"},
      {Fragments.RETURN_VALUE, "return {1}"},
      {Fragments.LEXER_MODE, "parserElementMode"},
      {Fragments.TOKEN, "{0}"},
      {Fragments.RECOGNIZED, "recognized"},
      {Fragments.LEXER_FUNCTION_NAME, "parserElement_{0}"},
      {Fragments.RETURN, "return {0}"}
  };

}
