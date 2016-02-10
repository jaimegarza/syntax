package me.jaimegarza.syntax.util;

public class ArrayUtils {
  /**
   * Given an array, make sure it is the right size, expanding
   * if needed.
   * 
   * @param oldArray is the array as it exists
   * @param newSize defines the size that is needed
   * @return a new array, or the same oldArray if the size is OK
   */
  public static Object resizeArray(Object oldArray, int newSize) {
    int oldSize = java.lang.reflect.Array.getLength(oldArray);
    Class<?> elementType = oldArray.getClass().getComponentType();
    Object newArray = java.lang.reflect.Array.newInstance(elementType, newSize);
    int preserveLength = Math.min(oldSize, newSize);
    if (preserveLength > 0) {
      System.arraycopy(oldArray, 0, newArray, 0, preserveLength);
    }
    return newArray;
  }


}
