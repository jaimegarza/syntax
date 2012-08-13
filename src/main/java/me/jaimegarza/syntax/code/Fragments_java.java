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
      {Fragments.HELLO,"Jello Jorld"}, // keep, for unit testing
      {Fragments.STXSTACK, "stack[stackTop{0}]"},
      {Fragments.CURRENT_CHAR, "currentChar"},
      {Fragments.LEXICAL_VALUE, "lexicalValue"},
      {Fragments.CURRENT_CHAR, "currentChar = getNextChar(false)"},
  };

}
