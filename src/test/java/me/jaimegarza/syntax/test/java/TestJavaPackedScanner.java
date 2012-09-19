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
package me.jaimegarza.syntax.test.java;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import me.jaimegarza.syntax.AnalysisException;
import me.jaimegarza.syntax.OutputException;
import me.jaimegarza.syntax.ParsingException;
import me.jaimegarza.syntax.language.Language;
import me.jaimegarza.syntax.test.AbstractGenerationBase;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.jci.compilers.CompilationResult;
import org.apache.commons.jci.problems.CompilationProblem;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class TestJavaPackedScanner extends AbstractGenerationBase {

  static final String packedArgs[] = {
      // "-v",
      "--algorithm",
      "l",
      "--language",
      "java",
      "--driver",
      "scanner",
      "classpath:java-test.sy",
      "${file.language}"
  };

  private static final String languagePackedChecks[] = {
      "int TOKENS=18",
      "int FINAL=34",
      "int SYMBS=19",
      "int ACTIONS=254",
      "int NON_TERMINALS=2",
      "Begin of Skeleton",
      "Java Skeleton"
  };

  private static final String grammarPackedChecks[] = {
      "Algorithm:.*LALR",
      "Language:.*java",
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

  protected static final int MAX_COMPILE_ERRORS = 10;

  @BeforeTest
  public void setUp() throws IOException {
    super.setUp(Language.java, "TestParser");
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
  public void test02Compile() throws ParsingException, AnalysisException, OutputException {
    generateLanguageFile(packedArgs);

    File source = new File(tmpLanguageFile);
    File sourceDir = source.getParentFile();
    CompilationResult result = compileJavaFile(source, sourceDir);

    if (result.getErrors().length > 0) {
      for (CompilationProblem problemo : result.getErrors()) {
        if (problemo.isError()) {
          System.err.println(problemo.toString());
        }
      }
      Assert.fail("Errors during the compilation of the output java file");
    }
  }

  @Test
  public void test03Runtime() throws ParsingException, AnalysisException, OutputException, MalformedURLException,
      ClassNotFoundException, InstantiationException, IllegalAccessException, SecurityException,
      NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
    generateLanguageFile(packedArgs);

    File source = new File(tmpLanguageFile);
    File sourceDir = source.getParentFile();
    CompilationResult result = compileJavaFile(source, sourceDir);
    Assert.assertEquals(result.getErrors().length, 0, "Syntax errors found trying to execute");

    URL urls[] = new URL[1];
    urls[0] = sourceDir.toURI().toURL();
    URLClassLoader classLoader = URLClassLoader.newInstance(urls, this.getClass().getClassLoader());
    String className = FilenameUtils.getBaseName(tmpLanguageFile);
    Class<?> clazz = classLoader.loadClass(className);
    String lexicalClassName = className + "$StackElement";
    Class<?> lexicalClazz = classLoader.loadClass(lexicalClassName);
    Object parser = clazz.newInstance();
    Method setVerbose = parser.getClass().getMethod("setVerbose", boolean.class);
    Method init = parser.getClass().getMethod("init");
    Method parse = parser.getClass().getMethod("parse", Integer.TYPE, lexicalClazz);
    Method getValidTokens = parser.getClass().getMethod("getValidTokens");
    Method getTotal = parser.getClass().getMethod("getTotal");
    setVerbose.invoke(parser, true);
    init.invoke(parser);
    for (Parameter p: parameters) {
      int [] tokens = (int[]) getValidTokens.invoke(parser);
      Assert.assertTrue(arrayContains(tokens, p.token), "Token " + p.token + " ain't there");
      Object lexicalValue = lexicalClazz.newInstance();
      Method setNumber = lexicalClazz.getMethod("setNumber", Integer.TYPE);
      setNumber.invoke(lexicalValue, p.value);
      parse.invoke(parser, p.token, lexicalValue);
      Object t = getTotal.invoke(parser);
      Assert.assertEquals(((Integer) t).intValue(), p.result, "Result is not " + p.result); 
    }
    Object o = getTotal.invoke(parser);
    Assert.assertTrue(o instanceof Integer);
    Integer i = (Integer) o;
    Assert.assertEquals((int) i, -17, "total does not match");
  }
  
  private static final int TOK_NUMBER = 32769;
  
  Parameter parameters[] = {
      new Parameter('(', 0, 0),
      new Parameter(TOK_NUMBER, 1, 1),
      new Parameter('+', 0, 0),
      new Parameter(TOK_NUMBER, 3, 3),
      new Parameter(')', 0, 0),
      new Parameter('*', 0, 0),
      new Parameter(TOK_NUMBER, 4, 4),
      new Parameter('/', 0, 0),
      new Parameter(TOK_NUMBER, 5, 5),
      new Parameter('+', 0, 0),
      new Parameter('-', 0, 0),
      new Parameter(TOK_NUMBER, 20, 20),
      new Parameter(0, 0, -17),
  };
  
  private boolean arrayContains(int array[], int value) {
    for (int x : array) {
      if (x == value) {
        return true;
      }
    }
    return false;
  }
  
  private static class Parameter {
    int token;
    int value;
    int result;
    
    Parameter(int token, int value, int result) {
      this.token = token;
      this.value = value;
      this.result = result;
    }
  }

}
