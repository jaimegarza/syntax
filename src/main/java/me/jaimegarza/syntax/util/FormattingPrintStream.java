package me.jaimegarza.syntax.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import me.jaimegarza.syntax.cli.Environment;

public class FormattingPrintStream extends PrintStream {

  private Environment environment;
  
  public void printFragment(String key, Object... objects) {
    String fragment = environment.formatFragment(key, objects);
    print(fragment);
  }

  public void printlnFragment(String key, Object... objects) {
    String fragment = environment.formatFragment(key, objects);
    println(fragment);
  }

  public FormattingPrintStream(File file, String csn) throws FileNotFoundException, UnsupportedEncodingException {
    super(file, csn);
  }

  public FormattingPrintStream(File file) throws FileNotFoundException {
    super(file);
  }

  public FormattingPrintStream(OutputStream out, boolean autoFlush, String encoding) throws UnsupportedEncodingException {
    super(out, autoFlush, encoding);
  }

  public FormattingPrintStream(OutputStream out, boolean autoFlush) {
    super(out, autoFlush);
  }

  public FormattingPrintStream(OutputStream out) {
    super(out);
  }

  public FormattingPrintStream(String fileName, String csn) throws FileNotFoundException, UnsupportedEncodingException {
    super(fileName, csn);
  }

  public FormattingPrintStream(String fileName) throws FileNotFoundException {
    super(fileName);
  }

  /**
   * @return the environment
   */
  public Environment getEnvironment() {
    return environment;
  }

  /**
   * @param environment
   *          the environment to set
   */
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

}
