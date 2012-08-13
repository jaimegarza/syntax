package me.jaimegarza.syntax;

import java.io.IOException;

/**
 * Routines implemented by the lexer.  The lexer is the unit that breaks
 * the input stream as a series of tokens.
 * 
 * @author jgarza
 *
 */
public interface Lexer {
  /**
   * Get one character.  Place it in RuntimeData.currentCharacter
   * @return the character
   * @throws IOException on input error
   */
  char getCharacter() throws IOException;

  /**
   * Reverse one character.  Place it in RuntimeData.currentCharacter
   * @return the character
   * @throws IOException on input error
   */
  void ungetCharacter(char c);
  
  /**
   * Standard tokens.  This routine deals with the parser type of symbols
   * @return next token
   * @throws IOException on input error
   */
  int getNormalSymbol() throws IOException;
  
  /**
   * Regex tokens.  They are surrounded by '/'
   * @return the next regex symbol
   * @throws IOException on input error
   */
  int getRegexSymbol() throws IOException;
}
