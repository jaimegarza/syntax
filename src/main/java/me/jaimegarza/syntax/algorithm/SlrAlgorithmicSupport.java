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
package me.jaimegarza.syntax.algorithm;

import java.util.List;

import me.jaimegarza.syntax.cli.Environment;
import me.jaimegarza.syntax.definition.Dot;
import me.jaimegarza.syntax.definition.LookAhead;
import me.jaimegarza.syntax.definition.Rule;
import me.jaimegarza.syntax.definition.RuleItem;
import me.jaimegarza.syntax.definition.State;

public class SlrAlgorithmicSupport extends BaseAlgorithmicSupport{

  /**
   * Construct the supporting utility algorithm class for LALR
   * 
   * @param environment is the calling environment
   */
  public SlrAlgorithmicSupport(Environment environment) {
    super(environment);
  }

  /*
   * (non-Javadoc)
   * @see me.jaimegarza.syntax.algorithm.AlgorithmicSupport#mergeLookAheads(me.jaimegarza.syntax.definition.State[], int, java.util.List)
   */
  @Override
  public boolean addLookaheadsToState(State[] I, int state, List<Dot> dots) {
    return false;
  }

  /*
   * (non-Javadoc)
   * @see me.jaimegarza.syntax.algorithm.AlgorithmicSupport#mergeLookaheads(me.jaimegarza.syntax.definition.Dot, me.jaimegarza.syntax.definition.Dot)
   */
  @Override
  public void mergeLookaheads(Dot marker, Dot auxiliary) {
    
  }

  /*
   * (non-Javadoc)
   * @see me.jaimegarza.syntax.algorithm.AlgorithmicSupport#computeLookAhead(me.jaimegarza.syntax.definition.Rule, me.jaimegarza.syntax.definition.RuleItem)
   */
  @Override
  public LookAhead computeLookAhead(Rule rule, RuleItem item) {
    return null;
  }

  /*
   * (non-Javadoc)
   * @see me.jaimegarza.syntax.algorithm.AlgorithmicSupport#printLookahead(me.jaimegarza.syntax.definition.Dot)
   */
  @Override
  public void printLookahead(Dot dot) {
  }

  /*
   * (non-Javadoc)
   * @see me.jaimegarza.syntax.algorithm.AlgorithmicSupport#dotContains(me.jaimegarza.syntax.definition.Dot, int)
   */
  @Override
  public boolean dotContains(Dot dot, int terminalId) {
    return dot.getRule().getLeftHand().getFollow().contains(terminalId);
  }
  
  /*
   * (non-Javadoc)
   * @see me.jaimegarza.syntax.algorithm.AlgorithmicSupport#isMultiPass()
   */
  @Override
  public boolean isMultiPass() {
    return false;
  }

  /*
   * (non-Javadoc)
   * @see me.jaimegarza.syntax.algorithm.AlgorithmicSupport#initializeDot(me.jaimegarza.syntax.definition.Dot)
   */
  @Override
  public void initializeDot(Dot dot) {
  }

  /*
   * (non-Javadoc)
   * @see me.jaimegarza.syntax.algorithm.AlgorithmicSupport#addAllLookaheads(me.jaimegarza.syntax.definition.Dot, me.jaimegarza.syntax.definition.Dot)
   */
  @Override
  public void addAllLookaheads(Dot marker, Dot auxiliary) {
    
  }

  /*
   * (non-Javadoc)
   * @see me.jaimegarza.syntax.algorithm.AlgorithmicSupport#hasFollows()
   */
  @Override
  public boolean hasFollows() {
    return true;
  }

}
