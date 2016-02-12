/*
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
     * Neither the name of Jaime Garza nor the
       names of its contributors may be used to endorse or promote products
       derived from this software without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ===============================================================================
*/
package me.jaimegarza.syntax.util;

/**
 * Utilities to manage filenames in the system.
 * 
 * Portions copy/pasted from commons-io, 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * @author jgarza
 *
 */
public class PathUtils {
  private static final char FORWARD_SLASH = '/';
  private static final char BACKWARDS_SLASH = '\\';
  private static final char DOT = '.';

  /**
   * Locate where the path ends<p>
   * So to say:<p>
   * <ul>
   *   <li><em>c:\path.name\filename</em>: index of \ before filename
   *   <li><em>/path.name/filename</em>: index of / before filename
   *   <li><em>c:\path/filename</em>: -index of / before filename
   *   <li><em>c:/path\filename.ext</em>: index of \ before filename
   * </ul>
   * @param filename is the filename to examine
   * @return the index of the last separator, or -1
   */
  public static int filenameSeparatorIndex(String filename) {
      if (filename == null) {
          return -1;
      }
      int forwardSlash = filename.lastIndexOf(FORWARD_SLASH);
      int backSlash = filename.lastIndexOf(BACKWARDS_SLASH);
      
      if (forwardSlash > backSlash) {
        // there is a forward slash after a backward slash, or no backslash at all
        return forwardSlash;
      } else {
        // either both were -1, or there is an actual backslash
        return backSlash;
      }
  }

  /**
   * Locate the index of the file's extension.
   * 
   * I locate the last dot and compare to see if it is in the filename
   * portion, or the path portion.<P>
   * 
   * So to say:<p>
   * <ul>
   *   <li><em>c:\path.name\filename</em>: -1
   *   <li><em>/path.name/filename</em>: -1
   *   <li><em>c:\path\filename</em>: -1
   *   <li><em>/path/filename</em>: -1
   *   <li><em>c:\path\filename.ext</em>: dot before ext
   *   <li><em>/path/filename.ext</em>: dot before ext
   * </ul>
   *
   * @param filename is the filename to examine
   * @return the index of the dot in the filename, or -1
   */
  public static int extensionSeparatorIndex(String filename) {
      if (filename == null) {
          return -1;
      }
      int extension = filename.lastIndexOf(DOT);
      int lastSeparator = filenameSeparatorIndex(filename);
      if (lastSeparator >= extension) {
        // the last dot is in the path
        return -1; // no extension
      } else {
        // good extension
        return extension;
      }
  }

  /**
   * Get the filename (with extension of a file path).  We will guide ourselves
   * by the position of the slashes.
   * 
   * @param filePath is the complete or partial filename
   * @return the filename portion of the path, including extension
   */
  public static String getFileName(String filePath) {
      if (filePath == null) {
          return null;
      }
      
      int slash = filenameSeparatorIndex(filePath);
      if (slash > 0) {
        // we have a slash, either forward or backward
        return filePath.substring(slash+1);
      } else {
        // the whole thing is the name
        return filePath;
      }
  }

  /**
   * Get the filename, and then strip the extension
   * @param filePath is the filename to examine
   * @return the filename minus the extension
   */
  public static String getFileNameNoExtension(String filePath) {
      String fileName = getFileName(filePath); // can be null, careful
      int dot = extensionSeparatorIndex(fileName);
      if (dot == -1) { // not found (or null)
          return fileName;
      } else {
          return fileName.substring(0, dot); // not including dot
      }
  }

  /**
   * Get only the file extension given a path
   * @param filePath is the path to examine
   * @return the extension if existing
   */
  public static String getFileExtension(String filePath) {
      if (filePath == null) {
          return null;
      }
      int dot = extensionSeparatorIndex(filePath);
      if (dot == -1) {
          return ""; // no extension
      } else {
          return filePath.substring(dot + 1);
      }
  }

  /**
   * Get the path of the filename, minus filename and extension
   * @param filePath is the path to examine
   * @return the path, or ""
   */
  public static String getFilePath(String filePath) {
    if (filePath == null) {
      return null;
    }
    int separatorIndex = filenameSeparatorIndex(filePath);
    // is there a separator?
    if (separatorIndex > 0) {
      // yes
      return filePath.substring(0, separatorIndex); // exclude separator
    } else {
      return "";
    }
  }
  /**
   * Get the path of the filename, minus filename and extension
   * @param filePath is the path to examine
   * @return the path, or ""
   */
  public static String getFilePathWithSeparator(String filePath) {
    if (filePath == null) {
      return null;
    }
    int separatorIndex = filenameSeparatorIndex(filePath);
    // is there a separator?
    if (separatorIndex > 0) {
      // yes
      return filePath.substring(0, separatorIndex+1); // include separator
    } else {
      return "";
    }
  }

}
