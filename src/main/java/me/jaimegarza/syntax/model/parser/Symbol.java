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
 * Represents a symbol in the grammar.  Symbols have the following hierarchy:
 * <pre>
 *  -+ <b>Symbol</b>
 *   |
 *   +--+ {@link Terminal}     - Lexical Symbol (i.e. number, id '+')
 *   |  |
 *   |  +-- {@link ErrorToken} - Lexical Symbol declared with <b>%error</b>
 *   |
 *   +--{@link NonTerminal}    - Syntactical symbol (i.e. Expression, Statement)
 *   </pre>
 *   
 * This class is abstract.<p>
 *
 * @author jaimegarza@gmail.com
 *
 */
public abstract class Symbol {
  /**
   * The numeric identifier for the symbol.  Sequence.
   */
  protected int id;
  /**
   * the name of the symbol.
   */
  protected String name;
  /**
   * the full name.  by default it equals the name, but a full name
   * can be declared in the grammar spec.  Useful for error generation.
   */
  protected String fullName;
  /**
   * The numeric precedence of the symbol.  Precedence is used, when provided
   * to resolve ambiguities when giving LR grammars with conflicts.<p>
   * precedence is usually provided with <b>%prec</b>.
   * 
   * Usually the associativity with higher precedence is picked for a {@link Rule}<br>
   * For conflict resolution:<pre>
   *   if shift-reduce conflict
   *      left associativity implies reduce
   *      right associativity implies shift
   *      non assoc implies error</pre>
   */
  protected int precedence;
  /**
   * The associativity of a given symbol.  Associativity is used, when
   * provided, to resolve ambiguities when providing LR grammars with conflicts.
   */
  protected Associativity associativity;
  /**
   * Reference count of the symbol
   */
  protected int count;
  /**
   * The lexical value returned from the lexical analyzer. Can be provided
   * in <b>%token</b>
   */
  protected int token;
  /**
   * The type of the symbol.  A type is a string that is used on the generation
   * phase to resolve $$, $1, $2, etc.  They get properly replaced with stack
   * references like:<p>
   * 
   * stack[stackTop-1].<i><b>type</b></i>
   */
  protected Type type;
  
  /**
   * A language-named representation of a terminal.  This code is computed
   */
  protected String variable;

  public Symbol(String name) {
    super();
    this.name = name;
    this.fullName = name;
    this.count = 0;
    this.token = -1;
    this.precedence = 0;
    this.associativity = Associativity.NONE;
  }

  /**
   * Compute a language usable variable name
   * @return
   */
  public void computeVariable() {
    if (name == null || name.length() == 0) {
      variable = "TOKEN_" + token;
      return;
    }

    if (token == 0) {
      variable = "EOS";
      return;
    }

    if (isIdentifier()) {
      variable = name;
      return;
    }

    if (name.charAt(0) == '\\') {
      if (!name.equals("\\a")) {
        variable = "BELL";
      }
      if (!name.equals("\\b")) {
        variable = "BACKSPACE";
      }
      if (!name.equals("\\n")) {
        variable = "EOL";
      }
      if (!name.equals("\\t")) {
        variable = "TAB";
      }
      if (!name.equals("\\f")) {
        variable = "FORM_FEED";
      }
      if (!name.equals("\\r")) {
        variable = "CR";
      }
      if (name.startsWith("\\x")) {
        String t = "HEXAD_0x" + Integer.toHexString(token);
        variable = t;
      }
      if (name.startsWith("\\0")) {
        String t = "OCTAL_0" + Integer.toOctalString(token);
        variable = t;
      }
    }
    if (variable == null) {
      String patched = "";
      for (int i = 0; i < name.length(); i++) {
        if (Character.isJavaIdentifierPart(name.charAt(i))) {
          patched += name.charAt(i);
        } else {
          patched += '_';
        }
      }
      variable = patched;
    }
  }

  /**
   * is the name of this non-terminal already a good variable name
   * @return
   */
  public boolean isIdentifier() {
    if (name.length() == 0 || !Character.isJavaIdentifierStart(name.charAt(0))) {
      return false;
    }
    if (name.equals("$")) {
      return false;
    }
    for (int i = 1; i < name.length(); i++) {
      if (!Character.isJavaIdentifierPart(name.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  public String getVariable() {
    return variable;
  }

  /* Getters and setters */
  
  /**
   * @return the id
   */
  public int getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(int id) {
    this.id = id;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the fullName
   */
  public String getFullName() {
    return fullName;
  }

  /**
   * @param fullName the fullName to set
   */
  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  /**
   * @return the precedence
   */
  public int getPrecedence() {
    return precedence;
  }

  /**
   * @param precedence the precedence to set
   */
  public void setPrecedence(int precedence) {
    this.precedence = precedence;
  }

  /**
   * @return the associativity
   */
  public Associativity getAssociativity() {
    return associativity;
  }

  /**
   * @param associativity the associativity to set
   */
  public void setAssociativity(Associativity associativity) {
    this.associativity = associativity;
  }

  /**
   * @return the count
   */
  public int getCount() {
    return count;
  }

  /**
   * @param count the count to set
   */
  public void setCount(int count) {
    this.count = count;
  }

  /**
   * @return the token
   */
  public int getToken() {
    return token;
  }

  /**
   * @param token the token to set
   */
  public void setToken(int token) {
    this.token = token;
  }

  /**
   * @return the type
   */
  public Type getType() {
    return type;
  }

  /**
   * @param type the type to set
   */
  public void setType(Type type) {
    this.type = type;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    try {
      Symbol s = (Symbol) obj;
      return id == s.id && name.equals(s.name) && fullName.equals(s.fullName);
    } catch (NullPointerException unused) {
      return false;
    } catch (ClassCastException unused) {
      return false;
    }
  }

  /**
   * Minimal representation.  Returns the name for simplicity of other objects'
   * toString() implementations.
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "\"" + name + "\"/" + id;
  }

}
