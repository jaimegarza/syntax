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

/**
 * <i>~pojo class</i><br><br>
 *
 * A goto is associated to a non-terminal, and defines the transitions 
 * from a state to the destination state with the {@link NonTerminal}.
 * A non-terminal actually has a set of states.
 * 
 *  The purpose of the list of gotos in a non-terminal is for table generation
 *  purposes.  It can actually reflect the fact that the gotos reflect the
 *  packed nature of the parser.
 *  
 * @author jaimegarza@gmail.com
 *
 */
public class GoTo {

  /**
   * origin state of the goto
   */
  private int origin;
  /**
   * destination of the goto
   */
  private int destination;

  /**
   * Construct the GoTo
   * @param origin is the from state
   * @param destination is the to state
   */
  public GoTo(int origin, int destination) {
    super();
    this.origin = origin;
    this.destination = destination;
  }

  /**
   * @return the origin
   */
  public int getOrigin() {
    return origin;
  }

  /**
   * @param origin the origin to set
   */
  public void setOrigin(int origin) {
    this.origin = origin;
  }

  /**
   * @return the destination
   */
  public int getDestination() {
    return destination;
  }

  /**
   * @param destination the destination to set
   */
  public void setDestination(int destination) {
    this.destination = destination;
  }

}
