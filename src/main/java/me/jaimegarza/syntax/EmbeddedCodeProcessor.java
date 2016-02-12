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
package me.jaimegarza.syntax;

import me.jaimegarza.syntax.model.parser.Type;

public interface EmbeddedCodeProcessor {
  /**
   * Retrieve the type embedded as in $&lt;<i>type</i>&gt;1
   * @param lexer the element that will give me the lexical logic
   * @return the type from the scanner
   */
  Type getTypeFromStream(Lexer lexer);

  /**
   * Scan the stream unti a string that starts with the given character ends
   * @param lexer the object in charge of managing the input stream
   * @param characterType, is the starting character
   * @return true on success
   */
  public boolean generateConstant(Lexer lexer, char characterType);
  
  /**
   * $1, $2, $-3, etc detected.  Proceed with the code generation
   * 
   * @param lexer the element that will give me the lexical logic
   * @param elementCount the number of elements in the rule
   * @param nonTerminalId the non terminal id for the rule
   * @param type the type of the element
   * @return true if everything is OK
   */
  boolean generateDollarNumber(Lexer lexer, int elementCount, Type type, int sign);
  
  /**
   * $$ detected.  Proceed with the code generation
   * 
   * @param lexer the element that will give me the lexical logic
   * @param elementCount the number of elements in the rule
   * @param nonTerminalId the non terminal id for the rule
   * @param type the type of the element
   * @return true if everything is OK
   * */
  boolean generateDollarDollar(Lexer lexer, int elementCount, String nonTerminalId, Type type);
  
  /**
   * $Letter was detected.  The idea is to transform such occurrences into $$ or $digit
   * occurrences by looking at the symbol whose name is given by the identifier.<p>
   * 
   * When two rule items have the same symbol, a disambiguating index like $Symbol[1],
   * $Symbol[2], etc. can be used.  The Left hand symbol is only used when non indexed;
   * 
   * @param lexer the element that will give me the lexical logic
   * @param elementCount the number of elements in the rule
   * @param nonTerminalId the non terminal id for the rule
   * @param type the type of the element
   * @return true if everything is OK
   */
  boolean generateDollarLetter(Lexer lexer, int elementCount, Type type, String nonTerminalId);
  
  /**
   * Skip a comment, and copying it to the output
   * 
   * @param lexer the element that will give me the lexical logic
   * @param primaryCharacter, for example /
   * @param secondaryCharacter, for example *
   * @return true on success
   */
  boolean skipAndOutputCompositeComment(Lexer lexer, char secondaryCharacter, char primaryCharacter);

}
