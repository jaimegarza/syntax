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
package me.jaimegarza.syntax.generator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import me.jaimegarza.syntax.EmbeddedCodeProcessor;
import me.jaimegarza.syntax.Lexer;
import me.jaimegarza.syntax.env.Environment;
import me.jaimegarza.syntax.exception.ParsingException;
import me.jaimegarza.syntax.model.parser.Associativity;
import me.jaimegarza.syntax.model.parser.ErrorToken;
import me.jaimegarza.syntax.model.parser.NonTerminal;
import me.jaimegarza.syntax.model.parser.Rule;
import me.jaimegarza.syntax.model.parser.RuleItem;
import me.jaimegarza.syntax.model.parser.Symbol;
import me.jaimegarza.syntax.model.parser.Terminal;
import me.jaimegarza.syntax.model.parser.TokenGroup;
import me.jaimegarza.syntax.model.parser.Type;
import me.jaimegarza.syntax.util.FormattingPrintStream;
import me.jaimegarza.syntax.util.PathUtils;

/**
 * This class contains the "non-parser" code, or supporting code for the syntax
 * parser.<p>
 * 
 * This is done so that the CodeParser can be generated from a syntaxt file
 * 
 * @author jgarza
 *
 */
public abstract class AbstractCodeParser extends AbstractPhase implements Lexer, EmbeddedCodeProcessor {
  private static final String DEFAULT_LEXER_MODE = "default";

  protected static final String DISTINGUISHED_SYMBOL_NAME = "$start";

  protected Stack<Character> inputChars = new Stack<Character>();
  protected boolean bActionDone = false;
  protected int currentRuleIndex;
  protected Type currentType;
  protected int markers = 0;
  protected boolean isCurlyBrace;
  protected boolean isEqual;
  protected boolean isRegexSlash;
  protected boolean isError;
  protected int tokenNumber;
  protected String currentNonTerminalName;
  protected boolean mustClose;
  protected boolean finalActions;
  protected boolean isErrorToken;
  protected Associativity ruleAssociativity;
  protected int rulePrecedence;
  protected int tokenActionCount;
  protected int actLine;
  protected boolean isFirstToken = true;
  protected int numberOfErrorTokens;
  
  public char currentChar;
  public String recognized;

  /**
   * Default Constructor
   * @param environment is the syntax environment
   */
  public AbstractCodeParser(Environment environment) {
    super(environment);

  }
  
  /**
   * Declare a token
   * @param id is the short name of the token
   * @param isErrorToken specifies if this token was declared with %error
   * @param associativity defines if a token is defined with %left, %right, etc.
   * @param precedence is the numeric precedence of the token
   * @param type is the type of the non-terminal
   * @param tokenNumber is the number of the token, or its value
   * @param fullName is the fullname, if given, of the token
   * @return true if OK
   */
  protected Terminal declareOneTerminal(String id, boolean isErrorToken, Associativity associativity, int precedence, Type type, int tokenNumber, String fullName) {
    Terminal terminal = runtimeData.findTerminalByName(id);
    if (terminal == null) {
      terminal = isErrorToken ? new ErrorToken(id) : new Terminal(id);
      runtimeData.getTerminals().add(terminal);
    }
    terminal.setCount(terminal.getCount() - 1);
    
    if (associativity != Associativity.NONE) {
      if (terminal.getAssociativity() != Associativity.NONE) {
        environment.error(-1, "Reassigning precedence/associativity for token \'%s\'.", terminal.getName());
        return null;
      }
      terminal.setPrecedence(precedence);
      terminal.setAssociativity(associativity);
    }
    
    if (type != null) {
      terminal.setType(type);
      type.addUsage(terminal);
    }
    
    if (tokenNumber >= 0) {
      if (terminal.getToken() != -1 && terminal.getToken() != tokenNumber) {
        environment.error(-1, "Warning: Token \'%s\' already has a value.", terminal.getName());
      }
      for (Terminal t : runtimeData.getTerminals()) {
        if (t != terminal && t.getToken() == tokenNumber) {
          environment.error(-1, "Warning: Token number %d already used on token \'%s\'.",
            tokenNumber, t.getName());
          return null;
        }
      }
      terminal.setToken(tokenNumber);
    }
    
    if (fullName != "" && fullName != null) {
      terminal.setFullName(fullName);
    }
    
    //if ($4 != null) {
    // SetEndToken($4, terminal.getName());
    //}
    
    return terminal;
  }
  
  /**
   * Declare one non terminal in the symbol table
   * @param typeName the desired type
   * @param name the name of the symbol
   * @return true if OK
   */
  protected boolean declareOneNonTerminal(String typeName, String name) {
    if (runtimeData.findTerminalByName(name) != null) {
      environment.error(-1, "Token \'%s\' cannot appear on a %%type clause.", name);
      return false;
    }
    NonTerminal nonTerminal = runtimeData.findNonTerminalByName(name);
    if (nonTerminal == null) {
      nonTerminal = new NonTerminal(name);
      runtimeData.getNonTerminals().add(nonTerminal);
    } else {
      nonTerminal.setCount(nonTerminal.getCount() - 1);
    }
    Type type = new Type(typeName);
    if (runtimeData.getTypes().contains(type)) {
      type = runtimeData.getTypes().get(runtimeData.getTypes().indexOf(type));
    } else {
      runtimeData.getTypes().add(type);
    }
    type.addUsage(nonTerminal);
    nonTerminal.setType(type);
    return true;
  }
  
  /**
   * Declare one given type
   * @param typeName the desired type
   * @return true if OK
   */
  protected boolean declareOneType(String typeName) {
    Type type = runtimeData.findType(typeName);
    if (type != null) {
      environment.error(-1, "Type \'%s\' already declared.", typeName);
      return false;
    }
    type = new Type(typeName);
    runtimeData.getTypes().add(type);
    return true;
  }
  
  /**
   * Adds one rule item to the current list of items
   * @param symbolName the name of the symbol being used
   * @param value the integer value of the symbol
   * @param mustClose whether this token must close
   * @return true if OK
   */
  public boolean declareOneItem(String symbolName, int value, boolean mustClose) {
    if (isFirstToken) {
      rulePrecedence = 0;
      ruleAssociativity = Associativity.NONE;
      isFirstToken = false;
    }
    if (bActionDone) {
      Rule stx = newEmptyRule();
      String rootName = "$code-fragment-" + (runtimeData.codeRule++);
      NonTerminal codeFragment = new NonTerminal(rootName);
      codeFragment.setCodeFragment(true);
      runtimeData.getNonTerminals().add(codeFragment);
      stx.setLeftHand(codeFragment);
      codeFragment.setCount(codeFragment.getCount() + 1);
      codeFragment.setPrecedence(1); /* used as non terminal */
      newItem(codeFragment);
      //stx.getItems().add(item);
      bActionDone = false;
    }
    Symbol symbol;
    NonTerminal nonTerminal = runtimeData.findNonTerminalByName(symbolName);
    if (nonTerminal == null) {
      Terminal terminal = runtimeData.findTerminalByName(symbolName);
      if (terminal != null) {
        rulePrecedence = terminal.getPrecedence();
        ruleAssociativity = terminal.getAssociativity();
        symbol = terminal;
      } else {
        if (mustClose && value >= 0) {
          terminal = new Terminal(symbolName);
          runtimeData.getTerminals().add(terminal);
          if (value >= 0) {
            for (Terminal cual : runtimeData.getTerminals()) {
              if (cual != terminal && cual.getToken() == value) {
                environment.error(-1, "Warning: Token number %d already used on token \'%s\'.",
                    value, cual.getName());
                return false;
              }
            }
            terminal.setToken(value);
          }
          symbol = terminal;
        } else {
          nonTerminal = new NonTerminal(symbolName);
          runtimeData.getNonTerminals().add(nonTerminal);
          nonTerminal.setCount(nonTerminal.getCount() + 1);
          symbol = nonTerminal;
        }
      }
    } else {
      symbol = nonTerminal;
    }
    newItem(symbol);
    return true;
  }
  
  /**
   * group a list of tokens
   * @param tokens the list of tokens
   * @param groupName the name of the group
   * @param displayName the display name of the group
   * @return true if OK
   */
  public boolean groupTokens(List<String> tokens, String groupName, String displayName) {
    List<Terminal> terminals = new LinkedList<Terminal>();
    for (String tokenName : tokens) {
      Terminal terminal = runtimeData.findTerminalByName(tokenName);
      if (terminal == null) {
        environment.error(-1, "The token " + tokenName + " has not been defined.  Grouping cannot proceed.");
        return false;
      }
      terminals.add(terminal);
    }
    TokenGroup group = new TokenGroup(terminals, groupName, displayName);
    runtimeData.getErrorGroups().add(group);
    return true;
  }
  
  /**
   * A %prec was given, and as such I need to load its context globally
   * @param tokenName is the name of the token given in the %prec
   * @return true on success
   */
  public boolean computeAssociativityAndPrecedence(String tokenName) {
    NonTerminal nonTerminal = runtimeData.findNonTerminalByName(tokenName);
    if (nonTerminal == null) {
      Terminal terminal = runtimeData.findTerminalByName(tokenName);
      if (terminal == null) {
        environment.error(-1, "Warning: token \'%s\' not declared.", tokenName);
        return false;
      } else {
        rulePrecedence = terminal.getPrecedence();
        ruleAssociativity = terminal.getAssociativity();
      }
    } else {
      environment.error(-1, "Warning: token \'%s\' not declared as token, but as a non-terminal.", tokenName);
      return false;
    }
    return true;
  }

  /**
   * Change the display name of a non terminal
   * @param name is the short name of the non terminal
   * @param fullName is its full name
   * @return true if OK
   */
  protected boolean nameOneNonTerminal(String name, String fullName) {
    if (runtimeData.findTerminalByName(name) != null) {
      environment.error(-1, "Token \'%s\' cannot appear on a %%name clause.", name);
      return false;
    }
    NonTerminal nonTerminal = runtimeData.findNonTerminalByName(name);
    if (nonTerminal == null) {
      nonTerminal = new NonTerminal(name);
      runtimeData.getNonTerminals().add(nonTerminal);
    } else {
      nonTerminal.setCount(nonTerminal.getCount() - 1);
    }
    nonTerminal.setFullName(fullName);
    return true;
  }

  /**
   * This routine places the non terminal left hand of a rule
   * @param name is the non terminal's name
   * @return true if OK
   */
  protected boolean setLeftHandOfLastRule(String name) {
    if (runtimeData.findTerminalByName(name) != null) {
      environment.error(-1, "The token \'%s\' cannot appear to the right of a rule.", name);
      return false;
    }
    NonTerminal nonTerminal = runtimeData.findNonTerminalByName(name);
    if (nonTerminal == null) {
      nonTerminal = new NonTerminal(name);
      runtimeData.getNonTerminals().add(nonTerminal);
    } else {
      nonTerminal.setCount(nonTerminal.getCount() - 1);
    }
    nonTerminal.setPrecedence(1); /* usado como no terminal */
    for (int i = currentRuleIndex; i < runtimeData.getRules().size(); i++) {
      Rule rule = runtimeData.getRules().get(i);
      if (rule.getLeftHand() == null) {
        rule.setLeftHand(nonTerminal);
      }
    }
    bActionDone = false;
    return true;
  }

  /**
   * Use the character stream to decode one character from octal<p>
   * for instance '\017'
   * @return the octal entered character
   */
  protected char decodeOctal() {
    int iCount = 3;
    char c2 = 0;
  
    while (iCount != 0) {
      c2 *= 8;
  
      if (currentChar >= '0' && currentChar <= '7') {
        c2 += currentChar - '0';
        getNextCharacter();
      } else if (currentChar == '\0') {
        return c2;
      } else {
        break;
      }
  
      iCount--;
    }
  
    return c2;
  }

  /**
   * Use the character stream to decode a control char<p>
   * \a - \z
   * @return the control char
   */
  protected char decodeControlChar() {
    char c2;
    getNextCharacter();
  
    if (currentChar == '\0') {
      return '\0';
    }
  
    if (currentChar >= 'a' && currentChar <= 'z') {
      c2 = currentChar;
      getNextCharacter();
      return (char) (c2 - ('a' - 1));
    } else if (currentChar >= 'A' && currentChar <= 'Z') {
      c2 = currentChar;
      getNextCharacter();
      return (char) (c2 - ('A' - 1));
    } else {
      return 'c' - 'a';
    }
  }

  /**
   * Use the character stream to decode a character entered with hex codes<P>
   * for instance \x1f
   * @return the character
   */
  protected char decodeHex() {
    int iCount = 2;
    char c2 = 0;
  
    getNextCharacter();
  
    while (iCount != 0) {
      c2 *= 16;
  
      if (currentChar >= '0' && currentChar <= '9') {
        c2 += currentChar - '0';
      } else if (currentChar >= 'a' && currentChar <= 'f') {
        c2 += 10 + (currentChar - 'a');
      } else if (currentChar >= 'A' && currentChar <= 'F') {
        c2 += 10 + (currentChar - 'A');
      } else if (currentChar == '\0') {
        return '\0';
      } else {
        return 'x' - 'a';
      }
  
      iCount--;
    }
  
    return c2;
  }

  /**
   * Use the character stream to decode the next escaped character
   * (i.e. hex, octal, control)
   * @return the encoded character
   */
  protected char decodeEscape() {
    char c2;
    switch (currentChar) {
      case '0':
      case '1':
      case '2':
      case '3':
      case '4':
      case '5':
      case '6':
      case '7':
        return decodeOctal();
      case 'a':
        getNextCharacter();
        return 7;
      case 'b':
        getNextCharacter();
        return '\b';
      case 'c':
        getNextCharacter();
        return decodeControlChar();
      case 'e':
        getNextCharacter();
        return '\\';
      case 'f':
        getNextCharacter();
        return '\f';
      case 'n':
        getNextCharacter();
        return '\n';
      case 'r':
        getNextCharacter();
        return '\r';
      case 't':
        getNextCharacter();
        return '\t';
      case 'v':
        getNextCharacter();
        return 11;
      case 'x':
        getNextCharacter();
        return decodeHex();
      default:
        c2 = currentChar;
        getNextCharacter();
        return c2;
    }
  }

  @Override
  /**
   * Get the next character. It can go to the stack of chars as needed
   * @return the next character
   */
  public char getNextCharacter() {
    if (inputChars.size() > 0) {
      currentChar = inputChars.pop();
      if (currentChar == '\n') {
        runtimeData.lineNumber++;
        runtimeData.columnNumber = 0;
      }
      runtimeData.columnNumber++;
      return currentChar;
    }
  
    // Get one char from stream
    try {
      int rc = environment.source.read();
      currentChar = (char) rc;
      // EOF?
      if (rc == -1) {
        currentChar = 0;
      }
    } catch (IOException e) {
      currentChar = 0;
    }
    
    if (currentChar == -1 || currentChar == 0) {
      return 0;
    }
  
    // EOL?
    if (currentChar == '\n') {
      runtimeData.lineNumber++;
      runtimeData.columnNumber = 0;
    }
  
    // CTRL-Z?  <-- suspect code
    if (currentChar == 26) {
      return 0;
    }
  
    runtimeData.columnNumber++;
    return currentChar;
  }

  @Override
  public void ungetChar(char c) {
    inputChars.push(c);
    if (c == '\n') {
      runtimeData.lineNumber--;
    }
  }
  
  @Override
  public char getCurrentCharacter() {
    return currentChar;
  }

  /****************************EMBEDDED CODE PROCESSOR **************************/
  public Type getTypeFromStream(Lexer lexer) {
    Type type;
    String s2;
    s2 = runtimeData.currentStringValue;
    lexer.getNormalSymbol();
    type = runtimeData.findType(runtimeData.currentStringValue);
    if (type == null) {
      environment.error(-1, "Warning: cannot find type '%s'.  It will be declared", runtimeData.currentStringValue);
      type = new Type(runtimeData.currentStringValue);
      runtimeData.getTypes().add(type);
    }
    runtimeData.currentStringValue = s2;
    return type;
  }

  /**
   * Return the item with the given index, or null
   * @param index is the offset into the rules, zero based
   * @return the rule item
   */
  private RuleItem getCurrentRuleItem(int index) {
    RuleItem item= null;
    if (runtimeData.currentRuleItems != null && index < runtimeData.currentRuleItems.size()) {
      item = runtimeData.currentRuleItems.get(index);
    }
    return item;
  }
  
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
  public boolean generateDollarLetter(Lexer lexer, int elementCount, Type type, String nonTerminalId) {
    String id = "";
    while (Character.isJavaIdentifierPart(currentChar) ) {
      id += currentChar;
      lexer.getNextCharacter();
    }
    int index = -1;
    if (currentChar == '[') {
      lexer.getNextCharacter();
      index = getDollarTextIndexFromStream();
      if (index == -2) {
        return false;
      }
    }
    
    Symbol element = getSymbolWithName(id);
    if (element == null) {
      environment.error(-1, "element " + id + " not found.");
      return false;
    }

    // check to see if this is the symbol of the rule.  No indexing for $$
    if (runtimeData.findNonTerminalByName(nonTerminalId).equals(element) && index == -1) {
      lexer.ungetChar(currentChar);
      return generateDollarDollar(lexer, elementCount, nonTerminalId, element.getType());
    }
    
    // in the rules now.  Locate the rule with the symbol obtained before.
    int itemIndex = 1;
    int elementIndex = 0;
    for (RuleItem item : runtimeData.currentRuleItems) {
      if (item.getSymbol().equals(element)) {
        elementIndex ++;
        if (index == -1 || index == elementIndex) {
          break;
        }
      }
      itemIndex++;
    }
    if (itemIndex > runtimeData.currentRuleItems.size()) {
      environment.error(-1, "Element " + id + " was not used in the rules.");
      return false;
    }
    
    // redirect to the $digit routine by placing elements on the stack.
    String s = Integer.toString(itemIndex);
    ungetChar(currentChar);
    for (int i = s.length()-1; i >= 0; i--) {
      lexer.ungetChar(s.charAt(i));
    }
    lexer.getNextCharacter();
    return generateDollarNumber(lexer, elementCount, type == null ? element.getType() : type, 1);
  }

  /**
   * Obtain a symbol, either terminal or non terminal, with the given name.
   * @param name the name of the symbol
   * @return the symbol
   */
  protected Symbol getSymbolWithName(String name) {
    Symbol element = runtimeData.findNonTerminalByName(name);
    if (element == null) {
      element = runtimeData.findTerminalByName(name);
    }
    return element;
  }

  /**
   * Find the $Letter[&lt;digits&gt;] digits. If &lt;digits&gt; start with
   * a zero, octal radix is assumed. 
   * @return the index.
   */
  protected int getDollarTextIndexFromStream() {
    int index;
    while (currentChar == ' ') {
      getNextCharacter();
    }
    index = 0;
    int base;
    if (currentChar == '0') {
      base = 8;
    } else {
      base = 10;
    }
    while (Character.isDigit(currentChar)) {
      index = index * base + currentChar - '0';
      getNextCharacter();
    }
    while (currentChar == ' ') {
      getNextCharacter();
    }
    if (currentChar != ']') {
      environment.error(-1, "Unfinished index detected.");
      return -2;
    }
    getNextCharacter();
    return index;
  }
  
  /**
   * $1, $2, $-3, etc detected.  Proceed with the code generation
   * 
   * @param lexer the element that will give me the lexical logic
   * @param elementCount the number of elements in the rule
   * @param type the type of the element
   * @param sign, 1 for possitive, -1 for negative
   * @return true if everything is OK
   */
  public boolean generateDollarNumber(Lexer lexer, int elementCount, Type type, int sign) {
    int num;
    int base;
    num = 0;
    if (currentChar == '0') {
      base = 8;
    } else {
      base = 10;
    }
    while (Character.isDigit(currentChar)) {
      num = num * base + currentChar - '0';
      lexer.getNextCharacter();
    }
    num = num * sign - elementCount;
    if (num > 0) {
      environment.error(-1, "Incorrect value of \'$%d\'. Bigger than the number of elements.", num + elementCount);
      return false;
    }
    if (num == 0) {
      environment.output.printFragment("stxstack", "");
    } else {
      environment.output.printFragment("stxstack", String.format("%+d", num));
    }
    if (runtimeData.getTypes().size() != 0) {
      if (num + elementCount <= 0 && type == null) {
        environment.error(-1, "Cannot determine the type for \'$%d\'.", num + elementCount);
        return false;
      }
      if (type == null) {
        int ruleIndex = 0;
        RuleItem rule = getCurrentRuleItem(0);
        for (int i = 1; i < num + elementCount && rule != null; i++) {
          rule = getCurrentRuleItem(++ruleIndex);
        }
        if (rule != null) {
          Terminal terminal = runtimeData.findTerminalByName(rule.getSymbol().getName());
          if (terminal != null) {
            terminal.setCount(terminal.getCount() - 1);
            type = terminal.getType();
          } else {
            NonTerminal nonTerminal = runtimeData.findNonTerminalByName(rule.getSymbol().getName());
            if (nonTerminal != null) {
              nonTerminal.setCount(nonTerminal.getCount() - 1);
              type = nonTerminal.getType();
            }
          }
        }
      } else if (type == Type.NullType) {
        type = null;
      }
      if (type != null) {
        environment.output.printf(".%s", type.getName());
      }
    }
    return true;
  }

  /**
   * $$ detected.  Proceed with the code generation
   * 
   * @param lexer the element that will give me the lexical logic
   * @param elementCount the number of elements in the rule
   * @param nonTerminalId the non terminal id for the rule
   * @param type the type of the element
   * @return true if everything is OK
   * */
  public boolean generateDollarDollar(Lexer lexer, int elementCount, String nonTerminalId, Type type) {
    if (elementCount == 1) {
      environment.output.printFragment("stxstack", "");
    } else if (elementCount != 0) {
      environment.output.printFragment("stxstack", "-" + Integer.toString(elementCount - 1));
    } else {
      environment.output.printFragment("stxstack", "+1");
    }
    if (runtimeData.getTypes().size() != 0) {
      if (type == null) {
        NonTerminal idp = runtimeData.findNonTerminalByName(nonTerminalId);
        if (idp != null) {
          idp.setCount(idp.getCount() - 1);
          type = idp.getType();
        }
      } else if (type == Type.NullType) {
        type = null;
      }
      if (type != null) {
        environment.output.printf(".%s", type.getName());
      }
    }
    lexer.getNextCharacter();
    return true;
  }

  public boolean generateConstant(Lexer lexer, char characterType) {
    environment.output.print(currentChar);
    while ((lexer.getNextCharacter()) != characterType) {
      if (currentChar == '\0') {
        environment.error(-1, "Statement ' .. ' or \" .. \" not ended.");
        return false;
      }
      if (currentChar == '\n') {
        environment.error(-1, "End of line reached on string literal.");
        return false;
      }
      if (currentChar == '\\') {
        environment.output.print(currentChar);
        lexer.getNextCharacter();
      }
      environment.output.print(currentChar);
    }
    return true;
  }

  public boolean skipAndOutputCompositeComment(Lexer lexer, char secondaryCharacter, char characterToFind) {
    boolean bBreak;
    
    environment.output.print(currentChar);
    lexer.getNextCharacter();
    bBreak = false;
    while (!bBreak) {
      if (currentChar == '\0') {
        environment.error(-1, "Unfinished comment.");
        return false;
      }
      while (currentChar == secondaryCharacter) {
        environment.output.print(currentChar);
        if ((lexer.getNextCharacter()) == characterToFind) {
          bBreak = true;
        }
      }
      environment.output.print(currentChar);
      lexer.getNextCharacter();
    }
    return true;
  }

  /**
   * A statement for code generation was found.  Finish it.
   */
  protected void generateCaseEnd() {
    environment.language.generateCaseEnd();
  }

  /**
   * Output the case for a given rule
   * @param ruleNumber is the rule number to be emitted in the case statement
   * @param comment is the comment
   * @return for indentation purposes the column where the case ended
   */
  protected int generateCaseStatement(int ruleNumber, String comment) {
    return environment.language.generateCaseStart(ruleNumber, Integer.toString(ruleNumber + 1), comment);
  }

  /**
   * Output the top of the rules if needed
   */
  protected void generateCodeGeneratorHeader() {
    if (runtimeData.ruleActionCount == 0) {
      environment.language.generateCodeGeneratorHeader();
    }
  }

  /**
   * copy action until the next ';' or '}' that actually closes
   * @param lexerMode is the mode of the lexer
   * @param token is the token related to the generation. Used in $t
   * @return true if OK
   */
  protected boolean generateLexerCode(String lexerMode, Terminal token) {
    FormattingPrintStream output = environment.getLexerModePrintStream(lexerMode);
    environment.language.generateLexerCode(output, lexerMode, this, token, 0);
    tokenActionCount++;
    return true;
  }
  
  /**
   * Produce the if statement that will match the regex symbol and the default return code
   * @param dfaNode is the index of the dfa vertex
   * @param token is the token related to the generation. Used in $t
   * @return true if successful
   */
  protected boolean generateDefaultRegexCode(int dfaNode, Terminal token) {
    FormattingPrintStream output = environment.getLexerModePrintStream("default");
    environment.language.generateRegexMatch(output, dfaNode);
    environment.language.generateRegexReturn(output, token);
    environment.language.generateRegexEnd(output);    
    tokenActionCount++;
    return true;
  }

  /**
   * Produce the if statement that will match the regex symbol and include additional code
   * @param lexerMode is the mode of the scanner
   * @param dfaNode is the index of the dfa vertex
   * @param token is the token related to the generation. Used in $t
   * @return true if successful
   */
  protected boolean generateRegexCode(String lexerMode, int dfaNode, Terminal token) {
    FormattingPrintStream output = environment.getLexerModePrintStream(lexerMode);
    environment.language.generateRegexMatch(output, dfaNode);
    environment.language.generateLexerCode(output, lexerMode, this, token, 1);
    environment.language.generateRegexEnd(output);        
    tokenActionCount++;
    return true;
  }

  /**
   * Generate the ending portion of the code generation
   */
  protected void generateCodeGeneratorFooter() {
    if (runtimeData.ruleActionCount != 0) {
      environment.language.generateCodeGeneratorFooter();
    } else {
      environment.language.generateVoidCodeGenerator();
    }
  }

  /**
   * Generate the bottom of the lexer
   */
  protected void generateLexerFooter() {
    if (tokenActionCount != 0) {
      if (environment.lexerModes.get(DEFAULT_LEXER_MODE) == null) {
        environment.getLexerModePrintStream(DEFAULT_LEXER_MODE);
      }
      List<String> modes = new ArrayList<String>(environment.lexerModes.keySet().size());
      modes.addAll(environment.lexerModes.keySet());
      Collections.sort(modes);

      int index = 0;
      if (modes.size() > 0) {
        for (String mode: modes) {
          environment.language.generateLexerModeDefinition(mode, index++);
        }
      }

      environment.language.generateLexerHeader(modes);
      if (modes.size() > 1) {
        index = 0;
        for (String mode: modes) {
          environment.language.generateLexerModeCase(mode, index++);
        }
      }
      
      environment.language.generateLexerFooter();
      
      if (modes.size() > 1) {
        for (String mode: modes) {
          FormattingPrintStream stream = environment.lexerModes.get(mode);
          String lexerCode = stream.getWriter().toString();
          environment.language.emitLine(runtimeData.lineNumber + 1);
          environment.language.generateLexerModeHeader(mode);
          environment.output.print(lexerCode);
          environment.output.println();
          environment.language.generateLexerModeFooter(mode);
        }
      }
    }
  }

  /**
   * During a declaration, emit the accompanying code
   * @return true if OK
   */
  protected boolean generateDeclaration() {
    while (Character.isWhitespace(currentChar)) {
      getNextCharacter();
    }
    environment.language.emitLine(runtimeData.lineNumber);
    while (currentChar != '\0') {
      if (currentChar == '\\') {
        if ((getNextCharacter()) == '}') {
          getNextCharacter();
          return true;
        } else {
          environment.output.print('\\');
        }
      } else if (currentChar == '%') {
        if ((getNextCharacter()) == '}') {
          getNextCharacter();
          return true;
        } else {
          environment.output.print('%');
        }
      } else if (currentChar == '$') {
        getNextCharacter();
        if (currentChar == '$') {
          getNextCharacter();
          switch (currentChar) {
            case 'b':
              getNextCharacter();
              environment.output.print(PathUtils.getFileNameNoExtension(environment.getOutputFile().getAbsolutePath()));
              break;
            case 'n':
              getNextCharacter();
              environment.output.print(PathUtils.getFileName(environment.getOutputFile().getAbsolutePath()));
              break;
            case 'f':
              getNextCharacter();
              environment.output.print(environment.getOutputFile().getAbsolutePath());
              break;
            case 'e':
              getNextCharacter();
              environment.output.print(PathUtils.getFileExtension(environment.getOutputFile().getAbsolutePath()));
              break;
            case 'p':
              getNextCharacter();
              environment.output.print(PathUtils.getFilePath(environment.getOutputFile().getAbsolutePath()));
              break;
            default:
              environment.output.print("$$");
          }
        } else {
          environment.output.print('$');
        }
      }
      environment.output.print(currentChar);
      getNextCharacter();
    }
    environment.error(-1, "End of file before \'\\}\' or \'%%}\'.");
    return false;
  }

  /**
   * For yacc compatibility this is called the union, but it is
   * really a structure
   * @return true if OK
   */
  protected boolean generateStructure() {
    environment.language.emitLine(runtimeData.lineNumber);
    runtimeData.setStackTypeDefined(true);
    return environment.language.generateStructure(this);
  }

  /**
   * new rule item with the given symbol
   * @param elem is the symbol associated to the rule
   * @return the new rule item
   */
  protected RuleItem newItem(Symbol elem) {
    RuleItem item;
  
    item = new RuleItem(elem);
    if (runtimeData.currentRuleItems == null) {
      runtimeData.currentRuleItems = new LinkedList<RuleItem>();
    }
    runtimeData.currentRuleItems.add(item);
    return item;
  }

  /**
   * new rule with no elements
   * @return the new rule
   */
  protected Rule newEmptyRule() {
    Rule rule;
  
    rule = new Rule(0, actLine, 0, null);
    runtimeData.getRules().add(rule);
    return rule;
  }

  /**
   * new rule with the currently recognized items
   * @return a new rule
   */
  protected Rule newRule() {
    Rule rule;
  
    rule = new Rule(0, actLine, rulePrecedence, null);
    if (runtimeData.currentRuleItems != null) {
      rule.getItems().addAll(runtimeData.currentRuleItems);
      for (RuleItem item : runtimeData.currentRuleItems) {
        item.setRule(rule);
      }
      runtimeData.currentRuleItems = null;
    }
    runtimeData.getRules().add(rule);
    rulePrecedence = 0;
    ruleAssociativity = Associativity.NONE;
    return rule;
  }

  /**
   * Starting rule.  Add it at the top.
   * 
   * @param root is the root symbol
   * @return the new rule
   */
  protected Rule newRootRule(NonTerminal root) {
    Rule rule;
  
    rule = new Rule(0, actLine, rulePrecedence, root);
    if (runtimeData.currentRuleItems != null) {
      rule.getItems().addAll(runtimeData.currentRuleItems);
      for (RuleItem item : runtimeData.currentRuleItems) {
        item.setRule(rule);
      }
      runtimeData.currentRuleItems = null;
    }
    runtimeData.getRules().add(0, rule);
    rulePrecedence = 0;
    ruleAssociativity = Associativity.NONE;
    return rule;
  }

  /**
   * Check non terminals whose precedence is zero, and make them terminals.
   */
  protected void reviewDeclarations() {
    for (int i = 0; i < runtimeData.getNonTerminals().size(); ) {
      NonTerminal nonTerminal = runtimeData.getNonTerminals().get(i);
      if (nonTerminal.getPrecedence() == 0) {
        environment.error(-1, "Warning: token \'%s\' not declared.", nonTerminal.getName());
        runtimeData.getTerminals().add(new Terminal(nonTerminal));
        runtimeData.getNonTerminals().remove(nonTerminal);
      } else {
        i++;
      }
    }
  }

  /**
   * Find out my root symbol
   */
  protected void computeRootSymbol() {
    runtimeData.setRoot(null);
    boolean bError = false;
    for (NonTerminal nonTerminal : runtimeData.getNonTerminals()) {
      if (nonTerminal.getCount() == 0) {
        if (runtimeData.getRoot() == null) {
          runtimeData.setRoot(nonTerminal);
        } else {
          bError = true;
          runtimeData.setRoot(null);
          break;
        }
      }
    }
  
    if (runtimeData.getStart() != null) { // Was it given with %start ?
      for (NonTerminal nonTerminal : runtimeData.getNonTerminals()) {
        if (nonTerminal.getCount() == 0 && !nonTerminal.equals(runtimeData.getStart())) {
          Rule stx = locateRuleWithId(nonTerminal.getId());
          environment.error(lineNumber(stx), "Warning: Symbol \'%s\' not used.", nonTerminal.getName());
        }
      }
    } else {
      if (runtimeData.getRoot() != null) {
        Rule stx = locateRuleWithId(runtimeData.getRoot().getId());
        environment.error(lineNumber(stx), "Assumed \'%s\' as distinguished symbol.", runtimeData.getRoot().getName());
        runtimeData.setStart(runtimeData.getRoot());
      } else if (bError) {
        for (NonTerminal id : runtimeData.getNonTerminals()) {
          if (id.getCount() == 0) {
            Rule stx = locateRuleWithId(id.getId());
            environment.error(lineNumber(stx), "Warning: Symbol \'%s\' not used.", id.getName());
          }
        }
        environment.error(-1, "Distinguished symbol cannot be determined. Use %%start.");
        return;
      } else {
        environment.error(-1, "The distinguished symbol does not exist.");
        return;
      }
    }
  
    boolean found = false;
    for (NonTerminal nonTerminal : runtimeData.getNonTerminals()) {
      if (nonTerminal.getName().equals(DISTINGUISHED_SYMBOL_NAME)) {
        runtimeData.setRoot(nonTerminal);
        found = true;
        break;
      }
    }
    if (!found) {
      NonTerminal root = new NonTerminal(DISTINGUISHED_SYMBOL_NAME);
      runtimeData.getNonTerminals().add(root);
      runtimeData.setRoot(root);
    }
    newItem(runtimeData.getStart());
    newRootRule(runtimeData.getRoot());
  }

  /**
   * The recovery table deals with tokens that can be used to recognize
   * syntax context and can recover from errors.
   */
  protected void generateTopRecoveryTable() {
    numberOfErrorTokens = 0;
    for (Terminal id : runtimeData.getTerminals()) {
      if (id instanceof ErrorToken) {
        numberOfErrorTokens++;
      }
    }
    environment.language.generateRecoveryTableHeader(numberOfErrorTokens);
  }

  /**
   * Assign ids, numbers, and print them.
   */
  protected void finalizeSymbols() {
    
    environment.reportWriter.subHeading("Terminal Symbols");
    environment.reportWriter.tableHead("symbols", 
        right("ID"), left("Name"), left("Full Name"), right("Value"),
        left("Err"), right("Refs"), right("Prec"),  left("Assc"), left("Type"));
  
    int recoveries = 0;
    int terminals = 0;
    for (Terminal id : runtimeData.getTerminals()) {
      // Look for the default token for a non assigned terminal symbol
      if (id.getToken() == -1) {
        int tok_num = 1;
        for (tok_num = Short.MAX_VALUE+1 ;; tok_num++) {
          Terminal cual = runtimeData.findTerminalByToken(tok_num);
          if (cual == null) {
            break;
          }
        }
        id.setToken(tok_num);
      }
      id.setId(terminals++);
      environment.reportWriter.tableRow(right(id.getId()), left(id.getName()), left(id.getFullName()), 
          right(id.getToken()), left(id instanceof ErrorToken ? "Yes" : "No "),
          right(id.getCount()), right(id.getPrecedence()), left(id.getAssociativity().displayName()),
          left(id.getType() != null ? id.getType().getName() : ""));
      if (id instanceof ErrorToken) {
        int recoveryToken = environment.isPacked() ? id.getToken() : terminals;
        ++recoveries;
        environment.language.generateErrorToken(recoveryToken, (ErrorToken) id, recoveries >= numberOfErrorTokens);
      }
    }
    
    environment.language.generateTokensHeader(terminals);
  
    int i = 1;
    for (Terminal id : runtimeData.getTerminals()) {
      environment.language.generateToken(id, i == terminals);
      i++;
    }
    i = 1;
    
    environment.reportWriter.tableEnd();
    
    environment.reportWriter.subHeading("Non Terminal Symbols");
    environment.reportWriter.tableHead("symbols", 
        right("ID"), left("Name"), left("FullName"), right("Refs"), left("Type"));
    
    int noterminals = 0;
    for (NonTerminal id : runtimeData.getNonTerminals()) {
      id.setId(noterminals + terminals);
      environment.reportWriter.tableRow(
          right(id.getId()), left(id.getName()), left(id.getFullName()),
          right(id.getCount()), left(id.getType() != null ? id.getType().getName() : ""));
      noterminals++;
      id.setFirst(null);
      id.setFollow(null);
    }
    
    environment.reportWriter.tableEnd();
    
    environment.reportWriter.subHeading("Types");
    environment.reportWriter.tableHead("symbols", left("Name"), left("Used By"));
  
    for (Type type : runtimeData.getTypes()) {
      String s = "<ul>";
      for (Symbol symbol : type.getUsedBy()) {
        s += "<li>" + symbol + "</li>\n";
      }
      s += "</ul>";
      environment.reportWriter.tableRow(left(type.getName()), left(s));
    }
    environment.reportWriter.tableEnd();
    
    environment.reportWriter.subHeading("Error Groups");
    environment.reportWriter.tableHead("symbols", left("Name"),
        left("Display Name"), left("Symbols"));
    
    for (TokenGroup errorGroup : runtimeData.getErrorGroups()) {
      String s = "<ul>";
      for (Terminal symbol : errorGroup.getTokens()) {
        s += "<li>" + symbol.toString() + "</li>";
      }
      s += "</ul>";
      environment.reportWriter.tableRow(left(errorGroup.getName()),
          left(errorGroup.getDisplayName()), left(s));
    }
    environment.reportWriter.tableEnd();

    environment.reportWriter.subHeading("Lexer Modes");
    environment.reportWriter.tableHead("lexermodes", left("Name"), left("Routine"));
    
    for (String lexerMode : environment.getLexerModes().keySet()) {
      environment.reportWriter.tableRow(left(lexerMode), left(environment.language.getLexerModeRoutine(lexerMode)));
    }
    environment.reportWriter.tableEnd();
}

  /**
   * set rule numbers and print
   */
  protected void finalizeRules() {
    environment.reportWriter.subHeading("Grammar");
    environment.reportWriter.tableHead("rules", right("Prec"), right("Rule"), left("Grammar"));
    int i = 0;
    for (Rule stx : runtimeData.getRules()) {
      stx.setRulenum(i);
      String s = stx.getLeftHand().getName() + " &rArr; ";
      for (RuleItem itm : stx.getItems()) {
        s += itm.getSymbol().getName() + ' ';
      }
      environment.reportWriter.tableRow(right(stx.getPrecedence()), right(i), left(s));
      i = i + 1;
    }
    environment.reportWriter.tableEnd();
  }

  /**
   * token definitions are declared as static or #define
   */
  protected void generateTokenDefinitions() {
    environment.language.generateTokenDefinitions();
  }

  /**
   * Locate a rule whose left hand if is the given id
   * @param id is the id of the non terminal on the left hand side
   * @return the rule, or null if not found
   */
  protected Rule locateRuleWithId(int id) {
    Rule rule = null;
    for (int i = 0; i < runtimeData.getRules().size(); i++) {
      rule = runtimeData.getRules().get(i);
      if (id == rule.getLeftHandId()) {
        break;
      }
    }
    return rule;
  }

  /**
   * @param rule is the rule's line number
   * @return the line number of a given rule
   */
  protected int lineNumber(Rule rule) {
    return rule != null ? rule.getLineNumber() - 1 : -1;
  }

  /**
   * Found a rule action.  Copy it to the output stream as-is
   * @param ruleNumber the rule index
   * @param elementCount the elements in the rule
   * @param nonTerminalName the left hand symbol of the rule
   * @return true if OK
   */
  protected boolean ruleAction(int ruleNumber, int elementCount, String nonTerminalName) {
    generateCodeGeneratorHeader();
    String ruleLabel = "";
    if (runtimeData.currentRuleItems != null) {
      for (RuleItem item : runtimeData.currentRuleItems) {
        ruleLabel = ruleLabel + " " + item.getSymbol().getName();
      }
    }
    generateCaseStatement(ruleNumber, "" + (ruleNumber+1) + ". " + nonTerminalName + " -> " + ruleLabel);
    
    while (currentChar == ' ') {
      getNextCharacter();
    }
    
    if (!environment.language.generateRuleCode(this, this, elementCount, nonTerminalName, runtimeData.columnNumber-2)) {
      return false;
    }
    
    generateCaseEnd();
    runtimeData.ruleActionCount++;
    
    return true;
  }

  protected boolean declareStart(String id) {
    if (runtimeData.getStart() != null) {
      environment.error(-1, "Distinguished symbol \'%s\' declared more than once.", runtimeData.getStart().getName());
      return false;
    }
    Terminal terminal = runtimeData.findTerminalByName(id);
    if (terminal == null) {
      NonTerminal nonTerminal = runtimeData.findNonTerminalByName(id);
      if (nonTerminal == null) {
        nonTerminal = new NonTerminal(id);
        runtimeData.getNonTerminals().add(nonTerminal);
      }
      nonTerminal.setCount(nonTerminal.getCount() - 1);
      runtimeData.setStart(nonTerminal);
    } else {
      environment.error(-1, "Distinguished symbol \'%s\' previously declared as token.", id);
      return false;
    }
    return true;
  }

  public abstract void execute() throws ParsingException;
  public abstract void dumpTokens() throws ParsingException; 

}
