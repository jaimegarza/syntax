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
package me.jaimegarza.syntax.algorithm;

import java.util.List;

import me.jaimegarza.syntax.model.parser.Dot;
import me.jaimegarza.syntax.model.parser.LookAhead;
import me.jaimegarza.syntax.model.parser.Rule;
import me.jaimegarza.syntax.model.parser.RuleItem;
import me.jaimegarza.syntax.model.parser.State;

public interface AlgorithmicSupport {
  /**
   * For algorithms that support look ahead per state, this method will
   * add all the lookaheads of the passed dots into the state's dots.
   * 
   * @param I is the array of states
   * @param state is the state number
   * @param dots is the list of marker {@link Dot}s
   * @return
   */
  boolean addLookaheadsToState(State I[], int state, List<Dot> dots);

  /**
   * Merge one marker dot's lookaheads into a destination.
   * 
   * @param marker is the source of the lookaheads
   * @param auxiliary is the second auxiliary dot
   */
  void mergeLookaheads(Dot marker, Dot auxiliary);

  /**
   * Simply put lookaheads of auxiliary into marker
   * @param marker is the receiving marker
   * @param auxiliary is the originating set of lookaheads
   */
  void addAllLookaheads(Dot marker, Dot auxiliary);

  /**
   * Compute the lookahead set of a rule on a specific item
   * 
   * @param rule is the rule where the lookahead begins
   * @param item is the dot position to start lookahead computations.
   * @return the new Lookahead, or null
   */
  LookAhead computeLookAhead(Rule rule, RuleItem item);
  
  /**
   * Print the lookaheads in a dot
   * @param dot is the dot to print
   */
  void printLookahead(Dot dot);
  
  /**
   * Checks a dot to see if it contains the symbol
   * 
   * @param dot the dot to check
   * @param terminalId is the symbol
   * @return true or false
   */
  boolean dotContains(Dot dot, int terminalId);
  
  /**
   * @return true if the algorithm needs multiple passes
   */
  boolean isMultiPass();

  /**
   * This dot is brand new, and used for a state.  Initialize it
   * @param dot is the dot to initialize
   */
  void initializeDot(Dot dot);

  /**
   * Does this algorithm support follow sets?
   * @return
   */
  boolean hasFollows();


}
