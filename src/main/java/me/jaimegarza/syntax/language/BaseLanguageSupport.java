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
package me.jaimegarza.syntax.language;

import java.io.PrintStream;

import me.jaimegarza.syntax.EmbeddedCodeProcessor;
import me.jaimegarza.syntax.Lexer;
import me.jaimegarza.syntax.code.Fragments;
import me.jaimegarza.syntax.definition.State;
import me.jaimegarza.syntax.definition.Symbol;
import me.jaimegarza.syntax.definition.Type;
import me.jaimegarza.syntax.env.Environment;
import me.jaimegarza.syntax.env.RuntimeData;

/**
 * Class that contains routines common to all language drivers
 * 
 * @author jaimegarza@gmail.com
 *
 */
public abstract class BaseLanguageSupport implements LanguageSupport {

  protected Environment environment;
  protected RuntimeData runtime;
  
  @Override
  public boolean getDefaultIncludeFlag() {
    return false;
  }

  @Override
  public void emitLine(int lineNumber) {
  }
  
  @Override
  public void emitLine(int lineNumber, String filename) {
  }

  @Override
  public int getNumberOfSpacesPerIndent() {
    return 2;
  }

  @Override
  public String indent(PrintStream out, int n) {
    StringBuilder b = new StringBuilder();
    int spaces = n * getNumberOfSpacesPerIndent();
    for (int i = 0; i < spaces; i++) {
      b.append(' ');
    }
    String indentString = b.toString();
    if (out != null) {
      out.print(indentString);
    }
    return indentString;
  }
  
  @Override
  public String indent(int n) {
    return indent(null, n);
  }
  
  /*
   * Getters and setters
   */

  /**
   * @return the environment
   */
  public Environment getEnvironment() {
    return environment;
  }

  /**
   * @param environment the environment to set
   */
  public void setEnvironment(Environment environment) {
    this.environment = environment;
    setRuntime(environment.getRuntimeData());
  }
  
  /**
   * @return the runtime
   */
  public RuntimeData getRuntime() {
    return runtime;
  }

  /**
   * @param runtime the runtime to set
   */
  public void setRuntime(RuntimeData runtime) {
    this.runtime = runtime;
  }
  
  /*
   * Adopting the C, JAVA as the default for all languages.  It may need to
   * be moved as more languages are implemented
   * (non-Javadoc)
   * @see me.jaimegarza.syntax.language.LanguageSupport#generateRuleCode(me.jaimegarza.syntax.definition.Lexer, int, java.lang.String)
   */
  @Override
  public boolean generateRuleCode(Lexer lexer, EmbeddedCodeProcessor processor, int elementCount, String nonTerminalId) {
    int nBracks = 0;
    boolean end = false;
    
    while (!end) {
      switch (runtime.currentCharacter) {
        case ';': /* final action in C & comment in ASM */
          if (nBracks == 0) {
            end = true;
          }
          break;

        case '{': /* level++ in C, JAVA */
          nBracks++;
          break;

        case '}': /* level -- in C, JAVA */
          if (--nBracks <= 0) {
            end = true;
          }
          break;

        case '/': /* possible comment in C, JAVA */
          environment.output.print(runtime.currentCharacter);
          lexer.getCharacter();
          if (runtime.currentCharacter != '*') {
            continue;
          }

          if (!processor.skipAndOutputCompositeComment(lexer, '*', '/')) {
            return false; 
          }
          continue;

        case '\'': /* constant */
        case '"': /* string */
          processor.generateConstant(lexer, runtime.currentCharacter);
          break;

        case '\n':
          environment.output.print(runtime.currentCharacter);
          lexer.getCharacter();
          indent(environment.output, environment.getIndent() + 1);
          continue;

        case 0:
          environment.error(-1, "Unfinished action detected.");
          return false;

        case '$':
          int command = manageDollar(lexer, processor, elementCount, nonTerminalId);
          if (command == 0) {
            return false;
          } else if (command > 0) {
            continue;
          }
          break;
      }
      environment.output.print(runtime.currentCharacter);
      lexer.getCharacter();
    }

    return true;
  }
  
  protected int manageDollar(Lexer lexer, EmbeddedCodeProcessor processor, int elementCount, String nonTerminalId) {
    Type type = null;
    int sign = 1;
    
    lexer.getCharacter();
    if (runtime.currentCharacter == '<') { /* type */
      type = processor.getTypeFromStream(lexer);
      if (type == null) { 
        return 0; // command a return false
      }
    }
    if (runtime.currentCharacter == '$') {
      if (!processor.generateDollarDollar(lexer, elementCount, nonTerminalId, type)) {
        return 0; // command a return false
      }
      return 1; // command a continue
    }
    if (runtime.currentCharacter == '-') {
      sign = -sign;
      lexer.getCharacter();
    }
    if (Character.isDigit(runtime.currentCharacter)) {
      if (!processor.generateDollarNumber(lexer, elementCount, type, sign)) {
        return 0; // command a return false
      }
      return 1; // command a continue
    }
    
    // fall through
    environment.output.print('$');
    if (sign < 0) {
      environment.output.print('-');
    }
    return -1; // command a break
  }

  protected boolean lexerDollar(Lexer lexer) {
    lexer.getCharacter();
    if (runtime.currentCharacter == '+') {
      lexer.getCharacter();
      environment.output.printFragment("getc");
      return true;
    } else if (runtime.currentCharacter == 'c') {
      lexer.getCharacter();
      environment.output.printFragment("currentChar");
      return true;
    } else if (runtime.currentCharacter == 'v') {
      lexer.getCharacter();
      environment.output.printFragment(Fragments.LEXICAL_VALUE);
      return true;
    }
    environment.output.print('$');
    return false;
  }

  protected boolean lexerComment(Lexer lexer, char characterToFind) {
    environment.output.print(runtime.currentCharacter);
    lexer.getCharacter();
    if (runtime.currentCharacter != '*') {
      return true;
    }
  
    environment.output.print(runtime.currentCharacter);
    lexer.getCharacter();
    boolean bBreak = false;
    while (!bBreak) {
      if (runtime.currentCharacter == '\0') {
        environment.error(-1, "Unfinished comment.");
        return false;
      }
      while (runtime.currentCharacter == '*') {
        environment.output.print(runtime.currentCharacter);
        if ((lexer.getCharacter()) == characterToFind) {
          bBreak = true;
        }
      }
      environment.output.print(runtime.currentCharacter);
      lexer.getCharacter();
    }
    return true;
  }

  protected boolean lexerString(Lexer lexer, char characterToFind) {
    environment.output.print(runtime.currentCharacter);
    while ((lexer.getCharacter()) != characterToFind) {
      if (runtime.currentCharacter == '\0') {
        environment.error(-1, "Statement ' .. ' or \" .. \" not ended");
        return false;
      }
      if (runtime.currentCharacter == '\n') {
        environment.error(-1, "End of line reached on string literal.");
        break;
      }
      if (runtime.currentCharacter == '\\') {
        environment.output.print(runtime.currentCharacter);
        lexer.getCharacter();
      }
      environment.output.print(runtime.currentCharacter);
    }
    return true;
  }
  
  @Override
  public boolean generateLexerCode(Lexer lexer) {
    int nBracks = 0;
    boolean end = false;
    boolean bStart = true;
    boolean bSkip = false;
  
    while (!end) {
      switch (runtime.currentCharacter) {
        case '$':
          if (lexerDollar(lexer)) {
            continue;
          }
          break;
  
        case ';': /* finish of action */
          if (nBracks <= 0) {
            end = true;
          }
          break;
  
        case '{': /* level++ in C */
          nBracks++;
          break;
  
        case '}': /* level -- in C */
          if (--nBracks <= 0 && bSkip) {
            end = true;
          }
          if (end && bSkip) {
            lexer.getCharacter();
            continue;
          }
          break;
  
        case '/': /* possible comment in C */
          if(!lexerComment(lexer, '/')) {
            return false;
          }
          continue;
  
        case '\'': /* constant */
        case '"': /* string */
          if (!lexerString(lexer, runtime.currentCharacter)) {
            return false;
          }
          break;
          
        case '\n':
          environment.output.print(runtime.currentCharacter);
          lexer.getCharacter();
          indent(environment.output, environment.getIndent() + 1);
          continue;
  
        case 0:
          environment.error(-1, "Unfinished action detected.");
          return false;
  
      }
      if (!bStart || runtime.currentCharacter != '{') {
        environment.output.print(runtime.currentCharacter);
      } else {
        bSkip = true;
      }
      if (runtime.currentCharacter > ' ') {
        bStart = false;
      }
      lexer.getCharacter();
    }
    return true;
  }

  protected String getErrorMessage(State I) {
    int msgIndex = I.getMessage();
    if (msgIndex >= 0) {
      return runtime.getErrorMessages().get(msgIndex);
    } else {
      return "No error assigned";
    }
  }

  protected String escapeDoubleQuotes(String error) {
    return error.replaceAll("\\\"", "\\\\\"");
  }

  protected String getShortSymbolName(Symbol t) {
    String name = t.getFullName();
    if (name.startsWith("\"") || name.startsWith("\'")) {
      name = name.substring(1);
    }
    if (name.endsWith("\"") || name.endsWith("\'")) {
      name = name.substring(0, name.length()-1);
    }
    if (name.length() > 6) {
      name = name.substring(0, 6);
    }
    if (name.endsWith(" ")) {
      name = name.substring(0, name.length()-1);
    }
    return name;
  }
}
