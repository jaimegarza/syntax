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
package me.jaimegarza.syntax.test.javascript;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import javax.script.Invocable;
import javax.script.ScriptException;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import junit.framework.Assert;
import me.jaimegarza.syntax.exception.AnalysisException;
import me.jaimegarza.syntax.exception.OutputException;
import me.jaimegarza.syntax.exception.ParsingException;
import me.jaimegarza.syntax.language.Language;
import me.jaimegarza.syntax.test.AbstractGenerationBase;

public class TestJavascriptExpandedScanner extends AbstractGenerationBase {

  private static final int EOS = 0;
  private static final int TOK_AND = 256;
  private static final int TOK_OR = 257;
  private static final int TOK_NOT = 258;
  private static final int TOK_LE = 259;
  private static final int TOK_LT = 260;
  private static final int TOK_GE = 261;
  private static final int TOK_GT = 262;
  private static final int TOK_NE = 263;
  private static final int TOK_EQ = 264;
  private static final int TOK_NUMBER = 32769;

  // @formatter:off
  private static final int[] OPERATORS = new int[] { 
      EOS,
      ')', 
      '*', 
      '+', 
      '-', 
      '/', 
      TOK_AND, 
      TOK_OR, 
      TOK_LE, 
      TOK_LT,
      TOK_GE, 
      TOK_GT, 
      TOK_NE, 
      TOK_EQ
  };
  
  private static final int[] OPERANDS = new int[] { 
      '(', 
      '-', 
      TOK_NOT, 
      TOK_NUMBER
  };

  static final String packedArgs[] = {
      // "-v",
      "--algorithm", "slr", 
      "--language", "javascript", 
      "--packing", "tabular",
      "--driver", "scanner", 
      "classpath:javascript-test.sy",
      "${file.language}"
  };

  private static final String languagePackedChecks[] = { 
      "var TOKENS = 18", 
      "var FINAL = 34", 
      "var SYMBS = 19",
      "var NON_TERMINALS = 2", 
      "Javascript Skeleton"
  };

  private static final String grammarPackedChecks[] = { 
      "Algorithm.*SLR", 
      "Language.*javascript", 
      "Packed\\?.*.*false",
      "Tokens.*18", 
      "Non Terminals.*2", 
      "Types.*1", 
      "Rules.*17", 
      "Errors.*3", 
      "Recoveries.*0", 
      "States.*34",
  };
  // @formatter:on

  protected static final int MAX_COMPILE_ERRORS = 10;

  @BeforeTest
  public void setUp() throws IOException {
    super.setUp(Language.javascript, "JsTestParser");
  }

  @Override
  @AfterTest
  public void tearDown() {
    super.tearDown();
  }

  @Test
  public void test01Generate() throws ParsingException, AnalysisException, OutputException {
    generateLanguageFile(packedArgs);

    checkRegularExpressions(tmpLanguageFile, languagePackedChecks);
    checkRegularExpressions(tmpGrammarFile, grammarPackedChecks);
  }

  private void parseOneSymbol(Invocable script, Object token, int value, int status, int valid[])
      throws NoSuchMethodException, ScriptException {
    Assert.assertEquals(status, script.invokeFunction("parse", token, value));
    String available = (String) script.invokeFunction("getValidTokens");

    String validTokens[] = available.split(",");

    Assert.assertEquals("Incorrect number of tokens", valid.length, validTokens.length);
    for (int i = 0; i < validTokens.length; i++) {
      int t = Integer.valueOf(validTokens[i]);
      Assert.assertEquals("Tokens are not equal", t, valid[i]);
    }
  }

  @Test
  public void test02Runtime() throws ParsingException, AnalysisException, OutputException, FileNotFoundException,
      ScriptException, NoSuchMethodException {
    generateLanguageFile(packedArgs);

    File source = new File(tmpLanguageFile);
    Reader reader = new FileReader(source);
    BufferedReader bufferedReader = new BufferedReader(reader);
    Invocable script = getInvocableScript(bufferedReader);
    script.invokeFunction("init");
    
    parseOneSymbol(script, "(", 0, 2, OPERANDS);
    parseOneSymbol(script, TOK_NUMBER, 1, 2, OPERATORS);
    parseOneSymbol(script, "+", 0, 2, OPERANDS);
    parseOneSymbol(script, TOK_NUMBER, 3, 2, OPERATORS);
    parseOneSymbol(script, ")", 0, 2, OPERATORS);
    parseOneSymbol(script, "*", 0, 2, OPERANDS);
    parseOneSymbol(script, TOK_NUMBER, 4, 2, OPERATORS);
    parseOneSymbol(script, "/", 0, 2, OPERANDS);
    parseOneSymbol(script, TOK_NUMBER, 5, 2, OPERATORS);
    parseOneSymbol(script, "+", 0, 2, OPERANDS);
    parseOneSymbol(script, TOK_NUMBER, 20, 2, OPERATORS);

    Assert.assertEquals(1, script.invokeFunction("parse", 0, 0));
    Assert.assertEquals(23.2, script.invokeFunction("getResult"));
  }
}
