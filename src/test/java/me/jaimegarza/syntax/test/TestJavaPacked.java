package me.jaimegarza.syntax.test;

import java.io.IOException;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import me.jaimegarza.syntax.Syntax;
import me.jaimegarza.syntax.cli.Environment;

public class TestJavaPacked extends AbstractTestBase{
  
  private static final String packedArgs[] = {
    "-v",
    "--algorithm",  "l",
    "--language", "java",
    "classpath:java-test.sy",
    "${file.language}",
    "${file.grammar}",
    "${file.include}"
  };
  
  private static final String languagePackedChecks[] = {
    "int TOKENS=18",
    "int FINAL=34",
    "int SYMBS=19",
    "int ACTIONS=57",
    "int NON_TERMINALS=2",
    "Begin of Skeleton",
    "Java Skeleton"
  };
  
  private static final String grammarPackedChecks[] = {
    
  };
  
  @BeforeTest
  public void setUp() throws IOException {
    System.out.println("filenames:");
    tmpLanguageFile = createTmpFile("Impl.java", "output implementation file");
    tmpGrammarFile = createTmpFile(".txt", "grammar file");
    tmpIncludeFile = createTmpFile(".java", "interface file");
  }

  @Test
  public void testPacked() {
    Environment environment = new Environment("Syntax", setupFileArguments(packedArgs));
    Syntax syntax = new Syntax(environment);
    syntax.execute();
    environment.release();
    checkRegularExpressions(tmpLanguageFile, languagePackedChecks);
    checkRegularExpressions(tmpGrammarFile, grammarPackedChecks);
    //removeTmpFile(tmpLanguageFile);
    //removeTmpFile(tmpGrammarFile);
    //removeTmpFile(tmpIncludeFile);
  }
}
