package me.jaimegarza.syntax;

import me.jaimegarza.syntax.cli.Environment;
import me.jaimegarza.syntax.generator.CodeParser;
import me.jaimegarza.syntax.generator.CodeWriter;
import me.jaimegarza.syntax.generator.RuntimeData;
import me.jaimegarza.syntax.generator.StructuralAnalyzer;
import me.jaimegarza.syntax.generator.TableGenerator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Syntax {

  private Environment environment;
  private RuntimeData runtimeData = new RuntimeData();

  public final Log LOG = LogFactory.getLog(this.getClass());

  public Syntax(Environment environment) {
    this.environment = environment;
  }

  private void execute() {
    CodeParser parser = new CodeParser(environment, runtimeData);
    StructuralAnalyzer analyzer = new StructuralAnalyzer(environment, runtimeData);
    TableGenerator generator = new TableGenerator(environment, runtimeData);
    CodeWriter writer = new CodeWriter(environment, runtimeData);
    try {
      parser.execute();
      analyzer.execute();
      generator.execute();
      writer.execute();
    } catch (AnalysisException e) {
      LOG.error("Internal error: " + e.getMessage(), e);
    } catch (ParsingException e) {
      LOG.error("Parsing error: " + e.getMessage(), e);
    } catch (OutputException e) {
      LOG.error("Output error: " + e.getMessage(), e);
    }
  }

  public static void main(String args[]) {
    Environment environment = new Environment("Syntax", args);
    if (environment.isDebug()) {
      System.out.println("environment\n" + environment);
    }

    try {
      if (environment.isVerbose()) {
        System.out.println("Syntax");
      }
      Syntax syntaxTool = new Syntax(environment);
      syntaxTool.execute();
    } finally {
      if (environment.isVerbose()) {
        System.out.println("Done\n");
      }
      environment.release();
    }
  }

}
