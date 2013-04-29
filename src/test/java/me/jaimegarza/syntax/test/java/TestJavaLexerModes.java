package me.jaimegarza.syntax.test.java;

import java.io.File;
import java.io.IOException;

import org.apache.commons.jci.compilers.CompilationResult;
import org.apache.commons.jci.problems.CompilationProblem;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import me.jaimegarza.syntax.AnalysisException;
import me.jaimegarza.syntax.OutputException;
import me.jaimegarza.syntax.ParsingException;
import me.jaimegarza.syntax.language.Language;
import me.jaimegarza.syntax.test.AbstractGenerationBase;

public class TestJavaLexerModes extends AbstractGenerationBase {

  static final String args[] = {
    "-v",
    "--algorithm",
    "l",
    "--language",
    "java",
    "--packing",
    "tabular",
    "--debug",
    "classpath:java-lexermodes.sy",
    "${file.language}" 
};


  @BeforeTest
  public void setUp() throws IOException {
    super.setUp(Language.java, "LexerMode");
  }

  @Override
  @AfterTest
  public void tearDown() {
    super.tearDown();
  }

  @Test
  public void testJavaLexerModes() throws ParsingException, AnalysisException, OutputException {
    generateLanguageFile(args);
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
}
