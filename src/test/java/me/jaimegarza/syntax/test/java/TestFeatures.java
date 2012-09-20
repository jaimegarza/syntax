package me.jaimegarza.syntax.test.java;

import java.io.IOException;

import org.testng.annotations.Test;

import me.jaimegarza.syntax.AnalysisException;
import me.jaimegarza.syntax.OutputException;
import me.jaimegarza.syntax.ParsingException;
import me.jaimegarza.syntax.language.Language;
import me.jaimegarza.syntax.test.AbstractGenerationBase;

public class TestFeatures extends AbstractGenerationBase {

  @Test
  public void testDeclareTokens() throws IOException, ParsingException, AnalysisException, OutputException {
    setUp(Language.java, "Tokens");
    generateLanguageFile(new String[] {
        "--algorithm",
        "l",
        "--language",
        "java",
        "--driver",
        "scanner",
        "--packing",
        "tabular",
        //"-v",
        //"-g",
        "classpath:tokens.sy",
        "${file.language}",
        "${file.include}",
        "${file.grammar}"
    });
    checkRegularExpressions(tmpGrammarFile, new String[] {
        " a .*One A.*256.*N/A",
        " b .*b.*257.*N/A",
        " c .*c.*32768.*N/A",
        " d .*d.*32769.*N/A",
        " e .*e.*300.*LEF",
        " f .*f.*301.*LEF",
        " g .*g.*302.*RIG",
        " h .*h.*303.*RIG",
        " i .*i.*304.*BIN",
        " j .*j.*305.*BIN",
    });
    tearDown();
  }

  @Test
  public void testTypes() throws IOException, ParsingException, AnalysisException, OutputException {
    setUp(Language.java, "Types");
    generateLanguageFile(new String[] {
        "--algorithm",
        "l",
        "--language",
        "java",
        "--driver",
        "scanner",
        "--packing",
        "tabular",
        //"-v",
        //"-g",
        "classpath:types.sy",
        "${file.language}",
        "${file.include}",
        "${file.grammar}"
    });
    checkRegularExpressions(tmpGrammarFile, new String[] {
        " a .*a.*32768.*N/A.*type1",
        " b .*b.*32769.*N/A.*type2",
        " c .*c.*32770.*N/A.*type1",
        "Expr.*type1",
        "Term.*type2",
    });
    checkRegularExpressions(tmpLanguageFile, new String[] {
        "case 1:.*stackTop.\\.type1.*=.*stackTop.\\.type1",
        "case 2:.*stackTop-2.\\.type1.*stackTop-2.\\.type1.*stackTop-1.\\.type1.*stackTop.\\.type2",
        "case 3:.*stackTop-2.\\.type1.*stackTop-2.\\.type2.*stackTop-1.\\.type2.*stackTop.\\.type2",
        "case 4:.*stackTop.\\.type2.*stackTop.\\.type1",
    });
    tearDown();
  }
}
