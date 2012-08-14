Syntax 4.0
=======================================================================
Syntax is a compiler compiler written as a tool for my students in 
college for the compiler construction course.

I thought that having a yacc like syntax would help the introduction of 
LR parsers.  Over time I have added:

- The introduction of lexical definitions as part of the grammar
  (non-regex)
- Error messages per %error definition
- Output for java
- Output for C
- Output for Delphi Pascal
- Translated to JAVA from its 1985 apple basic, and 2006 C codebase.
- Ability to compile with LALR (yacc) or SLR, more compact and simple,
  albeit a little more restrictive
- Eject the output table in a compressed mode (yacc) or a matrix, for 
  readability/teachability.
- Unlike yacc, the output is properly formated and readable!

I am planning to add in future releases of the 4.0 codebase:

- A regex token recognizer (perhaps with my own DFA)
- EBNF 
- javascript (I want to drive CodeMirror2 coloring editor to recognize
  my syntax.)
- Scala
- Error messages per rule, if possible.
- LR algorithm (lower priority)
- Honalee algorithm (low priority)
- Support the concept of %external for sectional inclusions, 
  encapsulation and reuse.
- Support for lexic-driven parsers (explained below)

-----------------------------------------------------------------------

LEXICAL-DRIVEN PARSERS
=======================================================================
Compiler driven parsers are the standard.  They get created, invoked 
with an input stream, which is then read one character at a time, 
checking for grammar compliance and generating code and other
structures as a result.

Lexical driven parsers differ in the fact that the input stream is 
discontinuous, usually user driven.  The parser gets created and then
waits for an input to come as a method call.  The parser will receive
the input and check it for correctness, causing shifts and reduces with
the given symbol.  When a state is reached that requires a new symbol,
the parser will stop, store its state, and return the method call.

As part of keeping state, a lexical driven parser will be able to
inform what possible symbols are possible in the next method call, thus
allowing user interfaces to enable/disable symbols.  The typical case
that I have encountered in the past was a calculator with operators,
numbers and parenthesis.  The buttons get enabled only on the presence
of valid symbol transitions on the current parser state.  Consider the
following calculator:

Given the
[1] [2] [3]  [+]
[4] [5] [6]  [-]
[7] [8] [9]  [*]
[(] [0] [)]  [/]
      [ = ]
       
Consider the following two states in an expression grammar:

State 1:
S ->  E .
E -> E . + E
E -> E . - E
E -> E . * E
E -> E . / E

State 2:
E -> (  E . )
E -> E . + E
E -> E . - E
E -> E . * E
E -> E . / E

State 1 can be seen as allowing an operator or the end of the input 
while state 2 shows that a parenthesis was used and thus a closing
parenthesis is valid, but not the end of the input.  Lets show this
in the calculator diagrams.  Invalid tokens in a state will  be shown
as periods.

For state 1:
 .   .   .   [+]
 .   .   .   [-]
 .   .   .   [*]
 .   .   .   [/]
      [ = ]
       
And for state 2:
 .   .   .   [+]
 .   .   .   [-]
 .   .   .   [*]
 .   .  [)]  [/]
       ...  
