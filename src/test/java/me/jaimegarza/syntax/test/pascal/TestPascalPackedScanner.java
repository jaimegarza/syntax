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
package me.jaimegarza.syntax.test.pascal;

import java.io.IOException;

import me.jaimegarza.syntax.AnalysisException;
import me.jaimegarza.syntax.OutputException;
import me.jaimegarza.syntax.ParsingException;
import me.jaimegarza.syntax.language.Language;
import me.jaimegarza.syntax.test.AbstractGenerationBase;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class TestPascalPackedScanner extends AbstractGenerationBase {

  static final String packedParserArguments[] = {
      //"-v",
      "--algorithm",
      "l",
      "--language",
      "pascal",
      "--driver",
      "scanner",
      "classpath:pascal-test.sy",
      "${file.language}",
      "${file.include}",
      "${file.grammar}",
  };

  private static final String languagePackedIncludeChecks[] = {
    "\\$DEFINE SCANNER_MODE",
    "TOKENS = 18",
    "FINAL = 34",
    "SYMBS = 19",
  };
  
  private static final String languagePackedParserChecks[] = {
    "Begin of Skeleton",
    "Pascal Skeleton",
    "StxLexer():longint",
    "StxCode.*BOOLEAN",
    "End of parser"
  };

  private static final String grammarPackedParserChecks[] = {
      "Algorithm:.*LALR",
      "Language:.*pascal",
      "Packed\\?:.*.*true",
      "Tokens:.*18",
      "Non Terminals:.*2",
      "Types:.*1",
      "Rules:.*17",
      "Errors:.*8",
      "Recoveries:.*0",
      "States:.*34",
  };

  @BeforeTest
  public void setUp() throws IOException {
    super.setUp(Language.pascal, "packedscanner");
  }


  @Override
  @AfterTest
  public void tearDown() {
    super.tearDown();
  }

  @Test
  public void test01GeneratePackedParser() throws ParsingException, AnalysisException, OutputException {
    generateLanguageFile(packedParserArguments);

    checkRegularExpressions(tmpIncludeFile, languagePackedIncludeChecks);
    checkRegularExpressions(tmpLanguageFile, languagePackedParserChecks);
    checkRegularExpressions(tmpGrammarFile, grammarPackedParserChecks);
  }

}
