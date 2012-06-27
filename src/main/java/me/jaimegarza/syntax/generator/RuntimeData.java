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
     * Neither the name of the <organization> nor the
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

import java.util.LinkedList;
import java.util.List;

import me.jaimegarza.syntax.definition.NonTerminal;
import me.jaimegarza.syntax.definition.Rule;
import me.jaimegarza.syntax.definition.State;
import me.jaimegarza.syntax.definition.Symbol;
import me.jaimegarza.syntax.definition.Terminal;
import me.jaimegarza.syntax.definition.Type;

public class RuntimeData {
  private NonTerminal root;
  private NonTerminal start;

  private List<Rule> rules = new LinkedList<Rule>();
  private List<NonTerminal> nonTerminals = new LinkedList<NonTerminal>();
  private List<Terminal> terminals = new LinkedList<Terminal>();
  private List<Type> types = new LinkedList<Type>();
  private State[] I;
  private int numberOfGoTos;
  private int numberOfActions;
  private int numberOfErrors;
  private int numberOfRecoveries;
  private List<String> errorMessages;
  private boolean finalActions;

  public int sourceLineNumber = 0;
  private boolean stackTypeDefined;

  public NonTerminal getRoot() {
    return root;
  }

  public void setRoot(NonTerminal root) {
    this.root = root;
  }

  public NonTerminal getStart() {
    return start;
  }

  public void setStart(NonTerminal start) {
    this.start = start;
  }

  public List<Rule> getRules() {
    return rules;
  }

  public List<NonTerminal> getNonTerminals() {
    return nonTerminals;
  }

  public List<Terminal> getTerminals() {
    return terminals;
  }

  public int getNumberOfGoTos() {
    return numberOfGoTos;
  }

  public void setNumberOfGoTos(int numberOfGoTos) {
    this.numberOfGoTos = numberOfGoTos;
  }

  public List<String> getErrorMessages() {
    return errorMessages;
  }

  public void setErrorMessages(List<String> errorMessages) {
    this.errorMessages = errorMessages;
  }

  public State[] getStates() {
    return I;
  }

  public void setStates(State[] I) {
    this.I = I;
  }

  public <T extends Symbol> T findSymbolByName(List<T> table, String name) {
    for (T symbol : table) {
      if (symbol.getName().equals(name)) {
        symbol.setCount(symbol.getCount() + 1);
        return symbol;
      }
    }
    return null;
  }

  public <T extends Symbol> T findSymbolByToken(List<T> table, int tokenId) {
    for (T symbol : table) {
      if (symbol.getToken() == tokenId) {
        return symbol;
      }
    }
    return null;
  }

  public <T extends Symbol> T findSymbolById(List<T> table, int id) {
    for (T symbol : table) {
      if (symbol.getId() == id) {
        return symbol;
      }
    }
    return null;
  }

  public Terminal findTerminalByName(String name) {
    return findSymbolByName(terminals, name);
  }

  public Terminal findTerminalByToken(int tokenId) {
    return findSymbolByToken(terminals, tokenId);
  }

  public Terminal findTerminalById(int id) {
    return findSymbolById(terminals, id);
  }

  public NonTerminal findNonTerminalByName(String name) {
    return findSymbolByName(nonTerminals, name);
  }

  public NonTerminal findNonTerminalByToken(int tokenId) {
    return findSymbolByToken(nonTerminals, tokenId);
  }

  public NonTerminal findNonTerminalById(int id) {
    return findSymbolById(nonTerminals, id);
  }

  public Type findType(String name) {
    for (Type t : types) {
      if (t.getName().equals(name)) {
        return t;
      }
    }
    return null;
  }

  public List<Type> getTypes() {
    return types;
  }

  public boolean isStackTypeDefined() {
    return stackTypeDefined;
  }

  public void setStackTypeDefined(boolean stackTypeDefined) {
    this.stackTypeDefined = stackTypeDefined;
  }

  public int getNumberOfActions() {
    return numberOfActions;
  }

  public void setNumberOfActions(int numberOfActions) {
    this.numberOfActions = numberOfActions;
  }

  public int getNumberOfErrors() {
    return numberOfErrors;
  }

  public void setNumberOfErrors(int numberOfErrors) {
    this.numberOfErrors = numberOfErrors;
  }

  public int getNumberOfRecoveries() {
    return numberOfRecoveries;
  }

  public void setNumberOfRecoveries(int numberOfRecoveries) {
    this.numberOfRecoveries = numberOfRecoveries;
  }

  public boolean hasFinalActions() {
    return finalActions;
  }

  public void setFinalActions(boolean finalActions) {
    this.finalActions = finalActions;
  }

}
