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

public class TestPascalLexerModes extends AbstractGenerationBase {

  static final String args[] = {
    // "-v",
    "--algorithm",
    "l",
    "--language",
    "pascal",
    "--noline",
    "classpath:pascal-lexermodes.sy",
    "${file.language}",
    "${file.include}",
    "${file.grammar}",
};


  @BeforeTest
  public void setUp() throws IOException {
    super.setUp(Language.pascal, "lexermode");
  }

  @Override
  @AfterTest
  public void tearDown() {
    super.tearDown();
  }

  @Test
  public void testPascalLexerModes() throws ParsingException, AnalysisException, OutputException {
    generateLanguageFile(args);
  }

}
