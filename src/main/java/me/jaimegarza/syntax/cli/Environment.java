/*
 ===============================================================================
 Copyright (c) 1985, 2012, Jaime Garza
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
     * Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.
     * Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.
     * Neither the name of the <organization> nor the
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
package me.jaimegarza.syntax.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class that extends a set of options to provide a title for a related set of
 * options.
 */
@SuppressWarnings("unused")
public class Environment extends Options {
  private static final long serialVersionUID = -4212115971332112220L;

  private static final boolean NO_ARG = false;
  private static final boolean HAS_ARG = true;
  private static final boolean NO_OPTIONAL_VALUE = false;
  private static final boolean OPTIONAL_VALUE = true;
  private static final boolean NOT_REQUIRED = false;
  private static final boolean REQUIRED = true;

  public final Log LOG = LogFactory.getLog(this.getClass());

  private String relatedTitle;
  private String[] args;
  private CommandLine cmd = null;
  private Language language;
  private boolean verbose;
  private boolean debug;
  private Algorithm algorithm;
  private boolean emitLine;
  private int margin;
  private int indent;
  private boolean packed;
  private boolean externalInclude;
  @SuppressWarnings("rawtypes")
  private List fileNames;
  private File sourceFile;
  private File outputFile;
  private File includeFile;
  private File reportFile;

  public BufferedReader source = null;
  public PrintStream output = null;
  public PrintStream include = null;
  public PrintStream report = null;

  private int parsedLine;

  public Environment(final String args[]) {
    this("", args);
  }

  public Environment(final String title, final String args[]) {
    super();
    this.relatedTitle = title;
    this.args = args;
    init();
    parse();
  }

  public void release() {
    if (source != null) {
      IOUtils.closeQuietly(source);
    }
    if (output != null) {
      IOUtils.closeQuietly(output);
    }
    if (include != null) {
      IOUtils.closeQuietly(include);
    }
    if (report != null) {
      IOUtils.closeQuietly(report);
    }
  }

  public void parse() {
    CommandLineParser parser = new GnuParser();
    try {
      cmd = parser.parse(this, args);
      if (getHelp()) {
        printHelp();
        System.exit(0);
      }
      setLanguage();
      setVerbose();
      setDebug();
      setAlgorithm();
      setEmitLine();
      setMargin();
      setIndent();
      setPacking();
      setExternalInclude();
      this.fileNames = cmd.getArgList();
      setSourceFile();
      setOutputFile();
      setReportFile();
    } catch (ParseException e) {
      System.out.println("Command line error: " + e.getMessage());
      printHelp();
      System.exit(1);
    }

  }

  public void add(String shortOption, String longOption, boolean hasArg, boolean isValueOptional, boolean isRequired,
      String description, String argName) {
    Option option = new Option(shortOption, longOption, hasArg, description);
    option.setOptionalArg(isValueOptional);
    option.setRequired(isRequired);
    option.setArgName(argName);
    addOption(option);
  }

  public void init() {
    add("h", "help", NO_ARG, NO_OPTIONAL_VALUE, NOT_REQUIRED, "displays the usage of the tool", "");
    add("l", "language", HAS_ARG, NO_OPTIONAL_VALUE, NOT_REQUIRED,
        "Setup the syntax and output to be either java|c|pascal, default c", "language");
    add("v", "verbose", NO_ARG, NO_OPTIONAL_VALUE, NOT_REQUIRED, "Verbose output, default no", "");
    add("a", "algorithm", HAS_ARG, NO_OPTIONAL_VALUE, NOT_REQUIRED,
        "Algorithm, either s|l (For SLR and LALR, default LALR)", "algorithm");
    add("d", "debug", NO_ARG, NO_OPTIONAL_VALUE, NOT_REQUIRED, "Prints debug information", "");
    add("n", "noline", NO_ARG, NO_OPTIONAL_VALUE, NOT_REQUIRED, "Disable #line directives in C, default enabled", "");
    add("m", "margin", HAS_ARG, NO_OPTIONAL_VALUE, NOT_REQUIRED, "Right margin on generated source, default 8000",
        "margin");
    add("i", "indent", HAS_ARG, NO_OPTIONAL_VALUE, NOT_REQUIRED, "Indent by n spaces, default 2", "spaces");
    add("p", "packing", HAS_ARG, NO_OPTIONAL_VALUE, NOT_REQUIRED,
        "Packing format of parser (packed|tabular, default packed)", "packing");
    add("x", "external", HAS_ARG, NO_OPTIONAL_VALUE, NOT_REQUIRED,
        "Generate include file (true,on,yes,1|false,off,no,0, default true)", "external");
  }

  public boolean has(String option) {
    if (cmd != null) {
      return cmd.hasOption(option);
    }
    return false;
  }

  public String get(String option, String defaultValue) {
    if (cmd != null) {
      return cmd.getOptionValue(option, defaultValue);
    }
    return defaultValue;
  }

  public boolean getHelp() {
    return has("h");
  }

  public void setLanguage() throws ParseException {
    String value = get("l", "c");
    if (value.equalsIgnoreCase("c")) {
      this.language = Language.C;
    } else if (value.equalsIgnoreCase("j") || value.equalsIgnoreCase("java")) {
      this.language = Language.java;
    } else if (value.equalsIgnoreCase("p") || value.equalsIgnoreCase("pascal")) {
      this.language = Language.pascal;
    } else {
      throw new ParseException("Option -a|--algorithm is not valid :" + value);
    }
  }

  public void setAlgorithm() throws ParseException {
    String value = get("a", "l");
    if (value.equalsIgnoreCase("s") || value.equalsIgnoreCase("slr")) {
      this.algorithm = Algorithm.SLR;
    } else if (value.equalsIgnoreCase("l") || value.equalsIgnoreCase("lalr")) {
      this.algorithm = Algorithm.LALR;
    } else {
      throw new ParseException("Option -a|--algorithm is not valid :" + value);
    }
  }

  public void setPacking() throws ParseException {
    String value = get("p", "p");
    if (value.equalsIgnoreCase("p") || value.equalsIgnoreCase("packed")) {
      this.packed = true;
    } else if (value.equalsIgnoreCase("t") || value.equalsIgnoreCase("tabular")) {
      this.packed = false;
    } else {
      throw new ParseException("Option -p|--packing is not valid :" + value);
    }
  }

  public void setExternalInclude() throws ParseException {
    String value = get("x", "true");
    if (value.equalsIgnoreCase("true") ||
        value.equalsIgnoreCase("yes") ||
          value.equalsIgnoreCase("on") ||
          value.equalsIgnoreCase("1")) {
      this.externalInclude = true;
    } else if (value.equalsIgnoreCase("false") ||
               value.equalsIgnoreCase("no") ||
                 value.equalsIgnoreCase("off") ||
                 value.equalsIgnoreCase("0")) {
      this.externalInclude = false;
      ;
    } else {
      throw new ParseException("Option -x|--external is not valid :" + value);
    }
  }

  public void setVerbose() throws ParseException {
    this.verbose = has("v");
  }

  public void setDebug() throws ParseException {
    this.debug = has("d");
  }

  public void setEmitLine() throws ParseException {
    this.emitLine = !has("n");
  }

  public void setMargin() throws ParseException {
    int value = 0;
    try {
      value = Integer.parseInt(get("m", "8000"));
    } catch (NumberFormatException e) {
      throw new ParseException("Option -m|--margin is not valid :" + value);
    }
    if (value <= 80) {
      throw new ParseException("Option -m|--margin should be greater than 80 :" + value);
    }
    this.margin = value;
  }

  public void setIndent() throws ParseException {
    int value = 0;
    try {
      value = Integer.parseInt(get("i", "2"));
    } catch (NumberFormatException e) {
      throw new ParseException("Option -i|--indent is not valid :" + value);
    }
    if (value < 2) {
      throw new ParseException("Option -m|--margin should be greater than 2 :" + value);
    }
    this.indent = value;
  }

  public void printHelp() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("Syntax [options] filename.sy", this);
  }

  public String getTitle() {
    return relatedTitle;
  }

  public File getFile(int index, boolean isRequired, String argumentName) throws ParseException {
    if (index >= fileNames.size()) {
      if (isRequired) {
        throw new ParseException("filename for  " + argumentName + "was not provided");
      }
      return null;
    }
    return new File((String) fileNames.get(index));
  }

  public String replaceExtension(String filename, String extension) {
    if (filename == null) {
      return null;
    }
    return FilenameUtils.getFullPath(filename) + FilenameUtils.getBaseName(filename) + extension;
  }

  public void setSourceFile() throws ParseException {
    File sourceFile = getFile(0, true, "source file");
    this.sourceFile = sourceFile;
    if (sourceFile != null) {
      this.outputFile = new File(replaceExtension(sourceFile.getPath(), language.extension()));
      if (externalInclude) {
        this.includeFile = new File(replaceExtension(sourceFile.getPath(), language.includeExtension()));
      }
      this.reportFile = new File(replaceExtension(sourceFile.getPath(), ".txt"));
    }
    try {
      source = new BufferedReader(new InputStreamReader(FileUtils.openInputStream(sourceFile)));
    } catch (IOException e) {
      throw new ParseException("Cannot open file " + sourceFile);
    }
  }

  public void setReportFile() throws ParseException {
    File reportFile = getFile(2, false, "report file");
    if (reportFile == null) {
      reportFile = new File(replaceExtension(outputFile.getAbsolutePath(), ".txt"));
    }
    if (reportFile != null) {
      this.reportFile = reportFile;
    }
    try {
      this.report = new PrintStream(FileUtils.openOutputStream(this.reportFile));
    } catch (IOException e) {
      throw new ParseException("Cannot open file " + reportFile);
    }
  }

  public void setOutputFile() throws ParseException {
    File outputFile = getFile(1, false, "output file");
    if (outputFile == null) {
      outputFile = new File(replaceExtension(sourceFile.getAbsolutePath(), language.extension()));
    }
    if (outputFile != null) {
      this.outputFile = outputFile;
      try {
        output = new PrintStream(FileUtils.openOutputStream(outputFile));
        if (externalInclude) {
          this.includeFile = new File(replaceExtension(outputFile.getPath(), language.includeExtension()));
          this.include = new PrintStream(FileUtils.openOutputStream(this.includeFile));
        }
      } catch (IOException e) {
        throw new ParseException("Cannot open file " + outputFile);
      }

    }
  }

  @Override
  public String toString() {
    return relatedTitle +
           "{\n" +
             "  verbose: " +
             verbose +
             "\n" +
             "  language: " +
             language +
             "\n" +
             "  algorithm: " +
             algorithm +
             "\n" +
             "  emit #line: " +
             emitLine +
             "\n" +
             "  margin: " +
             margin +
             "\n" +
             "  indent: " +
             indent +
             "\n" +
             "  packed: " +
             packed +
             "\n" +
             "  externalInclude: " +
             externalInclude +
             "\n" +
             "  sourceFile: " +
             (sourceFile == null ? "unknown" : sourceFile.getPath()) +
             "\n" +
             "  outputFile: " +
             (outputFile == null ? "(none)" : outputFile.getPath()) +
             "\n" +
             "  includeFile: " +
             (includeFile == null ? "(none)" : includeFile.getPath()) +
             "\n" +
             "  reportFile: " +
             (reportFile == null ? "(none)" : reportFile.getPath()) +
             "\n}\n" +
             super.toString() +
             "}";
  }

  public Language getLanguage() {
    return language;
  }

  public boolean isVerbose() {
    return verbose;
  }

  public boolean isDebug() {
    return debug;
  }

  public Algorithm getAlgorithm() {
    return algorithm;
  }

  public boolean isEmitLine() {
    return emitLine;
  }

  public int getMargin() {
    return margin;
  }

  public int getIndent() {
    return indent;
  }

  public boolean isPacked() {
    return packed;
  }

  public boolean isExternalInclude() {
    return externalInclude;
  }

  public File getSourceFile() {
    return sourceFile;
  }

  public File getOutputFile() {
    return outputFile;
  }

  public File getIncludeFile() {
    return includeFile;
  }

  public File getReportFile() {
    return reportFile;
  }

  public void error(int line, String msg, Object... args) {
    System.err.printf("%s(%05d) : ", sourceFile, line == -1 ? parsedLine + 1 : line);
    System.err.printf(msg + "\n", args);

  }

}
