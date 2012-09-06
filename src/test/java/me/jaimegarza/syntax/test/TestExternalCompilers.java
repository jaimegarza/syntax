package me.jaimegarza.syntax.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.testng.annotations.Test;

public class TestExternalCompilers extends AbstractTestBase {

  @Test
  public void generatePerlScript() throws IOException {
    String perlFileName = createTmpFile("externaltestsuite.pl", "test suite perl script");
    File outputFile = new File(perlFileName);
    File inputFile = new File("target/test-classes/externaltestsuite.pl");
    
    Reader reader = new FileReader(inputFile);
    Writer writer = new FileWriter(outputFile, false);
    BufferedReader bufferedReader = new BufferedReader(reader);
    BufferedWriter bufferedWriter = new BufferedWriter(writer);
    String line = bufferedReader.readLine();
    while (line != null) {
      bufferedWriter.write(line);
      bufferedWriter.newLine();
      bufferedWriter.flush();
      line = bufferedReader.readLine();
    }
    bufferedReader.close();
    bufferedWriter.close();
  }
}
