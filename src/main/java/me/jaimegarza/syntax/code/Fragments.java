package me.jaimegarza.syntax.code;

import java.util.ListResourceBundle;

/**
 * This class is used as a default catch it all
 * resource bundle for all languages.<p>
 * 
 * It also provides the "name" of the resource
 * during initialization of the environment by
 * pulling its canonical class name.
 * 
 * Please be advised that {1} is interpreted to be the
 * indentation string required.  Always.  This is a
 * contract.
 *
 * @author jaimegarza@gmail.com
 *
 */
public class Fragments extends ListResourceBundle{
  
  public static final String HELLO = "hello";
  public static final String STXSTACK = "stxstack";
  public static final String GETC = "getc";
  public static final String LEXICAL_VALUE = "lexicalValue";
  public static final String CURRENT_CHAR = "currentChar";

  @Override
  protected Object[][] getContents() {
    return contents;
  }

  private Object[] contents [] = {
      {HELLO,"Hola Mundo"}, // keep, for unit testing
      {STXSTACK, "StxStack[{0}]"}
  };

}
