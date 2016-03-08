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
package me.jaimegarza.syntax.test.java;

import java.io.IOException;

import org.testng.annotations.Test;

import me.jaimegarza.syntax.exception.AnalysisException;
import me.jaimegarza.syntax.exception.OutputException;
import me.jaimegarza.syntax.exception.ParsingException;
import me.jaimegarza.syntax.language.Language;
import me.jaimegarza.syntax.test.AbstractGenerationBase;

public class TestFeatures extends AbstractGenerationBase {

  @Test
  public void testDeclareTokens() throws IOException, ParsingException, AnalysisException, OutputException {
    setUp(Language.java, "Tokens");
    generateLanguageFile(new String[] {
        "--algorithm",
        "l",
        "--language",
        "java",
        "--driver",
        "scanner",
        "--packing",
        "tabular",
        //"-v",
        //"-g",
        "classpath:tokens.sy",
        "${file.language}",
        "${file.include}",
        "${file.grammar}"
    });
    checkRegularExpressions(tmpGrammarFile, new String[] {
        " a .*One A.*256.*No.*N/A",
        " b .*b.*257.*No.*N/A",
        " c .*c.*32768.*No.*N/A",
        " d .*d.*32769.*No.*N/A",
        " e .*e.*300.*No.*LEF",
        " f .*f.*301.*No.*LEF",
        " g .*g.*302.*No.*RIG",
        " h .*h.*303.*No.*RIG",
        " i .*i.*304.*No.*BIN",
        " j .*j.*305.*No.*BIN",
        " k .*k.*32770.*Yes.*N/A",
        " Expr.*an expression"
    });
    tearDown();
  }

  @Test
  public void testTypes() throws IOException, ParsingException, AnalysisException, OutputException {
    setUp(Language.java, "Types");
    generateLanguageFile(new String[] {
        "--algorithm",
        "l",
        "--language",
        "java",
        "--driver",
        "scanner",
        "--packing",
        "tabular",
        //"-v",
        //"-g",
        "classpath:types.sy",
        "${file.language}",
        "${file.include}",
        "${file.grammar}"
    });
    checkRegularExpressions(tmpGrammarFile, new String[] {
        " a .*a.*32768.*N/A.*type1",
        " b .*b.*32769.*N/A.*type2",
        " c .*c.*32770.*N/A.*type1",
        "Expr.*type1",
        "Term.*type2",
    });
    checkRegularExpressions(tmpLanguageFile, new String[] {
        ".*stackTop.\\.type1.*=.*stackTop.\\.type1",
        ".*stackTop-2.\\.type1.*stackTop-2.\\.type1.*stackTop-1.\\.type1.*stackTop.\\.type2",
        ".*stackTop-2.\\.type1.*stackTop-2.\\.type2.*stackTop-1.\\.type2.*stackTop.\\.type2",
        ".*stackTop.\\.type2.*stackTop.\\.type1",
    });
    tearDown();
  }
  
  @Test
  public void testErrors() throws IOException, ParsingException, AnalysisException, OutputException {
    setUp(Language.java, "Errors");
    generateLanguageFile(new String[] {
        "--algorithm",
        "l",
        "--language",
        "java",
        "--packing",
        "tabular",
        "-v",
        "-g",
        "classpath:errors.sy",
        "${file.language}",
        "${file.include}",
        "${file.grammar}"
    });
    /*checkRegularExpressions(tmpGrammarFile, new String[] {
        " a .*a.*32768.*N/A.*type1",
        " b .*b.*32769.*N/A.*type2",
        " c .*c.*32770.*N/A.*type1",
        "Expr.*type1",
        "Term.*type2",
    });*/
    checkRegularExpressions(tmpLanguageFile, new String[] {
        "Expecting an expression",
        "arithmetic operator, logic operator or relational operator expected",
        "arithmetic operator, logic operator, relational operator or right parenthesis expected",
    });
    tearDown();
  }
  
  @Test
  public void testCodeLexer() throws IOException, ParsingException, AnalysisException, OutputException {
    setUp(Language.java, "CodeLexer");
    generateLanguageFile(new String[] {
        "--algorithm",
        "l",
        "--language",
        "java",
        "--driver",
        "scanner",
        "--packing",
        "tabular",
        //"-v",
        //"-g",
        "classpath:codelexer.sy",
        "${file.language}",
        "${file.include}",
        "${file.grammar}"
    });
    checkRegularExpressions(tmpLanguageFile, new String[] {
        "public class Lexer \\{",
        "int parserElement\\(",
        "while \\(currentChar == ' '\\) currentChar = getNextChar\\(false\\)",
        "lexicalValue.id",
        "return '!'",
        "private static final int identifier=32768"
    });
    tearDown();
  }
}
