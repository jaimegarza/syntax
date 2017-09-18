/*
 Syntax is distributed under the Revised, or 3-clause BSD license
 ===============================================================================
 Copyright (c) 1985, 2012, 2016, Jaime Garza
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
     * Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.
     * Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.
     * Neither the name of the copyright holder nor the
       names of its contributors may be used to endorse or promote products
       derived from this software without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE FOR ANY
 DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ===============================================================================
*/
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
   * Make sure the java resource bundle works
   */
  @Test
  public void testJavascriptBundle() {
    Locale locale = new Locale("javascript");
    ResourceBundle bundle = ResourceBundle.getBundle(Fragments.class.getCanonicalName(), locale);
    String s = bundle.getString("hello");
    Assert.assertEquals(s, "Jsello Jsorld");
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
