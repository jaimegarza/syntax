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

public class TestCPackedScanner extends AbstractGenerationBase {

  static final String packedParserArgs[] = {
      // "-v",
      "--algorithm",
      "slr",
      "--language",
      "c",
      "--noline",
      "--driver",
      "scanner",
      "classpath:c-test.sy",
      "${file.language}"
  };

  private static final String includePackedParserChecks[] = {
      "#define SCANNER_MODE",
      "#define TOKENS 18",
      "#define FINAL 34",
      "#define SYMBS 19",
      "#define ACTIONS 254",
      "#define NON_TERMINALS 2",
  };
  
  private static final String languagePackedParserChecks[] = {
    "Begin of Skeleton",
    "C Skeleton",
    "unsigned long int StxLexer()",
    "int StxCode",
    "End of parser"
  };

  private static final String grammarPackedParserChecks[] = {
      "Algorithm:.*SLR",
      "Language:.*C",
      "Packed\\?:.*.*true",
      "Tokens:.*18",
      "Non Terminals:.*2",
      "Types:.*1",
      "Rules:.*17",
      "Errors:.*8",
      "Actions:.*254",
      "Gotos:.*16",
      "Recoveries:.*0",
      "States:.*34",
  };

  @BeforeTest
  public void setUp() throws IOException {
    super.setUp(Language.C, "packedscanner");
  }


  @Override
  @AfterTest
  public void tearDown() {
    super.tearDown();
  }

  @Test
  public void test01GeneratePackedParser() throws ParsingException, AnalysisException, OutputException {
    generateLanguageFile(packedParserArgs);

    checkRegularExpressions(tmpIncludeFile, includePackedParserChecks);
    checkRegularExpressions(tmpLanguageFile, languagePackedParserChecks);
    checkRegularExpressions(tmpGrammarFile, grammarPackedParserChecks);
  }

}
