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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility class to parse command line options
 * @author jgarza
 */
public class CommandLine {

  /** user provided arguments */
  private String[] args;
  /** the valid options that the program accepts */
  private List<CommandLineOption> options = new ArrayList<CommandLineOption>();
  /** the actual options with arguments provided (i.e. --foo bar) */
  private Map<CommandLineOption, CommandLineOptionValue> values = new HashMap<CommandLineOption, CommandLineOptionValue>();
  /** the actual flags provided (i.e. --one --two)  Flags do not have arguments */
  private Set<CommandLineOption> flags = new HashSet<CommandLineOption>();
  /** Not options nor arguments, filenames etc */
  private List<String> parameters = new ArrayList<String>();

  /**
   * Construct a command line object
   * @param args the user entered arguments
   */
  public CommandLine(String args[]) {
    this.args = args;
  }
  
  public void addOption(CommandLineOption option) {
    options.add(option);
  }
  
  /**
   * Perform the actual parsing of command line arguments
   * @throws CommandLineParseException on errors
   */
  public void parse() throws CommandLineParseException {
    for (int i = 0; i < args.length; i++) {
      String arg = args[i];
      String argName;
      CommandLineOption option;
      
      if (arg.startsWith("--")) { // a long option
        argName = arg.substring(2);
        option = locateLongOption(argName);
        i = addOption(i, arg, option);
      } else if (arg.startsWith("-")) { // not long, so it is short
        argName = arg.substring(1);
        option = locateShortOption(argName);
        i = addOption(i, arg, option);
      } else {
        parameters.add(arg);
      }
    }
    
    checkRequiredFlags();
    checkRequiredValues();
  }
  
  /**
   * Check to see if a flag was given
   * @param flag is the flag to check
   * @return true if given
   * @throws CommandLineParseException if the flag was not declared, or the option
   *     is not a flag
   */
  public boolean hasFlag(String flag) throws CommandLineParseException {
    for (CommandLineOption option: options) {
      if (option.getLongOption().equals(flag) || option.getShortOption().equals(flag)) {
        if (option.isHasArg()) {
          throw new CommandLineParseException("Internal error: Option " + flag + " is not a flag.");
        }
        
        return flags.contains(option);
      }
    }
    throw new CommandLineParseException("Internal error: Option " + flag + " does not exist");
  }
  
  /**
   * Get the value of a given option
   * @param argName is the option to check
   * @param defaultValue is the value if option not found
   * @return the value, or the default value.
   * @throws CommandLineParseException if the flag was not declared, or the option
   *     is a flag
   */
  public String getOptionValue(String argName, String defaultValue) throws CommandLineParseException {
    for (CommandLineOption option: options) {
      if (option.getLongOption().equals(argName) || option.getShortOption().equals(argName)) {
        if (!option.isHasArg()) {
          throw new CommandLineParseException("Internal error: Option " + argName + " is a flag.");
        }
        CommandLineOptionValue value = values.get(option);
        if (value == null) {
          return defaultValue;
        } else {
          return value.getValue();
        }
      }
    }
    throw new CommandLineParseException("Internal error: Option " + argName + " does not exist");
  }
  
  /**
   * Display the usage for this command line
   * @param commandLine is the header for the usage
   */
  public void usage(String commandLine) {
    System.out.println(commandLine);
    for (CommandLineOption option: options) {
      option.printUsage();
    }
  }
  
  /**
   * Throw exceptions if a flag that is required is missing
   * @throws CommandLineParseException
   */
  private void checkRequiredFlags() throws CommandLineParseException {
    for (CommandLineOption option: options) {
      if (option.isHasArg() == false &&
          option.isRequired() &&
          flags.contains(option) == false) {
        throw new CommandLineParseException("Flag " + option.getName() + " is required and it was not provided.");
      }
    }
  }

  /**
   * Throw exceptions if an option that is required is missing
   * @throws CommandLineParseException
   */
  private void checkRequiredValues() throws CommandLineParseException {
    for (CommandLineOption option: options) {
      if (option.isHasArg() == true &&
          option.isRequired() &&
          values.containsKey(option) == false) {
        throw new CommandLineParseException("Option " + option.getName() + " is required and it was not provided.");
      }
    }
  }

  /**
   * The user provided a function
   * @param i the index into the arguments
   * @param arg the given argument
   * @param option the found option
   * @return the new index
   * @throws CommandLineParseException on duplicates and missing arguments 
   */
  private int addOption(int i, String arg, CommandLineOption option) throws CommandLineParseException {
    if (option.isHasArg()) {
      i++;
      if (i >= args.length) {
        throw new CommandLineParseException("Option " + arg + " is missing its argument");
      }
      checkIfValueDuplicated(arg, option);
      values.put(option, new CommandLineOptionValue(option, args[i]));
    } else {
      chackIfFlagDuplicated(arg, option);
      flags.add(option);
    }
    return i;
  }

  /**
   * Do we already have a value for an argument?
   * @param arg the argument passed
   * @param option the option found
   * @throws CommandLineParseException if already provided
   */
  private void checkIfValueDuplicated(String arg, CommandLineOption option) throws CommandLineParseException {
    if (values.containsKey(option)) {
      throw new CommandLineParseException("Option " + arg + " was provided twice");
    }
  }

  /**
   * Do we already have such flag?
   * @param arg the argument passed
   * @param option the option found
   * @throws CommandLineParseException if already provided
   */
  private void chackIfFlagDuplicated(String arg, CommandLineOption option) throws CommandLineParseException {
    if (flags.contains(option)) {
      throw new CommandLineParseException("Option " + arg + " was provided twice");
    }
  }
  
  /**
   * Locate a command line given its long argument
   * @param arg is the argument being searched
   * @return the option, if found, or null
   * @throws CommandLineParseException 
   */
  private CommandLineOption locateLongOption(String arg) throws CommandLineParseException {
    if (arg == null) {
      throw new CommandLineParseException("argument cannot be null");
    }
    
    for (CommandLineOption option : options) {
      if (option.getLongOption().equals(arg)) {
        return option;
      }
    }
    throw new CommandLineParseException("argument " + arg + " is not valid");
  }

  /**
   * Locate a command line given its short argument
   * @param arg is the argument being searched
   * @return the option, if found, or null
   * @throws CommandLineParseException 
   */
  private CommandLineOption locateShortOption(String arg) throws CommandLineParseException {
    if (arg == null) {
      throw new CommandLineParseException("argument cannot be null");
    }
    if (arg.length() != 1) {
      throw new CommandLineParseException("argument " + arg + " needs to be one character (only one dash provided)");
    }
    for (CommandLineOption option : options) {
      if (option.getShortOption().equals(arg)) {
        return option;
      }
    }
    throw new CommandLineParseException("argument " + arg + " is not valid");
  }

  /**
   * @return the parameters
   */
  public List<String> getParameters() {
    return parameters;
  }

  /**
   * @param parameters the parameters to set
   */
  public void setParameters(List<String> parameters) {
    this.parameters = parameters;
  }
}
