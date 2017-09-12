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
package me.jaimegarza.syntax.env;

import java.util.LinkedList;
import java.util.List;

import me.jaimegarza.syntax.model.graph.Dfa;
import me.jaimegarza.syntax.model.parser.NonTerminal;
import me.jaimegarza.syntax.model.parser.Rule;
import me.jaimegarza.syntax.model.parser.RuleItem;
import me.jaimegarza.syntax.model.parser.State;
import me.jaimegarza.syntax.model.parser.Symbol;
import me.jaimegarza.syntax.model.parser.Terminal;
import me.jaimegarza.syntax.model.parser.TokenGroup;
import me.jaimegarza.syntax.model.parser.Type;

/**
 * Class that is used to hold the state of the phases.
 *
 * @author jaimegarza@gmail.com
 *
 */
public class RuntimeData {
  
  private static final ThreadLocal<RuntimeData> currentRuntimeData = new ThreadLocal<>();
  
  /**
   * Starting symbol as computed.  The source of truth at the end of parsing
   */
  private NonTerminal root;
  /**
   * Starting symbol as given
   */
  private NonTerminal start;
  /**
   * List of all rules
   */
  private List<Rule> rules = new LinkedList<Rule>();
  /**
   * ID of the automatically generated rules
   */
  public int codeRule = 1;
  /**
   * List of non terminals
   */
  private List<NonTerminal> nonTerminals = new LinkedList<NonTerminal>();
  /**
   * list of terminal symbols, or tokens
   */
  private List<Terminal> terminals = new LinkedList<Terminal>();
  /**
   * List of types
   */
  private List<Type> types = new LinkedList<Type>();
  /**
   * List of error groups
   */
  private List<TokenGroup> errorGroups = new LinkedList<TokenGroup>();
  /**
   * The list of all DFAs for the regular expressions
   */
  private List<Dfa> regularExpressions = new LinkedList<Dfa>();
  /**
   * List of states
   */
  private State[] I;
  /**
   * How many gotos are there in the goto table?
   */
  private int numberOfGoTos;
  /**
   * How many actions are there in the action table?
   */
  private int numberOfActions;
  /**
   * How many errors are there in the error table?
   */
  private int numberOfErrors;
  /**
   * How many elements are there in the recovery table?
   */
  private int numberOfRecoveries;
  /**
   * List of error messages as computed
   */
  private List<String> errorMessages;
  /**
   * Does it have final actions?
   */
  private boolean finalActions;
  /**
   * Did the stack structure get defined?
   */
  private boolean stackTypeDefined;
  /**
   * Current source line number
   */
  public int lineNumber = 1;
  /**
   * Current column in the source
   */
  public int columnNumber;
  /**
   * The token's value
   */
  public String currentStringValue;
  /**
   * Temporary space for rule items before assignment
   */
  public List<RuleItem> currentRuleItems = null;
  /**
   * How many actions do I have?
   */
  public int ruleActionCount;
  
  /**
   * Initialize the current runtime data
   */
  public RuntimeData() {
    currentRuntimeData.set(this);
  }
  
  /**
   * Get the global runtime data for this thread
   */
  public static RuntimeData getCurrentRuntimeData() {
    return currentRuntimeData.get();
  }
  
  /**
   * Check to see if a rule identified by nonTerminalId is empty, i.e. it has no
   * rule items.
   * 
   * @param nonTerminalId is the id of the rule to check
   * @return true or false
   */
  public boolean symbolCanBeEmpty(int nonTerminalId) {
    for (Rule rule : rules) {
      if (rule.getLeftHand().getId() == nonTerminalId && rule.getItems().size() == 0) {
        return true;
      }
    }
    return false;
  }

  /**
   * Generic routine to find symbols
   * @param table the table to look into
   * @param name the name of the symbol
   * @param <T> is the type of the symbol
   * @return the symbol, or null if not found.
   */
  public <T extends Symbol> T findSymbolByName(List<T> table, String name) {
    for (T symbol : table) {
      if (symbol.getName().equals(name)) {
        symbol.setCount(symbol.getCount() + 1);
        return symbol;
      }
    }
    return null;
  }

  /**
   * Generic routine to find symbols
   * @param table the table to look into
   * @param tokenId the token of the symbol
   * @param <T> is the type of the symbol
   * @return the symbol, or null if not found.
   */
  public <T extends Symbol> T findSymbolByToken(List<T> table, int tokenId) {
    for (T symbol : table) {
      if (symbol.getToken() == tokenId) {
        return symbol;
      }
    }
    return null;
  }

  /**
   * Generic routine to find symbols
   * @param table the table to look into
   * @param id the id of the symbol
   * @param <T> is the type of the symbol
   * @return the symbol, or null if not found.
   */
  public <T extends Symbol> T findSymbolById(List<T> table, int id) {
    for (T symbol : table) {
      if (symbol.getId() == id) {
        return symbol;
      }
    }
    return null;
  }

  /**
   * Find one terminal by name
   * @param name the name of the symbol
   * @return the terminal, or null if not found
   */
  public Terminal findTerminalByName(String name) {
    return findSymbolByName(terminals, name);
  }

  /**
   * Find one terminal by token
   * @param tokenId the token of the symbol
   * @return the terminal, or null if not found
   */
  public Terminal findTerminalByToken(int tokenId) {
    return findSymbolByToken(terminals, tokenId);
  }

  /**
   * Find one terminal by id
   * @param id the id of the symbol
   * @return the terminal, or null if not found
   */
  public Terminal findTerminalById(int id) {
    return findSymbolById(terminals, id);
  }

  /**
   * Find one non terminal by name
   * @param name the name of the symbol
   * @return the terminal, or null if not found
   */
  public NonTerminal findNonTerminalByName(String name) {
    return findSymbolByName(nonTerminals, name);
  }

  /**
   * Find one non terminal by token
   * @param tokenId the token of the symbol
   * @return the terminal, or null if not found
   */
  public NonTerminal findNonTerminalByToken(int tokenId) {
    return findSymbolByToken(nonTerminals, tokenId);
  }

  /**
   * Find one non terminal by id
   * @param id the id of the symbol
   * @return the terminal, or null if not found
   */
  public NonTerminal findNonTerminalById(int id) {
    return findSymbolById(nonTerminals, id);
  }

  /**
   * Locate a type
   * @param name the name of the type
   * @return the type, or null if not found
   */
  public Type findType(String name) {
    if (name == null || name.length() == 0) {
      return Type.NullType;
    }
    
    for (Type t : types) {
      if (t.getName().equals(name)) {
        return t;
      }
    }
    return null;
  }

  /**
   * @return the root
   */
  public NonTerminal getRoot() {
    return root;
  }

  /**
   * @param root the root to set
   */
  public void setRoot(NonTerminal root) {
    this.root = root;
  }

  /**
   * @return the start
   */
  public NonTerminal getStart() {
    return start;
  }

  /**
   * @param start the start to set
   */
  public void setStart(NonTerminal start) {
    this.start = start;
  }

  /**
   * @return the rules
   */
  public List<Rule> getRules() {
    return rules;
  }

  /**
   * @param rules the rules to set
   */
  public void setRules(List<Rule> rules) {
    this.rules = rules;
  }

  /**
   * @return the nonTerminals
   */
  public List<NonTerminal> getNonTerminals() {
    return nonTerminals;
  }

  /**
   * @param nonTerminals the nonTerminals to set
   */
  public void setNonTerminals(List<NonTerminal> nonTerminals) {
    this.nonTerminals = nonTerminals;
  }

  /**
   * @return the terminals
   */
  public List<Terminal> getTerminals() {
    return terminals;
  }

  /**
   * @param terminals the terminals to set
   */
  public void setTerminals(List<Terminal> terminals) {
    this.terminals = terminals;
  }

  /**
   * @return the types
   */
  public List<Type> getTypes() {
    return types;
  }

  /**
   * @param types the types to set
   */
  public void setTypes(List<Type> types) {
    this.types = types;
  }

  /**
   * @return the i
   */
  public State[] getStates() {
    return I;
  }

  /**
   * @param i the i to set
   */
  public void setStates(State[] i) {
    I = i;
  }

  /**
   * @return the numberOfGoTos
   */
  public int getNumberOfGoTos() {
    return numberOfGoTos;
  }

  /**
   * @param numberOfGoTos the numberOfGoTos to set
   */
  public void setNumberOfGoTos(int numberOfGoTos) {
    this.numberOfGoTos = numberOfGoTos;
  }

  /**
   * @return the numberOfActions
   */
  public int getNumberOfActions() {
    return numberOfActions;
  }

  /**
   * @param numberOfActions the numberOfActions to set
   */
  public void setNumberOfActions(int numberOfActions) {
    this.numberOfActions = numberOfActions;
  }

  /**
   * @return the numberOfErrors
   */
  public int getNumberOfErrors() {
    return numberOfErrors;
  }

  /**
   * @param numberOfErrors the numberOfErrors to set
   */
  public void setNumberOfErrors(int numberOfErrors) {
    this.numberOfErrors = numberOfErrors;
  }

  /**
   * @return the numberOfRecoveries
   */
  public int getNumberOfRecoveries() {
    return numberOfRecoveries;
  }

  /**
   * @param numberOfRecoveries the numberOfRecoveries to set
   */
  public void setNumberOfRecoveries(int numberOfRecoveries) {
    this.numberOfRecoveries = numberOfRecoveries;
  }

  /**
   * @return the errorMessages
   */
  public List<String> getErrorMessages() {
    return errorMessages;
  }

  /**
   * @param errorMessages the errorMessages to set
   */
  public void setErrorMessages(List<String> errorMessages) {
    this.errorMessages = errorMessages;
  }

  /**
   * @return the finalActions
   */
  public boolean hasFinalActions() {
    return finalActions;
  }

  /**
   * @param finalActions the finalActions to set
   */
  public void setFinalActions(boolean finalActions) {
    this.finalActions = finalActions;
  }

  /**
   * @return the stackTypeDefined
   */
  public boolean isStackTypeDefined() {
    return stackTypeDefined;
  }

  /**
   * @param stackTypeDefined the stackTypeDefined to set
   */
  public void setStackTypeDefined(boolean stackTypeDefined) {
    this.stackTypeDefined = stackTypeDefined;
  }

  /**
   * @return the errorGroups
   */
  public List<TokenGroup> getErrorGroups() {
    return errorGroups;
  }

  /**
   * @return the regularExpressions
   */
  public List<Dfa> getRegularExpressions() {
    return regularExpressions;
  }

  /**
   * @param regularExpressions the regularExpressions to set
   */
  public void setRegularExpressions(List<Dfa> regularExpressions) {
    this.regularExpressions = regularExpressions;
  }

  /**
   * Add a regular expression DFA
   * @param dfa is the DFA to add
   */
  public void addRegularExpression(Dfa dfa) {
    regularExpressions.add(dfa);
  }

}
