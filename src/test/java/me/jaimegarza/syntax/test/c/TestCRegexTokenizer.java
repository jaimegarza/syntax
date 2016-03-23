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
package me.jaimegarza.syntax.test.c;

import java.io.IOException;

import me.jaimegarza.syntax.exception.AnalysisException;
import me.jaimegarza.syntax.exception.OutputException;
import me.jaimegarza.syntax.exception.ParsingException;
import me.jaimegarza.syntax.language.Language;
import me.jaimegarza.syntax.test.AbstractGenerationBase;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class TestCRegexTokenizer extends AbstractGenerationBase {

  static final String packedParserArgs[] = {
      // "-v",
      "--algorithm",
      "l",
      "--language",
      "c",
      "--packing",
      "tabular",
      "--noline",
      "classpath:c-regexp-tokenizer.sy",
      "${file.language}"
  };

  private static final String includeTabularParserChecks[] = {
      "#define PARSER_MODE",
      "#define TOKENS 10",
      "#define FINAL 13",
      "#define SYMBS 12",
      "#define NON_TERMINALS 3",
  };
  
  private static final String languageTabularParserChecks[] = {
    "Begin of Skeleton",
    "C Skeleton",
    "unsigned long int StxLexer()",
    "int StxCode",
    "End of parser"
  };

  private static final String grammarTabularParserChecks[] = {
      "Algorithm:.*LALR",
      "Language:.*C",
      "Packed\\?:.*.*false",
      "Tokens:.*10",
      "Non Terminals:.*3",
      "Types:.*0",
      "Rules:.*12",
      "Errors:.*3",
      "Actions:.*0",
      "Gotos:.*0",
      "Recoveries:.*0",
      "States:.*13",
  };

  @BeforeTest
  public void setUp() throws IOException {
    super.setUp(Language.C, "regexptokenizer");
  }


  @Override
  @AfterTest
  public void tearDown() {
    super.tearDown();
  }

  @Test
  public void test01GeneratePackedParser() throws ParsingException, AnalysisException, OutputException {
    generateLanguageFile(packedParserArgs);

    checkRegularExpressions(tmpIncludeFile, includeTabularParserChecks);
    checkRegularExpressions(tmpLanguageFile, languageTabularParserChecks);
    checkRegularExpressions(tmpGrammarFile, grammarTabularParserChecks);
  }

}
