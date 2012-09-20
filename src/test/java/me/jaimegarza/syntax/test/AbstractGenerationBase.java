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
package me.jaimegarza.syntax.test;

import java.io.File;
import java.io.IOException;

import me.jaimegarza.syntax.AnalysisException;
import me.jaimegarza.syntax.OutputException;
import me.jaimegarza.syntax.ParsingException;
import me.jaimegarza.syntax.Syntax;
import me.jaimegarza.syntax.env.Environment;
import me.jaimegarza.syntax.language.Language;

import org.apache.commons.jci.compilers.CompilationResult;
import org.apache.commons.jci.compilers.JavaCompiler;
import org.apache.commons.jci.compilers.JavaCompilerFactory;
import org.apache.commons.jci.readers.FileResourceReader;
import org.apache.commons.jci.stores.FileResourceStore;

public abstract class AbstractGenerationBase extends AbstractTestBase {
  
  protected void generateLanguageFile(String[] args) throws ParsingException, AnalysisException, OutputException {
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

  public void setUp(Language language, String basename) throws IOException {
    System.out.println("filenames:");
    switch (language) {
      case java:
        tmpLanguageFile = createTmpFile(basename + ".java", "output implementation file");
        tmpGrammarFile = createTmpFile(basename + ".txt", "grammar file");
        tmpIncludeFile = createTmpFile(basename + "Intf.java", "interface file");
        break;
      case C:
        tmpLanguageFile = createTmpFile(basename + ".c", "output implementation file");
        tmpGrammarFile = createTmpFile(basename + ".txt", "grammar file");
        tmpIncludeFile = createTmpFile(basename + ".h", "include file");
        break;
      case pascal:
        tmpLanguageFile = createTmpFile(basename + ".pas", "output implementation file");
        tmpGrammarFile = createTmpFile(basename + ".txt", "grammar file");
        tmpIncludeFile = createTmpFile(basename + ".inc", "include file");
        break;
    }
  }

  public void tearDown() {
    // removeTmpFile(tmpLanguageFile);
    // removeTmpFile(tmpGrammarFile);
    // removeTmpFile(tmpIncludeFile);
  }


}
