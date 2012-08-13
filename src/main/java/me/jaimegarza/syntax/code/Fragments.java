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

  @Override
  protected Object[][] getContents() {
    return contents;
  }

  private Object[] contents [] = {
      {"hello","Hola Mundo"}, // keep, for unit testing
      {"stxstack", "StxStack[{0}]"}
  };

}
