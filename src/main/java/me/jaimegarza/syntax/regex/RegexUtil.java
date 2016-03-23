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
package me.jaimegarza.syntax.regex;

import me.jaimegarza.syntax.regex.node.AlternationNode;
import me.jaimegarza.syntax.regex.node.AnyCharNode;
import me.jaimegarza.syntax.regex.node.CharNode;
import me.jaimegarza.syntax.regex.node.CharRangeNode;
import me.jaimegarza.syntax.regex.node.ComplementNode;
import me.jaimegarza.syntax.regex.node.ConcatNode;
import me.jaimegarza.syntax.regex.node.IntersectionNode;
import me.jaimegarza.syntax.regex.node.LiteralNode;
import me.jaimegarza.syntax.regex.node.OptionalNode;
import me.jaimegarza.syntax.regex.node.RegexNode;
import me.jaimegarza.syntax.regex.node.RepeatMinimumNTimes;
import me.jaimegarza.syntax.regex.node.RepeatNtoMTimes;
import me.jaimegarza.syntax.regex.node.RepeatZeroOrManyNode;
import me.jaimegarza.syntax.regex.node.StringNode;

/**
 * Utilities to create the tree of regex nodes
 * @author jgarza
 *
 */
public class RegexUtil {

  /**
   * Generate a character node
   * @param c the character that represents the node
   * @return the new node
   */
  public static RegexNode character(char c) {
    return new CharNode(c);
  }

  /**
   * Generate a character range node
   * @param from is the beginning character that represents the node
   * @param to is the ending character that represents the node
   * @return the new node
   */
  public static RegexNode range(char from, char to) {
    return new CharRangeNode(from, to);
  }

  /**
   * Generate a union node
   * @param left is the left side of the union
   * @param right is the right side of the union
   * @return the new node
   */
  public static RegexNode alternation(RegexNode left, RegexNode right) {
    return new AlternationNode(left, right);
  }
  
  /**
   * Generate a concatenation node
   * @param left is the left side of the concatenation
   * @param right is the right side of the concatenation
   * @return the new node.  It can be of type union or string
   */
  public static RegexNode concatenation(RegexNode left, RegexNode right) {
    // Concatenate looking into literal string optimizations
    if (left instanceof LiteralNode && right instanceof LiteralNode) {
        return new StringNode(left.getString() + right.getString());
    } else {
      return new ConcatNode(left, right);
    }
  }

  /**
   * Generate an intersection node
   * @param left is the left side of the intersection
   * @param right is the right side of the intersection
   * @return the new node
   */
  public static RegexNode intersection(RegexNode left, RegexNode right) {
    return new IntersectionNode(left, right);
  }
  
  /**
   * Generate a complement node
   * @param exp is the expression to complement
   * @return the new node
   */
  public static RegexNode complement(RegexNode exp) {
    return new ComplementNode(exp);
  }
  
  /**
   * Generate an optional node
   * @param exp is the expression to make optional
   * @return the new node
   */
  public static RegexNode optional(RegexNode exp) {
    return new OptionalNode(exp);
  }
  
  /**
   * Generate a repeat node
   * @param exp is the expression to repeat
   * @return the new node
   */
  public static RegexNode repeat(RegexNode exp) {
    return new RepeatZeroOrManyNode(exp);
  }
  
  /**
   * Generate a repeat node
   * @param exp is the expression to repeat
   * @param min the minimum number of repetitions
   * @return the new node
   */
  public static RegexNode repeat(RegexNode exp, int min) {
    return new RepeatMinimumNTimes(exp, min);
  }
  
  /**
   * Generate a repeat node
   * @param exp is the expression to repeat
   * @param min the minimum number of repetitions
   * @param max the maximum number of repetitions
   * @return the new node
   */
  public static RegexNode repeat(RegexNode exp, int min, int max) {
    return new RepeatNtoMTimes(exp, min, max);
  }
  
  /**
   * Generate an any node
   * @return the new node
   */
  public static RegexNode any() {
    return new AnyCharNode();
  }
  
}
