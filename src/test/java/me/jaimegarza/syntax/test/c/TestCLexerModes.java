package me.jaimegarza.syntax.test.c;

import java.io.IOException;

import me.jaimegarza.syntax.AnalysisException;
import me.jaimegarza.syntax.OutputException;
import me.jaimegarza.syntax.ParsingException;
import me.jaimegarza.syntax.language.Language;
import me.jaimegarza.syntax.test.AbstractGenerationBase;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class TestCLexerModes extends AbstractGenerationBase {

  static final String args[] = {
    // "-v",
    "--algorithm",
    "l",
    "--language",
    "c",
    "--noline",
    "classpath:c-lexermodes.sy",
    "${file.language}" 
};


  @BeforeTest
  public void setUp() throws IOException {
    super.setUp(Language.C, "lexermode");
  }

  @Override
  @AfterTest
  public void tearDown() {
    super.tearDown();
  }

  @Test
  public void testCLexerModes() throws ParsingException, AnalysisException, OutputException {
    generateLanguageFile(args);
  }

}
