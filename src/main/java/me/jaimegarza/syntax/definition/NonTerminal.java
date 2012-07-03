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
package me.jaimegarza.syntax.definition;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * <i>~pojo class</i><br><br>
 * 
 * Represents a non terminal in the grammar (i.e. Expression, Statement).  Symbols have the 
 * following hierarchy:
 * <pre>
 *  -+ {@link Symbol}
 *   |
 *   +--+ {@link Terminal}     - Lexical Symbol (i.e. number, id '+')
 *   |  |
 *   |  +-- {@link ErrorToken} - Lexical Symbol declared with <b>%error</b>
 *   |
 *   +-- <b>NonTerminal</b>   - Syntactical symbol (i.e. Expression, Statement)
 *   </pre>
 *   
 *
 * @author jaimegarza@gmail.com
 *
 */
public class NonTerminal extends Symbol {
   /**
    * The <i>first</i> set for the non terminal.<p> 
    * The first set is the set of terminal
    * symbols that can appear as a starting symbol for a non-terminal.  They are 
    * computed by finding the rules where the non-terminal is on the left side, 
    * and then looking on the right side of these rules.<p>
    * 
    * Start with first symbol on the right side.<b>
    * <table border=1><tr><td>
    * <b>if the symbol on the right side of a rule where the non-terminal is the 
    * left hand side</b></td><td><b>resulting first action</b></td></tr>
    * <tr><td>terminal</td><td>right side symbol is added to the first set</td></tr>
    * <tr><td>non-terminal</td><td>the recursive computation of the first on the right 
    * side non-terminal symbol is merged into this first.<p>
    * If, in addition, the right side non-terminal symbol can result in a rule that
    * is empty (i.e. Expression -> <i>&lt;empty&gt;</i> ) then the second right hand symbol
    * is observed. And then the rules get applied to this new symbol as described in 
    * this table. This can iterate forward symbol after symbol until a right side 
    * non-terminal symbol has a non-empty rule or the end of the symbols in the rule 
    * is found.
    * </td></tr>
    * </table>
    * 
    * null means that the first has not been computed.
    */
  Set<Integer> first = null;
  /**
   * The <i>follow</i> set for the non terminal, only computed for SLR.<p> 
   * The follow set is the set of terminal symbols that can appear after the non terminal.
   * For example<pre>
   *   Expression -> Expression + Terminal</pre>
   * 
   * One can see that a plus sign can be after expression.  First looks at left hand sides.
   * Follow looks to the occurrence of the non terminal in the rule items.
   * 
   * With this in mind, the computation is similar to the computation of the first. 
   * See {@link #first} for additional explanations.<p>
   * 
   * null means that the first has not been computed.
   */
  Set<Integer> follow = null;
  /**
   * The goto set is a list of {@link Goto} objects that this object can reach.
   * 
   * A goto identifies a state where this element is transitioned.  It tells that
   * given an origin state, with this symbol a resulting transition is the destination
   * state.
   */
  List<GoTo> gotos = new LinkedList<GoTo>();

  /** 
   * Construct one non terminal
   * @param name is the name of the non terminal
   */
  public NonTerminal(String name) {
    super(name);
  }

  /**
   * Create a goto, and then add it to the goto list
   * @param origin the origin state
   * @param destination the destination state.
   */
  public void appendGoto(int origin, int destination) {
    GoTo goTo = new GoTo(origin, destination);
    addGoTo(goTo);
  }
  
  /**
   * compute the default goto for a non-terminal.  The default goto
   * is computed when packing a table, and it is the most likely destination
   * state (by virtue of counting)
   * 
   * @return the default destination state.
   */
  public int getDefaultGoto() {
    int defaultValue = 0, defaultCount = 0;
    int count;

    for (GoTo goTo : gotos) {
      if (goTo.getDestination() != defaultValue) {
        // count how many times it appears
        count = 0;
        for (GoTo counterGoto : gotos) {
          if (counterGoto.getDestination() == goTo.getDestination()) {
            count++;
          }
        }
        // is this more times than the current go to?
        if (count > defaultCount) {
          defaultCount = count;
          defaultValue = goTo.getDestination();
        }
      }
    }

    return defaultValue;
  }
  
  /**
   * Remove the goto identified by the destination
   * @param destinationState is the destination state
   * @return the number of elements in the goto table, for convenience.
   */
  public int removeGoto(int destinationState) {
    for (int i = 0; i < gotos.size(); ) {
      if (gotos.get(i).getDestination() == destinationState) {
        gotos.remove(i);
      } else {
        i++;
      }
    }
    return gotos.size();
  }

  /**
   * Add a goto to the goto set
   * @param goThere
   */
  public void addGoTo(GoTo goThere) {
    gotos.add(goThere);
  }

  /**
   * @return the gotos
   */
  public List<GoTo> getGotos() {
    return gotos;
  }

  /**
   * @return the first
   */
  public Set<Integer> getFirst() {
    return first;
  }

  /**
   * @param first the first to set
   */
  public void setFirst(Set<Integer> first) {
    this.first = first;
  }

  /**
   * @return the follow
   */
  public Set<Integer> getFollow() {
    return follow;
  }

  /**
   * @param follow the follow to set
   */
  public void setFollow(Set<Integer> follow) {
    this.follow = follow;
  }


}
