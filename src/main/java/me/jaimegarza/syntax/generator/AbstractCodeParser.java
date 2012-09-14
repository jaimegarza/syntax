/*
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
package me.jaimegarza.syntax.generator;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Stack;

import me.jaimegarza.syntax.EmbeddedCodeProcessor;
import me.jaimegarza.syntax.Lexer;
import me.jaimegarza.syntax.definition.Associativity;
import me.jaimegarza.syntax.definition.ErrorToken;
import me.jaimegarza.syntax.definition.NonTerminal;
import me.jaimegarza.syntax.definition.Rule;
import me.jaimegarza.syntax.definition.RuleItem;
import me.jaimegarza.syntax.definition.Symbol;
import me.jaimegarza.syntax.definition.Terminal;
import me.jaimegarza.syntax.definition.Type;
import me.jaimegarza.syntax.env.Environment;
import me.jaimegarza.syntax.util.PathUtils;

/**
 * This class contains the "non-parser" code, or supporting code for the syntax
 * parser.<p>
 * 
 * This is done so that the CodeParser can be generated from a syntaxt file
 * @author jgarza
 *
 */
public abstract class AbstractCodeParser extends AbstractPhase implements Lexer, EmbeddedCodeProcessor {
  protected static final String DISTINGUISHED_SYMBOL_NAME = "$start";

  protected Stack<Character> inputChars = new Stack<Character>();
  protected boolean bActionDone = false;
  protected int currentRuleIndex;
  protected Type currentType;
  protected int markers = 0;
  protected boolean isCurlyBrace;
  protected boolean isEqual;
  protected boolean isError;
  protected boolean isRegex;
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

  /**
   * Default Constructor
   * @param environment is the syntax environment
   */
  public AbstractCodeParser(Environment environment) {
    super(environment);

  }

  /**
   * Declare one non terminal in the symbol table
   * @param typeName the desired type
   * @param name the name of the symbol
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
   * Change the display name of a non terminal
   * @param name
   * @param fullName
   * @return
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
   * @return
   */
  protected boolean setLeftHandOfLastRule(String name) {
    if (runtimeData.findTerminalByName(name) != null) {
      environment.error(-1, "The token \'%s\' cannot appear to the right of a rule.", name);
      return false;
    }
    NonTerminal nonTerminal = runtimeData.findNonTerminalByName(name);
    if (nonTerminal == null) {
      runtimeData.getNonTerminals().add(new NonTerminal(name));
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
  protected char decodeOctal() throws IOException {
    int iCount = 3;
    char c2 = 0;
  
    while (iCount != 0) {
      c2 *= 8;
  
      if (runtimeData.currentCharacter >= '0' && runtimeData.currentCharacter <= '7') {
        c2 += runtimeData.currentCharacter - '0';
        getCharacter();
      } else if (runtimeData.currentCharacter == '\0') {
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
  protected char decodeControlChar() throws IOException {
    char c2;
    getCharacter();
  
    if (runtimeData.currentCharacter == '\0') {
      return '\0';
    }
  
    if (runtimeData.currentCharacter >= 'a' && runtimeData.currentCharacter <= 'z') {
      c2 = runtimeData.currentCharacter;
      getCharacter();
      return (char) (c2 - ('a' - 1));
    } else if (runtimeData.currentCharacter >= 'A' && runtimeData.currentCharacter <= 'Z') {
      c2 = runtimeData.currentCharacter;
      getCharacter();
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
  protected char decodeHex() throws IOException {
    int iCount = 2;
    char c2 = 0;
  
    getCharacter();
  
    while (iCount != 0) {
      c2 *= 16;
  
      if (runtimeData.currentCharacter >= '0' && runtimeData.currentCharacter <= '9') {
        c2 += runtimeData.currentCharacter - '0';
      } else if (runtimeData.currentCharacter >= 'a' && runtimeData.currentCharacter <= 'f') {
        c2 += 10 + (runtimeData.currentCharacter - 'a');
      } else if (runtimeData.currentCharacter >= 'A' && runtimeData.currentCharacter <= 'F') {
        c2 += 10 + (runtimeData.currentCharacter - 'A');
      } else if (runtimeData.currentCharacter == '\0') {
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
   * @throws IOException
   */
  protected char decodeEscape() throws IOException {
    char c2;
    switch (runtimeData.currentCharacter) {
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
        getCharacter();
        return 7;
      case 'b':
        getCharacter();
        return '\b';
      case 'c':
        getCharacter();
        return decodeControlChar();
      case 'e':
        getCharacter();
        return '\\';
      case 'f':
        getCharacter();
        return '\f';
      case 'n':
        getCharacter();
        return '\n';
      case 'r':
        getCharacter();
        return '\r';
      case 't':
        getCharacter();
        return '\t';
      case 'v':
        getCharacter();
        return 11;
      case 'x':
        getCharacter();
        return decodeHex();
      default:
        c2 = runtimeData.currentCharacter;
        getCharacter();
        return c2;
    }
  }

  public char getCharacter() throws IOException {
    if (inputChars.size() > 0) {
      runtimeData.currentCharacter = inputChars.pop();
      return runtimeData.currentCharacter;
    }
  
    // Get one char from stream
    runtimeData.currentCharacter = (char) environment.source.read();
    // EOF?
    if (runtimeData.currentCharacter == -1) {
      return 0;
    }
  
    // EOL?
    if (runtimeData.currentCharacter == '\n') {
      runtimeData.lineNumber++;
    }
  
    // CTRL-Z?  <-- suspect code
    if (runtimeData.currentCharacter == 26) {
      return 0;
    }
  
    return runtimeData.currentCharacter;
  }

  public void ungetCharacter(char c) {
    inputChars.push(c);
  }

  /****************************EMBEDDED CODE PROCESSOR **************************/
  public Type getTypeFromStream(Lexer lexer) throws IOException {
    Type type;
    String s2;
    s2 = runtimeData.currentStringValue;
    lexer.getNormalSymbol();
    type = runtimeData.findType(runtimeData.currentStringValue);
    if (type == null) {
      environment.error(-1, "Cannot find type '%s'.", runtimeData.currentStringValue);
      return null;
    }
    runtimeData.currentStringValue = s2;
    return type;
  }

  private RuleItem getCurrentRuleItem(int index) {
    RuleItem item= null;
    if (runtimeData.currentRuleItems != null && index < runtimeData.currentRuleItems.size()) {
      item = runtimeData.currentRuleItems.get(index);
    }
    return item;
  }

  public boolean generateDollarNumber(Lexer lexer, int elementCount, Type type, int sign) throws IOException {
    int num;
    int base;
    num = 0;
    if (runtimeData.currentCharacter == '0') {
      base = 8;
    } else {
      base = 10;
    }
    while (Character.isDigit(runtimeData.currentCharacter)) {
      num = num * base + runtimeData.currentCharacter - '0';
      lexer.getCharacter();
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
      }
      if (type != null) {
        environment.output.printf(".%s", type.getName());
      }
    }
    return true;
  }

  public boolean generateDollarDollar(Lexer lexer, int elementCount, String nonTerminalId, Type type) throws IOException {
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
      }
      if (type != null) {
        environment.output.printf(".%s", type.getName());
      }
    }
    lexer.getCharacter();
    return true;
  }

  public boolean generateConstant(Lexer lexer, char characterType) throws IOException {
    environment.output.print(runtimeData.currentCharacter);
    while ((lexer.getCharacter()) != characterType) {
      if (runtimeData.currentCharacter == '\0') {
        environment.error(-1, "Statement ' .. ' or \" .. \" not ended.");
        return false;
      }
      if (runtimeData.currentCharacter == '\n') {
        environment.error(-1, "End of line reached on string literal.");
        return false;
      }
      if (runtimeData.currentCharacter == '\\') {
        environment.output.print(runtimeData.currentCharacter);
        lexer.getCharacter();
      }
      environment.output.print(runtimeData.currentCharacter);
    }
    return true;
  }

  public boolean skipAndOutputCompositeComment(Lexer lexer, char secondaryCharacter, char characterToFind) throws IOException {
    boolean bBreak;
    
    environment.output.print(runtimeData.currentCharacter);
    lexer.getCharacter();
    bBreak = false;
    while (!bBreak) {
      if (runtimeData.currentCharacter == '\0') {
        environment.error(-1, "Unfinished comment.");
        return false;
      }
      while (runtimeData.currentCharacter == secondaryCharacter) {
        environment.output.print(runtimeData.currentCharacter);
        if ((lexer.getCharacter()) == characterToFind) {
          bBreak = true;
        }
      }
      environment.output.print(runtimeData.currentCharacter);
      lexer.getCharacter();
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
   */
  protected void generateCaseStatement(int ruleNumber, String comment) {
    environment.language.generateCaseStart(ruleNumber, Integer.toString(ruleNumber + 1), comment);
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
   */
  protected boolean generateLexerCode() throws IOException {
    if (tokenActionCount == 0) {
      environment.language.generateLexerHeader();
    }
    environment.language.emitLine(runtimeData.lineNumber + 1);
    indent(environment.output, environment.getIndent() + 1);
    environment.language.generateLexerCode(this);
    environment.output.println();
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
      environment.language.generateLexerFooter();
    }
  }

  /**
   * During a declaration, emit the accompanying code
   */
  protected boolean generateDeclaration() throws IOException {
    while (Character.isWhitespace(runtimeData.currentCharacter)) {
      getCharacter();
    }
    environment.language.emitLine(runtimeData.lineNumber);
    while (runtimeData.currentCharacter != '\0') {
      if (runtimeData.currentCharacter == '\\') {
        if ((getCharacter()) == '}') {
          getCharacter();
          return true;
        } else {
          environment.output.print('\\');
        }
      } else if (runtimeData.currentCharacter == '%') {
        if ((getCharacter()) == '}') {
          getCharacter();
          return true;
        } else {
          environment.output.print('%');
        }
      } else if (runtimeData.currentCharacter == '$') {
        getCharacter();
        if (runtimeData.currentCharacter == '$') {
          getCharacter();
          switch (runtimeData.currentCharacter) {
            case 'b':
              getCharacter();
              environment.output.print(PathUtils.getFileNameNoExtension(environment.getOutputFile().getAbsolutePath()));
              break;
            case 'n':
              getCharacter();
              environment.output.print(PathUtils.getFileName(environment.getOutputFile().getAbsolutePath()));
              break;
            case 'f':
              getCharacter();
              environment.output.print(environment.getOutputFile().getAbsolutePath());
              break;
            case 'e':
              getCharacter();
              environment.output.print(PathUtils.getFileExtension(environment.getOutputFile().getAbsolutePath()));
              break;
            case 'p':
              getCharacter();
              environment.output.print(PathUtils.getFilePath(environment.getOutputFile().getAbsolutePath()));
              break;
            default:
              environment.output.print("$$");
          }
        } else {
          environment.output.print('$');
        }
      }
      environment.output.print(runtimeData.currentCharacter);
      getCharacter();
    }
    environment.error(-1, "End of file before \'\\}\' or \'%%}\'.");
    return false;
  }

  /**
   * For yacc compatibility this is called the union, but it is
   * really a structure
   */
  protected boolean generateStructure() throws IOException {
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
    environment.report
        .printf("## Token                                    Name                                     Value Err  Refs  Prec Assc  Type\n");
    environment.report
        .printf("________________________________________________________________________________________________________________________\n");
  
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
      environment.report.printf("%2d %-40s %-40s %5d %s %5d %5d %-5s ", terminals, id.getId(), id.getName(), id
          .getToken(), id instanceof ErrorToken ? "Yes" : "No ", id.getCount(), id.getPrecedence(), id
          .getAssociativity().displayName());
      if (id.getType() != null) {
        environment.report.printf("%s", id.getType().getName());
      }
      environment.report.printf("\n");
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
    
    environment.report.printf("\n");
    environment.report
        .printf("## Non Terminals                            Name                                     Refs  Type\n");
    environment.report
        .printf("__________________________________________________________________________________________________\n");
  
    int noterminals = 0;
    for (NonTerminal id : runtimeData.getNonTerminals()) {
      environment.report.printf("%2d %-40s %-40s %-2d    ", noterminals + terminals, id.getId(), id.getName(),
          id.getCount());
      if (id.getType() != null) {
        environment.report.printf("%s", id.getType().getName());
      }
      environment.report.printf("\n");
      id.setId(noterminals + terminals);
      noterminals++;
      id.setFirst(null);
      id.setFollow(null);
    }
    
    environment.report.printf("\n");
    environment.report
        .printf("Types\n");
    environment.report
        .printf("_____________________________________________\n");
  
    for (Type type : runtimeData.getTypes()) {
      environment.report.println(type.toString());
    }
  }

  /**
   * set rule numbers and print
   */
  protected void finalizeRules() {
    environment.report.printf("\n");
    environment.report.printf("Prec Rule  Grammar\n");
    environment.report.printf("_____________________________________________________\n");
    int i = 0;
    for (Rule stx : runtimeData.getRules()) {
      stx.setRulenum(i);
      environment.report.printf("[%2d]  %3d. %s -> ", stx.getPrecedence(), i, stx.getLeftHand().getName());
      for (RuleItem itm : stx.getItems()) {
        environment.report.printf("%s ", itm.getSymbol().getName());
      }
      environment.report.printf("\n");
      i = i + 1;
    }
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
   * @return
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


}
