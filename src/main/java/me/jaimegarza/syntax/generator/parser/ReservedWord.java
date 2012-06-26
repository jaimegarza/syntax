package me.jaimegarza.syntax.generator.parser;

public class ReservedWord {
  String word;
  int token;
  
  public ReservedWord(String word, int token) {
    super();
    this.word = word;
    this.token = token;
  }

  public String getWord() {
    return word;
  }

  public void setWord(String word) {
    this.word = word;
  }

  public int getToken() {
    return token;
  }

  public void setToken(int token) {
    this.token = token;
  }

}
