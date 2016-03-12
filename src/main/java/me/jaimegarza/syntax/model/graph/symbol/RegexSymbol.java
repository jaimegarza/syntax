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
package me.jaimegarza.syntax.model.graph.symbol;

/**
 * A regular expression symbol. Abstract.
 * @author jgarza
 *
 */
public abstract class RegexSymbol {
  
  protected final static int HASH_EPSILON = 1;
  protected final static int HASH_ANY = 2;
  
  protected final static int ANY_CODE = 0;
  protected final static int CHAR_CODE = 1;
  protected final static int CHARACTER_CLASS_CODE = 2;
  protected final static int EPSILON_CODE = 3;

  /**
   * Is the symbol an &epsilon; symbol?
   * @return true if it is so
   */
	public abstract boolean isEpsilon();

	/**
	 * A string representation of this symbol
	 * @return a simple representation of the symbol
	 */
	public abstract String canonical();
	
	/**
	 * Does a character matches a symbol?
	 * @param c the character
	 * @return true if it matches
	 */
	public abstract boolean matches(char c);
	
	/**
	 * When written to the output stream, each symbol has a separate code
	 * @return the code for this symbol
	 */
	public abstract int code();

	/**
	 * What is the size of this object in the edge table?
	 * @return the number of integer objects
	 */
  public abstract int sizeof();

  /**
   * @return the array of strings to be written to the edge table
   */
  public abstract int[] getCodeArray();

}
