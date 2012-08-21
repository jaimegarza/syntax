package me.jaimegarza.syntax.test;

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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.jci.compilers.CompilationResult;
import org.apache.commons.jci.problems.CompilationProblem;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class TestJavaExpanded extends AbstractGenerationBase {

  static final String expandedArgs[] = {
      // "-v",
      "--algorithm",
      "l",
      "--language",
      "java",
      "--packing",
      "tabular",
      "classpath:java-test.sy",
      "${file.language}"
  };

  private static final String languageExpandedChecks[] = {
      "int TOKENS=18",
      "int FINAL=34",
      "int SYMBS=19",
      "int ACTIONS=0",
      "int NON_TERMINALS=2",
      "Begin of Skeleton",
      "Java Skeleton Parser for packed tables"
  };

  private static final String grammarExpandedChecks[] = {
      "Algorithm:.*LALR",
      "Language:.*java",
      "Packed\\?:.*.*false",
      "Tokens:.*18",
      "Non Terminals:.*2",
      "Types:.*1",
      "Rules:.*17",
      "Errors:.*7",
      "Actions:.*0",
      "Gotos:.*0",
      "Recoveries:.*0",
      "States:.*34",
  };

  @Override
  @BeforeTest
  public void setUp() throws IOException {
    super.setUp();
  }

  @Override
  @AfterTest
  public void tearDown() {
    super.tearDown();
  }

  @Test
  public void test01Generate() throws ParsingException, AnalysisException, OutputException {
    generateJavaFile(expandedArgs);

    checkRegularExpressions(tmpLanguageFile, languageExpandedChecks);
    checkRegularExpressions(tmpGrammarFile, grammarExpandedChecks);
  }

  @Test
  public void test02Compile() throws ParsingException, AnalysisException, OutputException {
    generateJavaFile(expandedArgs);

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
    generateJavaFile(expandedArgs);

    File source = new File(tmpLanguageFile);
    File sourceDir = source.getParentFile();
    CompilationResult result = compileJavaFile(source, sourceDir);
    Assert.assertEquals(result.getErrors().length, 0, "Syntax errors found trying to execute");

    URL urls[] = new URL[1];
    urls[0] = sourceDir.toURI().toURL();
    URLClassLoader classLoader = URLClassLoader.newInstance(urls, this.getClass().getClassLoader());
    String className = FilenameUtils.getBaseName(tmpLanguageFile);
    Class<?> clazz = classLoader.loadClass(className);
    Object parser = clazz.newInstance();
    Method setVerbose = parser.getClass().getMethod("setVerbose", boolean.class);
    Method parse = parser.getClass().getMethod("parse");
    Method getTotal = parser.getClass().getMethod("getTotal");
    setVerbose.invoke(parser, true);
    parse.invoke(parser);
    Object o = getTotal.invoke(parser);
    Assert.assertTrue(o instanceof Integer);
    Integer i = (Integer) o;
    Assert.assertEquals((int) i, -17, "total does not match");
  }

}
