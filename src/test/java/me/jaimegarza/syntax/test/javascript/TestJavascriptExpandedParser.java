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
import java.io.StringReader;

import javax.script.ScriptException;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import me.jaimegarza.syntax.exception.AnalysisException;
import me.jaimegarza.syntax.exception.OutputException;
import me.jaimegarza.syntax.exception.ParsingException;
import me.jaimegarza.syntax.language.Language;
import me.jaimegarza.syntax.test.AbstractGenerationBase;

public class TestJavascriptExpandedParser extends AbstractGenerationBase {

  // @formatter:off
  static final String packedArgs[] = {
      // "-v",
      "--algorithm", "slr",
      "--packing", "tabular",
      "--language", "javascript",
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

  private static final String outputPackedChecks[] = {
      "The result is 23.2"
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

  @Test
  public void test02Runtime() throws ParsingException, AnalysisException, OutputException, FileNotFoundException,
      ScriptException, NoSuchMethodException {
    generateLanguageFile(packedArgs);

    File source = new File(tmpLanguageFile);
    Reader reader = new FileReader(source);
    BufferedReader bufferedReader = new BufferedReader(reader);
    String output = executeScript(bufferedReader);
    reader = new StringReader(output);
    checkRegularExpressions(reader, "javascript-output", outputPackedChecks);
  }

}
