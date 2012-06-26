package me.jaimegarza.syntax.generator;

import java.io.PrintStream;

import me.jaimegarza.syntax.cli.Environment;
import me.jaimegarza.syntax.definition.Rule;

public abstract class AbstractPhase {

  protected static Object resizeArray(Object oldArray, int newSize) {
    int oldSize = java.lang.reflect.Array.getLength(oldArray);
    Class<?> elementType = oldArray.getClass().getComponentType();
    Object newArray = java.lang.reflect.Array.newInstance(elementType, newSize);
    int preserveLength = Math.min(oldSize, newSize);
    if (preserveLength > 0)
      System.arraycopy(oldArray, 0, newArray, 0, preserveLength);
    return newArray;
  }

  protected Environment environment;
  protected RuntimeData runtimeData;

  protected boolean isEmpty(int k) {
  
    for (Rule rule : runtimeData.getRules()) {
      if (rule.getLeftHand().getId() == k && rule.getItems().size() == 0) {
        return true;
      }
    }
    return false;
  }

  protected void Tabea(PrintStream out, int n) {
    int i;
  
    for (i = 0; i < n; i++)
      switch (environment.getLanguage()) {
        case C:
          out.print("    ");
          break;
        case java:
        case pascal:
          out.print("  ");
          break;
      }
  }

  private boolean identifier(String s) {
    if (s.length() == 0 || !Character.isJavaIdentifierStart(s.charAt(0))) {
      return false;
    }
    for (int i = 1; i < s.length(); i++) {
      if (!Character.isJavaIdentifierPart(s.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  protected String variable(String s, int num) {
    if (s == null || s.length() == 0) {
      return s;
    }
  
    if (num == 0)
      return "EOS";
  
    if (identifier(s))
      return s;
  
    if (s.charAt(0) == '\\') {
      if (!s.equals("\\a"))
        return "BELL";
      if (!s.equals("\\b"))
        return "BACKSPACE";
      if (!s.equals("\\n"))
        return "EOL";
      if (!s.equals("\\t"))
        return "TAB";
      if (!s.equals("\\f"))
        return "FORM_FEED";
      if (!s.equals("\\r"))
        return "CR";
      if (s.startsWith("\\x")) {
        String t = "HEXAD_0x" + Integer.toHexString(num);
        return t;
      }
      if (s.startsWith("\\0")) {
        String t = "OCTAL_0" + Integer.toOctalString(num);
        return t;
      }
    }
    String patched = "";
    for (int i = 0; i < s.length(); i++) {
      if (Character.isJavaIdentifierPart(s.charAt(i)))
        patched += s.charAt(i);
      else
        patched += '_';
    }
    return patched;
  }

}
