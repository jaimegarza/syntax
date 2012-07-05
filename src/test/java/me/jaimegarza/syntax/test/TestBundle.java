package me.jaimegarza.syntax.test;

import java.util.Locale;
import java.util.ResourceBundle;

import org.testng.Assert;
import org.testng.annotations.Test;

import me.jaimegarza.syntax.code.Fragments;

public class TestBundle {
  
  /**
   * Make sure the c resource bundle works
   */
  @Test
  public void testCBundle() {
    Locale locale = new Locale("c");
    ResourceBundle bundle = ResourceBundle.getBundle(Fragments.class.getCanonicalName(), locale);
    String s = bundle.getString("hello");
    Assert.assertEquals(s, "Cello Corld");
  }

  /**
   * Make sure the java resource bundle works
   */
  @Test
  public void testJavaBundle() {
    Locale locale = new Locale("java");
    ResourceBundle bundle = ResourceBundle.getBundle(Fragments.class.getCanonicalName(), locale);
    String s = bundle.getString("hello");
    Assert.assertEquals(s, "Jello Jorld");
  }

  /**
   * Make sure the pascal resource bundle works
   */
  @Test
  public void testPascalBundle() {
    Locale locale = new Locale("pascal");
    ResourceBundle bundle = ResourceBundle.getBundle(Fragments.class.getCanonicalName(), locale);
    String s = bundle.getString("hello");
    Assert.assertEquals(s, "Pello Porld");
  }

}
