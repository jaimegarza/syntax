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
package me.jaimegarza.syntax.regex;

import me.jaimegarza.syntax.model.graph.Construct;
import me.jaimegarza.syntax.model.graph.Dfa;
import me.jaimegarza.syntax.model.graph.Nfa;
import me.jaimegarza.syntax.model.graph.Node;
import me.jaimegarza.syntax.model.graph.Transition;
import me.jaimegarza.syntax.model.graph.symbol.AnyCharacter;
import me.jaimegarza.syntax.model.graph.symbol.Character;
import me.jaimegarza.syntax.model.graph.symbol.CharacterClass;
import me.jaimegarza.syntax.model.graph.symbol.Epsilon;

public class NfaUtil {
  
  /**
   * constructs a nfa for a character
   * <pre><code>
   *      [n] -- c --&gt; [n+1]
   * </code></pre>
   * 
   * @param graph the graph to which the construct would be added
   * @param c is the character
   * @return the construct
   */
  public static Construct character(Nfa graph, char c) {
    Node start = graph.newNode();
    Node end = graph.newNode();
    new Transition(start, end, new Character(c));
    return new Construct(start, end);
  }

  /**
   * constructs a nfa for a character class
   * <pre><code>
   *      [n] -- char-class --&gt; [n+1]
   * </code></pre>
   * 
   * @param graph the graph to which the construct would be added
   * @param cc is the character class
   * @return the construct
   */
  public static Construct characterClass(Nfa graph, CharacterClass cc) {
    Node start = graph.newNode();
    Node end = graph.newNode();
    new Transition(start, end, cc);
    return new Construct(start, end);
  }
  
  /**
   * constructs a nfa for any character
   * <pre><code>
   *      [n] -- &lt;any&gt;--&gt; [n+1]
   * </code></pre>
   * 
   * @param graph the graph to which the construct would be added
   * @return the construct
   */
  public static Construct any(Nfa graph) {
    Node start = graph.newNode();
    Node end = graph.newNode();
    new Transition(start, end, new AnyCharacter());
    return new Construct(start, end);
  }
  
  /**
   * constructs a nfa for a concatenation (A B)
   * <pre><code>
   *      [construct-from] -- &epsilon; --&gt; [construct-to]
   * </code></pre>

   * @param graph the graph to which the construct would be added
   * @param from is the starting construct
   * @param to is the ending construct
   * @return the construct
   */
  public static Construct concatenate(Nfa graph, Construct from, Construct to) {
    new Transition(from.getEnd(), to.getStart(), new Epsilon());
    return new Construct(from.getStart(), to.getEnd());
  }
  
  /**
   * constructs a nfa for an alternation (A | B)
   * <pre><code>
   *      +--&gt; [construct] --+
   *      &epsilon;                  &epsilon;
   *      |                  |
   *      |                  V
   *  --&gt;[n]               [n+1] --&gt;
   *      |                  ^
   *      &epsilon;                  |
   *      |                  &epsilon;
   *      +--&gt; [construct] --+
   * </code></pre>
   *
   * @param graph the graph to which the construct would be added
   * @param a is the first alternating construct
   * @param b is the second alternating construct
   * @return the construct
   */
  public static Construct alternate(Nfa graph, Construct a, Construct b) {
    Node start = graph.newNode();
    Node end = graph.newNode();
    new Transition(start, a.getStart(), new Epsilon());
    new Transition(start, b.getStart(), new Epsilon());
    new Transition(a.getEnd(), end, new Epsilon());
    new Transition(b.getEnd(), end, new Epsilon());
    return new Construct(start, end);
  }

  /**
   * constructs a nfa for a zero to many (A*)
   * <pre><code>
   *      +------------&epsilon;------------+
   *      |                         |
   *      |                         V
   *  --&gt;[n]-&epsilon;-&gt; [construct] -&epsilon;-&gt; [n+1]--&gt;
   *             ^         |
   *             |         |         
   *             +----&epsilon;----+
   * </code></pre>
   * 
   * @param graph the graph to which the construct would be added
   * @param a is the repeating construct
   * @return the construct
   */
  public static Construct zeroOrMany(Nfa graph, Construct a) {
    Node start = graph.newNode();
    Node end = graph.newNode();
    new Transition(start, a.getStart(), new Epsilon());
    new Transition(a.getEnd(), end, new Epsilon());
    new Transition(start, end, new Epsilon());
    new Transition(a.getEnd(), a.getStart(), new Epsilon());
    return new Construct(start, end);
  }

  /**
   * constructs a nfa for an one to many (A+)
   * <pre><code>

   *  --&gt;[n]-&epsilon;-&gt; [construct] -&epsilon;-&gt; [n+1]--&gt;
   *             ^         |
   *             |         |         
   *             +----&epsilon;----+
   * </code></pre>
   * 
   * @param graph the graph to which the construct would be added
   * @param a is the repeating construct
   * @return the construct
   */
  public static Construct oneOrMany(Nfa graph, Construct a) {
    Node start = graph.newNode();
    Node end = graph.newNode();
    new Transition(start, a.getStart(), new Epsilon());
    new Transition(a.getEnd(), end, new Epsilon());
    new Transition(a.getEnd(), a.getStart(), new Epsilon());
    return new Construct(start, end);
  }

  /**
   * constructs a nfa for an option (A?)
   * <pre><code>
   *   [construct] 
   *   |         ^
   *   |         |         
   *   +----&epsilon;----+
   * </code></pre>
   * 
   * @param graph the graph to which the construct would be added
   * @param a is the optional construct
   * @return the construct
   */
  public static Construct optional(Nfa graph, Construct a) {
    new Transition(a.getStart(), a.getEnd(), new Epsilon());
    return a;
  }
  
  /**
   * Create a new dfa out of a nfa
   * @param graph is the graph
   * @param c the starting and ending nodes of the nfa
   * @return a new DFA
   */
  public static Dfa finalize(Nfa graph, Construct c) {
    Dfa dfa = new Dfa();
    c.getStart().setStarting(true);
    c.getEnd().setAccept(true);
    
    dfa.generateFromNfa(graph);
    /*System.out.println(graph);
    /System.out.println(dfa);
    for (DfaNode node : dfa.getNodes()) {
      List<Transition> code = node.getCodeTransitions();
      System.out.println(code);
    }*/
    return dfa;
  }
  
}
