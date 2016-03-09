package me.jaimegarza.syntax.test.java;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.jci.compilers.CompilationResult;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import me.jaimegarza.syntax.exception.AnalysisException;
import me.jaimegarza.syntax.exception.OutputException;
import me.jaimegarza.syntax.exception.ParsingException;
import me.jaimegarza.syntax.language.Language;
import me.jaimegarza.syntax.test.AbstractGenerationBase;

public class TestLalr extends AbstractGenerationBase {

  static final String expandedArgs[] = {
      // "-v",
      "--algorithm",
      "l",
      "--language",
      "java",
      "--packing",
      "tabular",
      //"-v",
      //"-g",
      "classpath:lalr.sy",
      "${file.language}"
  };

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
  public void test01WithExecute() throws ParsingException, AnalysisException, OutputException, 
                 MalformedURLException, ClassNotFoundException, InstantiationException, IllegalAccessException, 
                 NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
    generateLanguageFile(expandedArgs);

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
    Method getExpr = parser.getClass().getMethod("getExpr");
    setVerbose.invoke(parser, true);
    Object o = parse.invoke(parser);
    Assert.assertTrue(o instanceof Integer);
    int rc = (int) o;
    Assert.assertEquals(rc, 1, "Parse did not succeed");
    o = getExpr.invoke(parser);
    Assert.assertTrue(o instanceof String);
    String s = (String) o;
    Assert.assertEquals(s, "(abc)d", "string does not match");
  }


}
