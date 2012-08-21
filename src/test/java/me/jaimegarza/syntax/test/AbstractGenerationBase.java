package me.jaimegarza.syntax.test;

import java.io.File;
import java.io.IOException;

import me.jaimegarza.syntax.AnalysisException;
import me.jaimegarza.syntax.OutputException;
import me.jaimegarza.syntax.ParsingException;
import me.jaimegarza.syntax.Syntax;
import me.jaimegarza.syntax.cli.Environment;

import org.apache.commons.jci.compilers.CompilationResult;
import org.apache.commons.jci.compilers.JavaCompiler;
import org.apache.commons.jci.compilers.JavaCompilerFactory;
import org.apache.commons.jci.readers.FileResourceReader;
import org.apache.commons.jci.stores.FileResourceStore;

public abstract class AbstractGenerationBase extends AbstractTestBase {
  
  protected void generateJavaFile(String[] args) throws ParsingException, AnalysisException, OutputException {
    Environment environment = new Environment("Syntax", setupFileArguments(args));
    Syntax syntax = new Syntax(environment);
    syntax.executeInternal();
    environment.release();
  }

  protected CompilationResult compileJavaFile(File source, File sourceDir) {
    JavaCompiler compiler = new JavaCompilerFactory().createCompiler("eclipse");
    String sources[] = new String[1];
    sources[0] = source.getName();
    CompilationResult result = compiler.compile(sources, new FileResourceReader(sourceDir), new FileResourceStore(sourceDir));
    return result;
  }

  public void setUp() throws IOException {
    System.out.println("filenames:");
    tmpLanguageFile = createTmpFile("TestParser.java", "output implementation file");
    tmpGrammarFile = createTmpFile("TestParser.txt", "grammar file");
    tmpIncludeFile = createTmpFile("TestParserIntf.java", "interface file");
  }

  public void tearDown() {
    // removeTmpFile(tmpLanguageFile);
    // removeTmpFile(tmpGrammarFile);
    // removeTmpFile(tmpIncludeFile);
  }


}
