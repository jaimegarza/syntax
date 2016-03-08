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
package me.jaimegarza.syntax.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.testng.Assert;

public abstract class AbstractTestBase {

  protected String tmpLanguageFile;
  protected String tmpGrammarFile;
  protected String tmpIncludeFile;

  protected String createTmpFile(String filename, String name) throws IOException {
    //File file = File.createTempFile("syntax-", suffix);
    File file = new File("test-output/working-files/" + filename);
    String absolutePath = file.getAbsolutePath();
    absolutePath = absolutePath.replaceAll("\\\\", "\\/");
    System.out.println(name + " name:" + absolutePath);
    file.delete();
    return absolutePath;
  }

  protected String[] setupFileArguments(String[] args) {
    String[] result = new String[args.length];
    for (int i = 0; i < args.length; i++) {
      result[i] = args[i];
      result[i] = result[i].replaceAll("\\$\\{file\\.language\\}", tmpLanguageFile);
      result[i] = result[i].replaceAll("\\$\\{file\\.grammar\\}", tmpGrammarFile);
      result[i] = result[i].replaceAll("\\$\\{file\\.include\\}", tmpIncludeFile);
    }
    return result;
  }

  protected void checkRegularExpressions(String fileName, String[] regExpArray) {
    RegularExpression[] regularExpressions = constructRegularExpressions(regExpArray);
    File file = new File(fileName);
    try {
      Reader reader = new FileReader(file);
      BufferedReader bufferedReader = new BufferedReader(reader);
      String line = bufferedReader.readLine();
      while (line != null) {
        prodLine(line, regularExpressions);
        line = bufferedReader.readLine();
      }
      bufferedReader.close();
      failWithUncheckedExpressions(regularExpressions);
    } catch (FileNotFoundException e) {
      Assert.fail("File " + fileName + " cannot be opened since it cannot be found", e);

    } catch (IOException e) {
      Assert.fail("File " + fileName + "cannot be read", e);
    }
  }

  private void prodLine(String line, RegularExpression[] regularExpressions) {
    for (RegularExpression regularExpression : regularExpressions) {
      if (regularExpression.checked) {
        continue;
      }
      Matcher matcher = regularExpression.pattern.matcher(line);
      if (matcher.find()) {
        regularExpression.checked = true;
      }
    }
  }
  
  private void failWithUncheckedExpressions(RegularExpression[] regularExpressions) {
    StringBuilder builder = new StringBuilder();
    for (RegularExpression regularExpression : regularExpressions) {
      if (regularExpression.checked == false) {
        if (builder.length() > 0) {
          builder.append(", ");
        }
        builder.append('\"').append(regularExpression.pattern.pattern()).append('\"');
      }
    }
    if (builder.length() > 0) {
      Assert.fail("The following checks failed: " + builder.toString());
    }
  }

  private RegularExpression[] constructRegularExpressions(String[] regularExpressions) {
    RegularExpression[] regExps = new RegularExpression[regularExpressions.length];
    for (int i = 0; i < regularExpressions.length; i++) {
      regExps[i] = new RegularExpression();
      regExps[i].pattern = Pattern.compile(regularExpressions[i]);
      regExps[i].checked = false;
    }
    return regExps;
  }

  protected void removeTmpFile(String fileName) {
    File file = new File(fileName);
    if (file.exists()) {
      file.delete();
    }
  }

  private static class RegularExpression {
    Pattern pattern;
    boolean checked;
  }

}
