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
package me.jaimegarza.syntax.env;


/**
 * Describes a definition of a command line argument
 * @author jgarza
 */
public class CommandLineOption {
  private static final int RIGHT_MARGIN = 72;
  /** display name for the option */
  String name;
  /** description to be used in the usage printout */
  String description;
  /** the full name to be used with double dash */
  String longOption;
  /** the short letter to be used with single dash */
  String shortOption;
  /** is it a flag or an argument? */
  boolean hasArg;
  /** is it required? */
  boolean required;

  /**
   * Construct one option
   * @param name is the display name of the option
   * @param description is the descriptive text to be used in the usage
   * @param longOption is the full text for double dashes
   * @param shortOption is the character for single dashes
   * @param hasArg does it require an argument, or is it a flag?
   * @param required is it required?
   * @param defaultValue what would be the default value if not required and omitted
   */
  public CommandLineOption(String shortOption, String longOption, boolean hasArg, boolean required, String description, String name) {
    super();
    this.name = name;
    this.description = description;
    this.longOption = longOption;
    this.shortOption = shortOption;
    this.hasArg = hasArg;
    this.required = required;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name
   *          the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description
   *          the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @return the longOption
   */
  public String getLongOption() {
    return longOption;
  }

  /**
   * @param longOption
   *          the longOption to set
   */
  public void setLongOption(String longOption) {
    this.longOption = longOption;
  }

  /**
   * @return the shortOption
   */
  public String getShortOption() {
    return shortOption;
  }

  /**
   * @param shortOption
   *          the shortOption to set
   */
  public void setShortOption(String shortOption) {
    this.shortOption = shortOption;
  }

  /**
   * @return the hasArg
   */
  public boolean isHasArg() {
    return hasArg;
  }

  /**
   * @param hasArg
   *          the hasArg to set
   */
  public void setHasArg(boolean hasArg) {
    this.hasArg = hasArg;
  }

  /**
   * @return the required
   */
  public boolean isRequired() {
    return required;
  }

  /**
   * @param required
   *          the required to set
   */
  public void setRequired(boolean required) {
    this.required = required;
  }
  
  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    try {
      CommandLineOption a = (CommandLineOption) obj;
      return name.equals(a.name);
    } catch (NullPointerException unused) {
      return false;
    } catch (ClassCastException unused) {
      return false;
    }
  }

  public void printUsage() {
    System.out.println(toString());
  }
  
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (!required) {
      builder.append('[');
    }
    if (shortOption != null) {
      builder.append('-').append(shortOption);
      if (longOption != null) {
        builder.append("|--").append(longOption);
      }
    } else {
      builder.append("--").append(longOption);
    }

    // if the Option has a value
    if (hasArg) {
      if (name != null) {
        builder.append(" <").append(name).append('>');
      } else {
        builder.append(" <arg>");
      }
    }
    if (!required) {
      builder.append(']');
    }
    builder.append('\n');
    if (description != null) {
      String line = description;
      int nextLineIndex = findBeginningOfNextLine(line);
      while (nextLineIndex != -1) {
        String currentLine = line.substring(0, nextLineIndex).trim();
        builder.append("    ").append(currentLine).append('\n');
        line = line.substring(nextLineIndex);
        nextLineIndex = findBeginningOfNextLine(line);
      }
      if (line.trim().length() > 0) {
        builder.append("    ").append(line.trim()).append('\n');
      }
    }
    return builder.toString();
  }
  
  /**
   * Use spaces to locate line boundaries
   * @param text is the text to check
   * @return the index of the next line, or -1 if this is the end
   */
  private int findBeginningOfNextLine(String text) {
    int position;
    
    int length = text.length();
    if (length <= RIGHT_MARGIN) {
      return -1; // no next one, this is it
    }

    position = text.indexOf('\n');
    if(position != -1 && position <= RIGHT_MARGIN) {
      return position+1; // next after LF
    }

    position = RIGHT_MARGIN-1;
    while (position >= 0) {
      char c = text.charAt(position);
      if (c == ' ' || c == '\n' || c == '\r') {
        return position+1;
      }
      position--;
    }

    position = RIGHT_MARGIN;
    while (position < length) {
      char c = text.charAt(position);
      if (c == ' ' || c == '\n' || c == '\r') {
        return position == length-1 ? -1 : position+1;
      }
      position++;
    }

    return -1;
  }

}
