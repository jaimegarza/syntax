Syntax 4.2
=======================================================================
For further documentation, please refer to <a href="https://jaimegarza.github.io/syntax">https://jaimegarza.github.io/syntax</a>
## Introduction
---

A computer program or application is written in a computer language like C, C++, Swift, Java, Pascal, Javascript, Scala and others. Languages are either compiled or interpreted. In a compiled program a translation is done from the source files written in the language into a binary form that can be understood by an execution engine, either as machine code, or instructions for a JVM or CLI. Other languages do not compile, but rather interpret as they find the code, like in the case of javascript, and no intermediate representation is needed.

Computer language compilers and interpreters are computer programs themselves. But, how do you create such programs? As many people know, trying to write a compiler by brute force is a long, tedious and error prone effort. Thankfuly there are standard techniques for creating these compilers. They are designed and modeled usually in a meta-language that describes the language structure. Then a **compiler-compiler** is used to produce the computer language compiler or interpreter.

Syntax is a **compiler-compiler**. It can be used to model languages and their different elements in a structured and rigorous format. Other compiler-compilers exist like yacc, bison, javacc, antlr, and many others. In addition, other tools had to be used for parts of the compilation to happen, like the use of lex and others. 

Syntax can be used to create either compilers or interpreters. Syntax was born in the 1980s as an alternative to yacc since yacc:

1. Had a great syntax and approach, yet its output was inescrutable.
2. It delegated the lexical analysis to an external tool.
3. Has been maintained, yet few improvements have been made.

Syntax is a compiler-compiler written as a tool for my students in college for the compiler construction course while I was a professor at De La Salle University in Mexico City. It is inspired by **yacc**. But why another yacc-like tool? For once, we have lexical analysis included in it with great output of results in HTML, including visual DFA graphs, different algorithms (LALR, SLR), support for C, Java, Pascal, Javascript, and extensible to others. Besides, I wanted to teach my students how the code for a compiler-compiler looked like, and yacc source is impenetrable and coarse. Now my students were able to browse compiler-compiler generation code in all its facets. And the code generated is clean and readable.

Just an additional note: you may ask yourself, if a compiler-compiler is used to generate a compiler, how is Syntax written? Is there a compiler-compiler-compiler of sorts? Well, the answer is no. The first version of Syntax was created in Apple Basic using standard SLR tecnhiques, but mostly by hand. Once I moved to a C codebase in the late eighties, I used syntax 1.x to "define" a structure for my C based version of syntax 2.0. So in a sense, Syntax 1 was the compiler compiler for the 2.0 version. And so forth. Today syntax is built with syntax, albeit one version back. The current version (4.2 at the time of writing) is built with Syntax 4.1.

I thought that having a yacc like syntax would help the introduction and teaching of LR parsers. Over time I have added:

1. The introduction of **lexical definitions** as part of the grammar (**regex** and **non-regex**). You can either code your parser with embedded code that scans the text, or use the provided built-in lexer generator using regex.
1. **Error messages** per %error definition. Unlike yacc, error messages can be provided in the language definition file and obtained as needed.
1. Output for:
    * **Java**
    * **C**
    * **Free Pascal/Delphi Pascal**
    * **Javascript, for Node.js and Nashorn on JVM**
    * Future: Other JVM languages
1. Translated to Java from its 1985 apple basic, and 2006 C codebase. The grammar definition is in Syntax format. Syntax is used to generate Syntax itself!
1. Support for lexic-driven parsers. Unlike standard parsers, lexic driven parsers allow you to move in the parse graph by keeping state. The lexer calls the parser, and when done with transitions, control is returned to the lexer who can wait for the next token.
1. Ability to compile with **LALR** (yacc) or **SLR**, more compact and simple, albeit a little more restrictive
1. Eject the output table in a **compressed mode** (yacc) or a **matrix**, for readability/teachability. Also, produce a rich **HTML report**.
1. Unlike yacc and bison, the output is **properly formated and readable!**

I am planning to add in future releases of the 4.0 codebase:

* LR algorithm (lower priority)
* Honalee algorithm (low priority)
* Support the concept of %external for sectional inclusions, encapsulation and reuse.
* Additional languages like Rust, GO, Swift, and others.
