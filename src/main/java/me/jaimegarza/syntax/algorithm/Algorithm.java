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

/**
 * Describes the algorithm supported by the compiler-compiler.<p>
 * <ul>
 *   <li>SLR stands for Simple LR parser.  It is based on the computation of the
 *   sets of first and follow sets associated to non terminal symbols. It 
 *   constructs a compact number of states.</li>
 *   
 *   <li>LALR stands for Look Ahead LR parser.  Similar to the SLR parser except
 *   that it eliminates follow sets in favor of examining the reduced follow
 *   sets in a specific point of the rules where they appear.  Follow sets in
 *   SLR are computed for a non-terminal based on ALL the rules where they
 *   appear.</li>
 * </ul>
 * 
 * It is worth noting that SLR requires more reduce actions to produce an
 * error because of the generic follow sets.  Errors are only produced on Shift
 * attempts.
 * <p>
 * Also, of importance is that when packed parsers are produced, reduces are 
 * forced to make the packing smaller.  In that sense, a LALR parser can degenerate
 * into a similar packed SLR.  The major difference is that LALR can support
 * certain parsers by producing less conflict on the generation phase due to
 * the smaller assumptions of follow sets.
 * <p>
 * The less restrictive cannonical LR or simply LR parser allows for more context
 * free grammars, but I have not found much use to that so far in pragmatic
 * situations.  Implementing this algorithm would not be too hard.  It uses no
 * follow up sets at all.  Produced parsers are more extensive, errors are 
 * detected quicker.  However, non packed parsers are too sparse in memory, and
 * when packing occurs the tables degenerate as well.  LR parsers can indeed be
 * impractically too large.
 * <p>
 * 
 * TODO: P4-Add LR mode
 * TODO: P5-Add Honalee algorithm
 * 
 * @see {@link me.jaimegarza.syntax.generator.StructuralAnalyzer} for the creation
 *   of follow ups
 *
 * @author jaimegarza@gmail.com
 *
 */
public enum Algorithm {
  LALR, SLR
}
