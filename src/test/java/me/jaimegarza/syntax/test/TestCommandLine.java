package me.jaimegarza.syntax.test;

import me.jaimegarza.syntax.env.CommandLine;
import me.jaimegarza.syntax.env.CommandLineOption;
import me.jaimegarza.syntax.env.CommandLineParseException;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestCommandLine extends AbstractTestBase {
  CommandLineOption options[] = new CommandLineOption[] {
      new CommandLineOption("f", "foo", false, false, "Do we need to foo the system, or is it unfooed", "foo"),
      new CommandLineOption("b", "bar", true, false, "Specifies where to set up the bar", "bar"),
      new CommandLineOption("z", "baz", false, true, "Better tell me if this is baz or not", "baz"),
      new CommandLineOption("q", "qux", true, true, "The user really needs to enter a qux value, fo sheez. " +
          "I am also testing with longer command line descriptions to test right margin\n and in some cases the use of newline.  This example should do for the most part.", "qux"),
  };

  @Test
  public void testPositiveLongOptions() {
    CommandLine commandLine = new CommandLine(new String[] {"--foo", "--bar", "low", "--baz", "--qux", "quux"});
    addAllCommandLineOptions(commandLine);
    try {
      commandLine.parse();
      Assert.assertTrue(commandLine.hasFlag("f"));
      Assert.assertTrue(commandLine.hasFlag("z"));
      Assert.assertEquals(commandLine.getOptionValue("b", "high"), "low");
      Assert.assertEquals(commandLine.getOptionValue("q", ""), "quux");
    } catch (CommandLineParseException e) {
      Assert.fail("Parser error", e);
    }
  }

  @Test
  public void testPositiveShortOptions() {
    CommandLine commandLine = new CommandLine(new String[] {"-f", "-b", "low", "-z", "-q", "quux"});
    addAllCommandLineOptions(commandLine);
    try {
      commandLine.parse();
      Assert.assertTrue(commandLine.hasFlag("foo"));
      Assert.assertTrue(commandLine.hasFlag("baz"));
      Assert.assertEquals(commandLine.getOptionValue("bar", "high"), "low");
      Assert.assertEquals(commandLine.getOptionValue("qux", ""), "quux");
    } catch (CommandLineParseException e) {
      Assert.fail("Parser error", e);
    }
  }

  @Test
  public void testPositiveParameters() {
    CommandLine commandLine = new CommandLine(new String[] {"--baz", "--qux", "quux", "abc", "def", "g"});
    addAllCommandLineOptions(commandLine);
    try {
      commandLine.parse();
      Assert.assertTrue(commandLine.hasFlag("z"));
      Assert.assertEquals(commandLine.getOptionValue("q", ""), "quux");
      Assert.assertEquals(commandLine.getParameters().size(), 3);
      Assert.assertTrue(commandLine.getParameters().contains("abc"));
      Assert.assertTrue(commandLine.getParameters().contains("def"));
      Assert.assertTrue(commandLine.getParameters().contains("g"));
    } catch (CommandLineParseException e) {
      Assert.fail("Parser error", e);
    }
  }

  @Test
  public void testPositiveDefault() {
    CommandLine commandLine = new CommandLine(new String[] {"--baz", "--qux", "quux"});
    addAllCommandLineOptions(commandLine);
    try {
      commandLine.parse();
      Assert.assertTrue(commandLine.hasFlag("z"));
      Assert.assertEquals(commandLine.getOptionValue("q", ""), "quux");
      Assert.assertEquals(commandLine.getOptionValue("bar", "missing"), "missing");
    } catch (CommandLineParseException e) {
      Assert.fail("Parser error", e);
    }
  }

  @Test
  public void testNegativeMissingRequiredFlag() {
    CommandLine commandLine = new CommandLine(new String[] {"-f", "-b", "low", "-q", "quux"});
    addAllCommandLineOptions(commandLine);
    try {
      commandLine.parse();
      Assert.assertTrue(commandLine.hasFlag("foo"));
      Assert.assertTrue(commandLine.hasFlag("baz"));
      Assert.assertEquals(commandLine.getOptionValue("bar", "high"), "low");
      Assert.assertEquals(commandLine.getOptionValue("qux", ""), "quux");
      Assert.fail();
    } catch (CommandLineParseException e) {
      Assert.assertTrue(e.getMessage().contains("baz"));
    }
  }

  @Test
  public void testNegativeArgIsNotFlag() {
    CommandLine commandLine = new CommandLine(new String[] {"-f", "-b", "low", "-z", "-q", "quux"});
    addAllCommandLineOptions(commandLine);
    try {
      commandLine.parse();
      Assert.assertTrue(commandLine.hasFlag("q"));
      Assert.fail();
    } catch (CommandLineParseException e) {
      Assert.assertTrue(e.getMessage().contains(" q "));
    }
  }

  @Test
  public void testNegativeArgIsNotOption() {
    CommandLine commandLine = new CommandLine(new String[] {"-f", "-b", "low", "-z", "-q", "quux"});
    addAllCommandLineOptions(commandLine);
    try {
      commandLine.parse();
      Assert.assertEquals(commandLine.getOptionValue("z", null), null);
      Assert.fail();
    } catch (CommandLineParseException e) {
      Assert.assertTrue(e.getMessage().contains(" z "));
    }
  }

  @Test
  public void testNegativeOptionNotComplete() {
    CommandLine commandLine = new CommandLine(new String[] {"-f", "-b", "low", "-z", "-q"});
    addAllCommandLineOptions(commandLine);
    try {
      commandLine.parse();
      Assert.fail();
    } catch (CommandLineParseException e) {
      Assert.assertTrue(e.getMessage().contains(" -q "));
    }
  }

  @Test
  public void testNegativeOptionNotExisting() {
    CommandLine commandLine = new CommandLine(new String[] {"-f", "-b", "low", "-z", "-q", "quux"});
    addAllCommandLineOptions(commandLine);
    try {
      commandLine.parse();
      Assert.assertEquals(commandLine.getOptionValue("x", null), null);
      Assert.fail();
    } catch (CommandLineParseException e) {
      Assert.assertTrue(e.getMessage().contains("x"));
    }
  }

  @Test
  public void testNegativeFlagNotExisting() {
    CommandLine commandLine = new CommandLine(new String[] {"-f", "-b", "low", "-z", "-q", "quux"});
    addAllCommandLineOptions(commandLine);
    try {
      commandLine.parse();
      commandLine.hasFlag("x");
      Assert.fail();
    } catch (CommandLineParseException e) {
      Assert.assertTrue(e.getMessage().contains("x"));
    }
  }

  @Test
  public void testNegativeMissingRequiredOption() {
    CommandLine commandLine = new CommandLine(new String[] {"-f", "-b", "low", "-z"});
    addAllCommandLineOptions(commandLine);
    try {
      commandLine.parse();
      Assert.assertTrue(commandLine.hasFlag("foo"));
      Assert.assertTrue(commandLine.hasFlag("baz"));
      Assert.assertEquals(commandLine.getOptionValue("bar", "high"), "low");
      Assert.assertEquals(commandLine.getOptionValue("qux", ""), "quux");
      Assert.fail();
    } catch (CommandLineParseException e) {
      Assert.assertTrue(e.getMessage().contains("qux"));
    }
  }

  @Test
  public void testNegativeExtraOption() {
    CommandLine commandLine = new CommandLine(new String[] {"-f", "-b", "low", "-z", "--oops"});
    addAllCommandLineOptions(commandLine);
    try {
      commandLine.parse();
      Assert.fail();
    } catch (CommandLineParseException e) {
      Assert.assertTrue(e.getMessage().contains("oops"));
    }
  }

  @Test
  public void testUsage() {
    CommandLine commandLine = new CommandLine(new String[] {"--foo", "--bar", "low", "--baz", "--qux", "quux"});
    addAllCommandLineOptions(commandLine);
    commandLine.usage("TestCommandLine [options]");
    try {
      commandLine.parse();
    } catch (CommandLineParseException e) {
      Assert.fail("Parser error", e);
    }
  }

  private void addAllCommandLineOptions(CommandLine commandLine) {
    for (CommandLineOption option: options) {
      commandLine.addOption(option);
    }
  }  
  
}
