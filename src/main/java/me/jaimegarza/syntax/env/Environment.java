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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import me.jaimegarza.syntax.algorithm.Algorithm;
import me.jaimegarza.syntax.algorithm.AlgorithmicSupport;
import me.jaimegarza.syntax.algorithm.LalrAlgorithmicSupport;
import me.jaimegarza.syntax.algorithm.SlrAlgorithmicSupport;
import me.jaimegarza.syntax.code.Fragments;
import me.jaimegarza.syntax.definition.Driver;
import me.jaimegarza.syntax.language.BaseLanguageSupport;
import me.jaimegarza.syntax.language.Language;
import me.jaimegarza.syntax.language.LanguageSupport;
import me.jaimegarza.syntax.util.FormattingPrintStream;

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
 * Examines the command line resources and encapsulates the resulting
 * options in getter methods.
 * <p>
 * In addition to that it offers the private output, report and source
 * fields to use as PrintWriters and BufferedReaders.
 */
@SuppressWarnings("unused")
public class Environment extends Options {
  private static final String CLASSPATH_PREFIX = "classpath:";

  private static final long serialVersionUID = -4212115971332112220L;

  private static final boolean NO_ARG = false;
  private static final boolean HAS_ARG = true;
  private static final boolean NO_OPTIONAL_VALUE = false;
  private static final boolean OPTIONAL_VALUE = true;
  private static final boolean NOT_REQUIRED = false;
  private static final boolean REQUIRED = true;

  private final Log LOG = LogFactory.getLog(this.getClass());

  private String relatedTitle;
  private String[] args;
  private CommandLine cmd = null;
  private Language languageEnum;
  private Algorithm algorithmEnum;
  private boolean verbose;
  private boolean debug;
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
  private RuntimeData runtimeData = new RuntimeData();

  public BufferedReader source = null;
  public FormattingPrintStream output = null;
  public FormattingPrintStream include = null;
  public FormattingPrintStream report = null;
  public AlgorithmicSupport algorithm = null;
  public LanguageSupport language = null;
  
  private ResourceBundle fragments;
  private int parsedLine;
  private Locale locale;

  private Driver driver;



  /**
   * Construct an environment with the given arguments
   * @param args command line arguments
   * @param runtimeData is the data used during generation
   */
  public Environment(final String args[]) {
    this("", args);
  }

  /**
   * Construct an environment with the given arguments
   * @param title title of the program
   * @param args command line arguments
   */
  public Environment(final String title, final String args[]) {
    super();
    this.relatedTitle = title;
    this.args = args;
    this.runtimeData.setEnvironment(this);
    init();
    parse();
  }

  /**
   * close files
   */
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
  
  public String getFragment(String key) {
    return fragments.getString(key);
  }
  
  public String formatFragment(String key, Object...objects) {
    String fragment = fragments.getString(key);
    return MessageFormat.format(fragment, objects);
  }

  /**
   * Parse the command line arguments
   */
  private void parse() {
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
      setDriver();
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

  /**
   * Register a command line valid option
   * @param shortOption the -<letter>
   * @param longOption the --<word> option
   * @param hasArg if it has an extra argument
   * @param isValueOptional if it has an arg, is it required?
   * @param isRequired is this option required?
   * @param description a long description to be used in help
   * @param argName the argument name when it has an arg, for the help
   */
  private void add(String shortOption, String longOption, boolean hasArg, boolean isValueOptional, boolean isRequired,
      String description, String argName) {
    Option option = new Option(shortOption, longOption, hasArg, description);
    option.setOptionalArg(isValueOptional);
    option.setRequired(isRequired);
    option.setArgName(argName);
    addOption(option);
  }

  /**
   * initialize the compilation options.
   */
  private void init() {
    add("h", "help", NO_ARG, NO_OPTIONAL_VALUE, NOT_REQUIRED, "displays the usage of the tool", "");
    add("l", "language", HAS_ARG, NO_OPTIONAL_VALUE, NOT_REQUIRED,
        "Setup the syntax and output to be either java|c|pascal, default c", "language");
    add("v", "verbose", NO_ARG, NO_OPTIONAL_VALUE, NOT_REQUIRED, "Verbose output, default no", "");
    add("a", "algorithm", HAS_ARG, NO_OPTIONAL_VALUE, NOT_REQUIRED,
        "Algorithm, either s|l (For SLR and LALR, default LALR)", "algorithm");
    add("g", "debug", NO_ARG, NO_OPTIONAL_VALUE, NOT_REQUIRED, "Prints debug information", "");
    add("n", "noline", NO_ARG, NO_OPTIONAL_VALUE, NOT_REQUIRED, "Disable #line directives in C, default enabled", "");
    add("m", "margin", HAS_ARG, NO_OPTIONAL_VALUE, NOT_REQUIRED, "Right margin on generated source, default 8000",
        "margin");
    add("i", "indent", HAS_ARG, NO_OPTIONAL_VALUE, NOT_REQUIRED, "Indent by n spaces, default 2", "spaces");
    add("p", "packing", HAS_ARG, NO_OPTIONAL_VALUE, NOT_REQUIRED,
        "Packing format of parser (packed|tabular, default packed)\n" +
        "* please note that unpacked tables are \n" +
        "* mostly for didactical purposes as they \n" +
        "* may lend a big number of states in a \n" +
        "* sparsely populated table.", "packing");
    add("x", "external", HAS_ARG, NO_OPTIONAL_VALUE, NOT_REQUIRED,
        "Generate include file (true,on,yes,1|false,off,no,0, default is language dependent)", "external");
    add("d", "driver", HAS_ARG, NO_OPTIONAL_VALUE, NOT_REQUIRED,
        "What parser driver is to be used (parser|scanner, default is parser)", "parser");
  }

  /**
   * Check to see if an option is present
   * @param option the option by string
   * @return true if provided
   */
  private boolean has(String option) {
    if (cmd != null) {
      return cmd.hasOption(option);
    }
    return false;
  }

  /**
   * Obtain the argument of an option
   * @param option the option by string
   * @param defaultValue is the value returned if option not given
   * @return the option value
   */
  private String get(String option, String defaultValue) {
    if (cmd != null) {
      return cmd.getOptionValue(option, defaultValue);
    }
    return defaultValue;
  }

  /**
   * @return true if help is requested
   */
  private boolean getHelp() {
    return has("h");
  }

  /**
   * compute the language from options
   * @throws ParseException if the option cannot be computed
   */
  private void setLanguage() throws ParseException {
    String value = get("l", "c");
    for (Language l: Language.values()) {
      if (l.support().getId().equalsIgnoreCase(value) ||
          l.support().getLanguageCode().equalsIgnoreCase(value)) {
        this.languageEnum = l;
        break;
      }
    }
    if (this.languageEnum == null) {
      throw new ParseException("Option -a|--algorithm is not valid :" + value);
    }
    this.language = this.languageEnum.support();
    if (this.language == null) {
      throw new ParseException("language " + value + " was no instantiated.");
    }
    ((BaseLanguageSupport) this.language).setEnvironment(this);
    this.locale = new Locale(language.getLanguageCode());
    fragments = ResourceBundle.getBundle(Fragments.class.getCanonicalName(), locale);
    
  }

  /**
   * compute the algorithm from options
   * @throws ParseException if the option cannot be computed
   */
  private void setAlgorithm() throws ParseException {
    String value = get("a", "l");
    if (value.equalsIgnoreCase("s") || value.equalsIgnoreCase("slr")) {
      this.algorithmEnum = Algorithm.SLR;
      this.algorithm = new SlrAlgorithmicSupport(this);
    } else if (value.equalsIgnoreCase("l") || value.equalsIgnoreCase("lalr")) {
      this.algorithmEnum = Algorithm.LALR;
      this.algorithm = new LalrAlgorithmicSupport(this);
    } else {
      throw new ParseException("Option -a|--algorithm is not valid :" + value);
    }
  }

  /**
   * compute the packed/unpacked nature of table from options
   * @throws ParseException if the option cannot be computed
   */

  private void setPacking() throws ParseException {
    String value = get("p", "p");
    if (value.equalsIgnoreCase("p") || value.equalsIgnoreCase("packed")) {
      this.packed = true;
    } else if (value.equalsIgnoreCase("t") || value.equalsIgnoreCase("tabular")) {
      this.packed = false;
    } else {
      throw new ParseException("Option -p|--packing is not valid :" + value);
    }
  }

  /**
   * compute the external file requirements from options
   * @throws ParseException if the option cannot be computed
   */
  private void setExternalInclude() throws ParseException {
    String value = get("x", Boolean.toString(language.getDefaultIncludeFlag()));
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
    } else {
      throw new ParseException("Option -x|--external is not valid :" + value);
    }
  }

  /**
   * compute the driver to be used
   * @throws ParseException if the option cannot be computed
   */
  private void setDriver() throws ParseException {
    String value = get("d", "parser");
    if (value.equalsIgnoreCase("p") || value.equalsIgnoreCase("parser")) {
      this.driver = Driver.PARSER;
    } else if (value.equalsIgnoreCase("s") || value.equalsIgnoreCase("scanner")) {
      this.driver = Driver.SCANNER;
    } else {
      throw new ParseException("Option -d|--driver is not valid :" + value);
    }
  }

  /**
   * compute the verbosity from options
   * @throws ParseException if the option cannot be computed
   */
  private void setVerbose() throws ParseException {
    this.verbose = has("v");
  }

  /**
   * compute the debugging output from options
   * @throws ParseException if the option cannot be computed
   */
  private void setDebug() throws ParseException {
    this.debug = has("g");
  }

  /**
   * compute the need to output #line on C files from options
   * @throws ParseException if the option cannot be computed
   */
  private void setEmitLine() throws ParseException {
    this.emitLine = !has("n");
  }

  /**
   * compute the margin from options
   * @throws ParseException if the option cannot be computed
   */
  private void setMargin() throws ParseException {
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

  /**
   * compute the indentation from options
   * @throws ParseException if the option cannot be computed
   */
  private void setIndent() throws ParseException {
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

  /**
   * print the usage of the program
   */
  private void printHelp() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("Syntax [options] filename.sy", this);
  }

  /**
   * getter for the title
   */
  public String getTitle() {
    return relatedTitle;
  }

  /**
   * Obtain the file by index.  I keep a list of filenames where:
   * <ul>
   * <li>0. Source file
   * <li>1. Output file
   * <li>2. Report file
   * </ul>
   * @param index the index of the filename
   * @param isRequired if it has to be there
   * @param argumentName the name of the option
   * @return the file
   * @throws ParseException if the option cannot be read
   */
  private File getFile(int index, boolean isRequired, String argumentName) throws ParseException {
    if (index >= fileNames.size()) {
      if (isRequired) {
        throw new ParseException("filename for  " + argumentName + "was not provided");
      }
      return null;
    }
    
    String fileName = (String) fileNames.get(index);
    File file;
    if (fileName.startsWith(CLASSPATH_PREFIX)) {
      // testing cases
      fileName = fileName.substring(CLASSPATH_PREFIX.length());
      URL url = Thread.currentThread().getContextClassLoader().getResource(fileName);
      file = new File(URI.create(url.toString()));
    } else {
      file = new File(fileName);
    }
    return file;
  }

  /**
   * given a filename, return a new filename with the extension provided
   * @param filename is the input filename
   * @param extension is the new extension
   * @return the new filename
   */
  private String replaceExtension(String filename, String extension) {
    if (filename == null) {
      return null;
    }
    return FilenameUtils.getFullPath(filename) + FilenameUtils.getBaseName(filename) + extension;
  }

  /**
   * Compute the source file
   * @throws ParseException if the source file cannot be computed
   */
  private void setSourceFile() throws ParseException {
    this.sourceFile = getFile(0, true, "source file");
    if (sourceFile != null) {
      this.outputFile = new File(replaceExtension(sourceFile.getPath(), language.getExtensionSuffix()));
      if (externalInclude) {
        this.includeFile = new File(replaceExtension(sourceFile.getPath(), language.getIncludeExtensionSuffix()));
      }
      this.reportFile = new File(replaceExtension(sourceFile.getPath(), ".txt"));
    }
    try {
      source = new BufferedReader(new InputStreamReader(FileUtils.openInputStream(sourceFile)));
    } catch (IOException e) {
      throw new ParseException("Cannot open file " + sourceFile);
    }
  }

  /**
   * Compute the report file
   * @throws ParseException if the report file cannot be computed
   */
  private void setReportFile() throws ParseException {
    File reportFile = getFile(2, false, "report file");
    if (reportFile == null) {
      reportFile = new File(replaceExtension(outputFile.getAbsolutePath(), ".txt"));
    }
    if (reportFile != null) {
      this.reportFile = reportFile;
    }
    try {
      this.report = new FormattingPrintStream(this, FileUtils.openOutputStream(this.reportFile));
    } catch (IOException e) {
      throw new ParseException("Cannot open file " + reportFile);
    }
  }

  /**
   * Compute the output file
   * @throws ParseException if the output file cannot be computed
   */
  private void setOutputFile() throws ParseException {
    File outputFile = getFile(1, false, "output file");
    if (outputFile == null) {
      outputFile = new File(replaceExtension(sourceFile.getAbsolutePath(), language.getExtensionSuffix()));
    }
    if (outputFile != null) {
      this.outputFile = outputFile;
      try {
        output = new FormattingPrintStream(this, FileUtils.openOutputStream(outputFile));
        if (externalInclude) {
          this.includeFile = new File(replaceExtension(outputFile.getPath(), language.getIncludeExtensionSuffix()));
          this.include = new FormattingPrintStream(this, FileUtils.openOutputStream(this.includeFile));
        } else {
          this.include = this.output;
        }
      } catch (IOException e) {
        throw new ParseException("Cannot open file " + outputFile);
      }

    }
  }

  /**
   * display an error
   * @param line is the source file line number
   * @param msg is the message for printf
   * @param args are the additional entries in the message
   */
  public void error(int line, String msg, Object... args) {
    System.err.printf("%s(%05d) : ", sourceFile, line == -1 ? parsedLine + 1 : line);
    System.err.printf(msg + "\n", args);

  }

  /**
   * @return the relatedTitle
   */
  public String getRelatedTitle() {
    return relatedTitle;
  }

  /**
   * @return the args
   */
  public String[] getArgs() {
    return args;
  }

  /**
   * @return the cmd
   */
  public CommandLine getCmd() {
    return cmd;
  }

  /**
   * @return the language
   */
  public Language getLanguageEnum() {
    return languageEnum;
  }

  /**
   * @return the verbose
   */
  public boolean isVerbose() {
    return verbose;
  }

  /**
   * @return the debug
   */
  public boolean isDebug() {
    return debug;
  }

  /**
   * @return the algorithm
   */
  public Algorithm getAlgorithmType() {
    return algorithmEnum;
  }
  
  /**
   * @return the driver being used
   */
  public Driver getDriver() {
    return driver;
  }

  /**
   * @return the emitLine
   */
  public boolean isEmitLine() {
    return emitLine;
  }

  /**
   * @return the margin
   */
  public int getMargin() {
    return margin;
  }

  /**
   * @return the indent
   */
  public int getIndent() {
    return indent;
  }

  /**
   * @return the packed
   */
  public boolean isPacked() {
    return packed;
  }

  /**
   * @return the externalInclude
   */
  public boolean isExternalInclude() {
    return externalInclude;
  }

  /**
   * @return the sourceFile
   */
  public File getSourceFile() {
    return sourceFile;
  }

  /**
   * @return the outputFile
   */
  public File getOutputFile() {
    return outputFile;
  }

  /**
   * @return the includeFile
   */
  public File getIncludeFile() {
    return includeFile;
  }

  /**
   * @return the reportFile
   */
  public File getReportFile() {
    return reportFile;
  }

  /**
   * @return the parsedLine
   */
  public int getParsedLine() {
    return parsedLine;
  }

  /**
   * @return the locale
   */
  public Locale getLocale() {
    return locale;
  }

  /**
   * @return the runtimeData
   */
  public RuntimeData getRuntimeData() {
    return runtimeData;
  }

  /**
   * @param runtimeData the runtimeData to set
   */
  public void setRuntimeData(RuntimeData runtimeData) {
    this.runtimeData = runtimeData;
    this.runtimeData.setEnvironment(this);
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return relatedTitle +
           "{\n" +
             "  verbose: " +
             verbose +
             "\n" +
             "  language: " +
             languageEnum +
             "\n" +
             "  algorithm: " +
             algorithmEnum +
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

}