package me.jaimegarza.syntax.util;

import java.io.OutputStream;
import java.io.PrintStream;

import me.jaimegarza.syntax.cli.Environment;

public class FormattingPrintStream extends PrintStream {

  private Environment environment;
  
  public void printFragment(String key, Object... objects) {
    String fragment = environment.formatFragment(key, objects);
    print(fragment);
  }

  public void printlnFragment(String key, Object... objects) {
    printFragment(key, objects);
    println();
  }

  public void printIndentedFragment(String key, int indent, Object... objects) {
    Object newObjects[] = new Object[objects.length+1];
    for (int i = 0; i < objects.length; i++) {
      newObjects[i+1] = objects[i];
    }
    newObjects[0] = environment.language.indent(indent);
    printFragment(key, newObjects);
  }

  public void printlnIndentedFragment(String key, int indent, Object... objects) {
    printIndentedFragment(key, indent, objects);
    println();
  }

  /**
   * Construct a formatting print stream from an output stream
   * @param out the output stream
   */
  public FormattingPrintStream(OutputStream out) {
    super(out);
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
