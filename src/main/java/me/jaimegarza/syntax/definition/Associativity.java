package me.jaimegarza.syntax.definition;

public enum Associativity {
    NONE(""), 
    LEFT("LEF"), 
    RIGHT("RIG"), 
    BINARY("BIN");
    
    String theName;
    Associativity(String theName) {
      this.theName = theName;
    }
    
    public String displayName() {
      return this.theName;
    }
}
