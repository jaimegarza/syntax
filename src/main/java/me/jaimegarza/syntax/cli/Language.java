package me.jaimegarza.syntax.cli;

public enum Language {
  C(".c",".h"),
  java(".java", "Intf.java"),
  pascal(".pas", ".inc");
  
  String fileExt;
  String incExt;
  Language(String fileExt, String incExt) {
    this.fileExt = fileExt;
    this.incExt = incExt;
  }
  
  public String extension() {
    return this.fileExt;
  }
  
  public String includeExtension() {
    return this.incExt;
  }
}
