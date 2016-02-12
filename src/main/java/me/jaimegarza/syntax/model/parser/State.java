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
package me.jaimegarza.syntax.model.parser;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * <i>~pojo class</i><br><br>
 *
 * A grammar G is defined as a set {<b>N</b>, <b>T</b>, <b>&Sigma;</b> &isin; <b>N</b>, <b>R</b>}
 * <ul>
 *   <li>n&#7522; &isin; <b>N</b> is the set of {@link NonTerminal} symbols
 *   <li>t&#7522; &isin; <b>T</b> is the set of {@link Terminal} symbols
 *   <li><b>&Sigma;</b> &isin; <b>N</b>, is the "distinguished symbol", or
 *    the root of the grammar
 *   <li><b>R</b> is the set of {@link Rule}s of the form<pre>
 *    &alpha; &rarr; &beta;</pre>
 *    where:<br>
 *    &alpha; &isin; <b>N</b><br>
 *    &beta; is a repetition of zero or many symbols s&#7522;; &isin; <b>N</b> &cup; <b>T</b>
 * </ul>
 * 
 * A parser P is defined as a set {<b>I</b>, <b>A</b>, <b>&Gamma;</b>, <b>E</b>}<pre>
 *   <b>P</b> = (<b>N</b> &cup; <b>T</b>) &times; <b>I</b></pre>
 * <ul>
 *   <li>i&#7522; &isin; <b>I</b> is the mapping set of states that identify 
 *   the FSA of the parser.<br><br>
 *   A state i&#7522; contains a set <b>A<super>'</super>
 *   </b> &sube; <b>A</b> and a set <b>&Gamma;<super>'</super></b> &sube; <b>&Gamma;</b>
 *   for its particular set of actions and gotos.  It also contains <b>e</b>&#7522; or 
 *   <i>&empty;</i> as the error that this state reports on an error.<br><br>
 *   <li>a&#7522; &isin; <b>A</b> is the subset &sube; <b>T</b> &times; <b>I</b> of 
 *   all a {@link Action}s or
 *   state transitions.<br>  An action a&#7522; is a pair (<b>t</b> &isin; <b>T</b>, 
 *   <b>i</b> &isin; <b>I</b>) that maps a state to another via terminal <b>t</b> on a 
 *   <b>Shift</b> action.<pre>
 *   s<sub>j</sub> = A(<b>t</b>, s<sub>i</sub>)</pre>
 *   <li>&gamma;&#7522; &isin; <b>&Gamma;</b> is the subset &sube; <b>N</b> &times; 
 *   <b>I</b> of {@link GoTo}s.<br>
 *   A GoTo &gamma;&#7522; is a pair (<b>n</b> &isin; <b>N</b>, <b>i</b> &isin; <b>I</b>) that
 *   maps a state to another state via non-terminal <b>n</b> on the final state of a 
 *   <b>Reduce</b> action.<pre>
 *   s<sub>j</sub> = &Gamma;(n, s<sub>i</sub>)</pre>
 *   <li>e&#7522; &isin; <b>E</b> is the set of errors that can be emitted by the grammar
 * </ul>
 * 
 * Additionally, the closure of a dot -- where &chi; &isin; <b>N</b> &cup; <b>T</b> and &beta; 
 * &isin; (<b>N</b> &cup;
 * <b>T</b>)* -- is defined as <pre>
 * <i>closure</i>(<b>n</b>&#7522; &rarr; &alpha; . &chi;&beta;) =
 * 
 *   1. &empty; if &chi; &isin; T or &not;&exist;&chi;&beta;; or
 *   2. {&chi; &rarr; . &delta;; &forall; &chi; &isin; N} | &chi; &rarr; <b>&delta;</b> &isin; <b>R</b>
 * </pre>
 * <p>
 * For generation purposes <b>i</b>&#7522; is augmented with<br>
 * <b>from</b> &isin; <b>I</b><br>
 * <b>symbol</b> &isin; <b>N</b> &cup; <b>T</b><br>
 * <b>default</b> &isin; <b>A</b><br>
 * <b>review</b> &isin; {<i>true</i>, <i>false</i>}<br>
 * <b>dot</b>&#7522; is a pair (<b>r</b>&#7522; &isin; <b>R</b>, <b>pos</b> &isin; 
 * <b>Int</b> &le; |<b>r</b>&#7522;|), where |<b>r</b>&#7522;| is the number of items in a rule <b>r</b>&#7522;
 * <p>
 * @author jaimegarza@gmail.com
 *
 */
public class State {
  /**
   * The identifier of the state
   */
  int id;
  /**
   * The from is the state i&#7522; &isin; <b>I</b> from which this state originates.<p>
   * A state transition is identified as a map i<sub>j</sub> = A(symbol, i&#7522;) U 
   * &Gamma;(symbol, i&#7522;).
   * In this context, i<sub>j</sub> is this state and from is i&#7522;
   */
  int from;
  /**
   * The symbol &isin; <b>T</b>is the grammar symbol that caused the transition
   * to this state.<p>
   * A state transition is identified as a map i<sub>j</sub> = A(symbol, i&#7522;) U 
   * &Gamma;(symbol, i&#7522;).
   * In this context, i<sub>j</sub> is this state
   */
  Symbol symbol;
  /**
   * A state does not itself contain a list of rules. It has a list
   * of {@link Dot}s, which themselves identify the rules uniquely. 
   */
  List<Dot> kernelDots = new LinkedList<Dot>();
  /**
   * I keep the closure separate, so that the equals method is done only on the
   * kernel dots.
   */
  List<Dot> closureDots = new LinkedList<Dot>();
  /**
   * 
   */
  List<Dot> effectiveDots = null;
  /**
   * The actual list of effective dots is dirty.  On new request, rebuild.
   */
  boolean dirtyList = true;
  /**
   * For packed grammars, it identifies the default action to take.
   * A state assigns a default action in order to eliminate sparse 
   * areas of no transition in favor of a single default transition.<p>
   * Not every state has a default, and in fact having a default may
   * lend to additional reduce actions before an error is detected.<p>
   */
  int defaultValue;
  /**
   * The list of {@link Action}s that the state can perform.  In other words
   * this is the list of state transitions on terminal symbols. See {@link #position}
   * for a graphical representation of the action table.
   */
  List<Action> actions;
  /**
   * The starting position into the packed action table <b>A</b><pre><code>
   * State Pos #           Actions
   * i<sub>0</sub>      0 4 --------> a<sub>0</sub>=(i<sub>0</sub>, t<sub>0</sub> &isin; T) 
   * i<sub>1</sub>      4 6----+     a<sub>1</sub>=(i<sub>1</sub>, t<sub>1</sub> &isin; T)
   * i<sub>2</sub>     10 2     |     a<sub>2</sub>=(i<sub>2</sub>, t<sub>2</sub> &isin; T)
   *  |              |     a<sub>3</sub>=(i<sub>3</sub>, t<sub>3</sub> &isin; T)
   *  |              +---> a<sub>4</sub>=(i<sub>4</sub>, t<sub>4</sub> &isin; T)
   *  |                    a<sub>5</sub>=(i<sub>5</sub>, t<sub>5</sub> &isin; T)
   *  |                    a<sub>6</sub>=(i<sub>6</sub>, t<sub>6</sub> &isin; T)
   *  |                    a<sub>7</sub>=(i<sub>7</sub>, t<sub>7</sub> &isin; T)
   *  |                    a<sub>8</sub>=(i<sub>8</sub>, t<sub>8</sub> &isin; T)
   *  |                    a<sub>9</sub>=(i<sub>9</sub>, t<sub>9</sub> &isin; T)
   *  +------------------> a<sub>10</sub>=(i<sub>10</sub>, t<sub>10</sub> &isin; T)
   *                       a<sub>11</sub>=(i<sub>11</sub>, t<sub>11</sub> &isin; T)
   *</code></pre> 
   */
  int position;
  /**
   * The index of the error message that gets produced by this state.
   */
  int message;
  /**
   * The row is the unpacked parsing table's row that identifies the state
   */
  private int row[];
  /**
   * The review flag is used as a volatile value during state generation to
   * aid in the process of processing further states.  When this flag is on
   * the state is reviewed in the loop for further states. When all states have
   * been reviewed, the process is complete.
   */
  boolean review;

  /**
   * Construct a state
   * @param id the id of the state
   * @param from the state that originated the new state
   * @param symbol the symbol that was followed to reach this state
   */
  public State(int id, int from, Symbol symbol) {
    //this();
    this.id = id;
    this.review = true;
    this.from = from;
    this.symbol = symbol;
    this.message = -1;
  }

  /**
   * Convenience method to get the id of the originating symbol
   * @return
   */
  public int getSymbolId() {
    return symbol == null ? -1 : symbol.getId();
  }

  /**
   * Combine the dots if necessary.
   */
  private void computeEffectiveDots() {
    if (effectiveDots == null) {
      effectiveDots = new LinkedList<Dot>();
    }
    if (dirtyList) {
      effectiveDots.clear();
      effectiveDots.addAll(kernelDots);
      effectiveDots.addAll(closureDots);
      dirtyList = false;
    }
  }
  
  /**
   * @return only the kernel dots
   */
  public List<Dot> getKernelDots() {
    return kernelDots;
  }

  /**
   * @return only the kernel dots
   */
  public List<Dot> getClosureDots() {
    return closureDots;
  }

  /**
   * @return only the kernel dots
   */
  public List<Dot> getAllDots() {
    computeEffectiveDots();
    return effectiveDots;
  }

  /**
   * @param i the index
   * @return a dot in with the given index
   */
  public Dot getDot(int i) {
    Dot dot = null;
    computeEffectiveDots();
    if (effectiveDots != null && i < effectiveDots.size()) {
      dot = effectiveDots.get(i);
    }
    return dot;
  }

  /**
   * Add a dot to the kernel dot set
   * @param dot the dot to be added
   */
  public void addKernelDot(Dot dot) {
    if (!kernelDots.contains(dot)) {
      kernelDots.add(dot);
      dirtyList = true;
    }
  }

  /**
   * Add a dot to the closure dot set
   * @param dot the dot to be added
   */
  public void addClosureDot(Dot dot) {
    if (!closureDots.contains(dot)) {
      closureDots.add(dot);
      dirtyList = true;
    }
  }

  /**
   * Merge all dots into this objects kernel dots.
   * @param dots is the list of dots to merge from
   */
  public void addAllKernelDots(List<Dot> dots) {
    for (Dot dot : dots) {
      addKernelDot(dot);
    }
  }

  /**
   * Merge all dots into this objects closure dots.
   * @param dots is the list of dots to merge from
   */
  public void addAllClosureDots(List<Dot> dots) {
    for (Dot dot : dots) {
      addClosureDot(dot);
    }
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
   * @return the review
   */
  public boolean isReview() {
    return review;
  }

  /**
   * @param review the review to set
   */
  public void setReview(boolean review) {
    this.review = review;
  }

  /**
   * @return the from
   */
  public int getFrom() {
    return from;
  }

  /**
   * @param from the from to set
   */
  public void setFrom(int from) {
    this.from = from;
  }

  /**
   * @return the symbol
   */
  public Symbol getSymbol() {
    return symbol;
  }

  /**
   * @param symbol the symbol to set
   */
  public void setSymbol(Symbol symbol) {
    this.symbol = symbol;
  }

  /**
   * @return the defaultValue
   */
  public int getDefaultValue() {
    return defaultValue;
  }

  /**
   * @param defaultValue the defaultValue to set
   */
  public void setDefaultValue(int defaultValue) {
    this.defaultValue = defaultValue;
  }

  /**
   * @return the actions
   */
  public List<Action> getActions() {
    return actions;
  }

  /**
   * @param actions the actions to set
   */
  public void setActions(List<Action> actions) {
    this.actions = actions;
  }

  /**
   * @return the position
   */
  public int getPosition() {
    return position;
  }

  /**
   * @param position the position to set
   */
  public void setPosition(int position) {
    this.position = position;
  }

  /**
   * @return the actionSize
   */
  public int getActionSize() {
    return actions == null ? 0 : actions.size();
  }

  /**
   * @return the message
   */
  public int getMessage() {
    return message;
  }

  /**
   * @param message the message to set
   */
  public void setMessage(int message) {
    this.message = message;
  }

  /**
   * @return the row
   */
  public int[] getRow() {
    return row;
  }

  /**
   * @param row the row to set.  The array
   * is copied into a new array since the original array's
   * content may change.
   */
  public void setRow(int[] row) {
    this.row = Arrays.copyOf(row, row.length);
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
      State s = (State) obj;
      // skipping review on purpose, since review is really volatile
      // skipping defaultValue, actions since they may be transitional (i.e.
      // about to be calculated and this is why I am comparing in the first
      // place)
      return from == s.from && symbol.equals(s.symbol) && kernelDots.equals(s.kernelDots); // && position == s.position;
    } catch (NullPointerException unused) {
      return false;
    } catch (ClassCastException unused) {
      return false;
    }
  }

  /**
   * Returns a representation of the state (all its rules and dots) with
   * additional information of what is this state's origin (what state this is 
   * coming from, and with what symbol)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String sym = symbol != null ? symbol.toString() : "(no symbol)";
    String s = "" + id + ". " + (from == -1 ? "starting state" : ("from " + from + " with " + sym)) + "\n";
    for (Dot dot : kernelDots) {
      s = s + dot.getRule().getLeftHand() + " -> " + dot.toString();
    }
    if (closureDots.size() > 0) {
      s = s + "...............................................................\n";
    }
    for (Dot dot : closureDots) {
      s = s + dot.getRule().getLeftHand() + " -> " + dot.toString();
    }
    return s;
  }

}
