package me.jaimegarza.syntax.code;

import java.util.ListResourceBundle;

/**
 * Fragments of code to be used for code generation
 * when the language is java
 *
 * Please be advised that {1} is interpreted to be the
 * indentation string required.  Always.  This is a
 * contract.
 *
 * @author jaimegarza@gmail.com
 *
 */
public class Fragments_java extends ListResourceBundle {

  @Override
  protected Object[][] getContents() {
    return contents;
  }
  
  /**
   * The fragments
   */
  Object[] contents [] = {
      {"hello","Jello Jorld"}, // keep, for unit testing
      {"stxstack", "stack[stackTop{0}]"},
      {"currentChar", "currentChar"},
      {"lexicalValue", "lexicalValue"},
      {"getc", "currentChar = getNextChar(false)"},
  };

}
