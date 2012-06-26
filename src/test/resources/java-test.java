public class TestParser {
  
  private class ReservedWord {
    String word;
    int    token;
    
    ReservedWord(String word, int token) {
      this.word = word;
      this.token = token;
    }
  }

  private ReservedWord reservedWord[] = {
    new ReservedWord("AND",   TOK_AND),
    new ReservedWord("OR",    TOK_OR),
    new ReservedWord("NOT",   TOK_NOT)
  };
  
  int findReservedWord(String word) {
    for (int i = 0; i < reservedWord.length; i++) {
      if (reservedWord[i].word.equalsIgnoreCase(word)) {
        return reservedWord[i].token;
      }
    }
    return 0;
  }
  


  // LexicalRecognizer
  private char currentChar;

  int parserElement(boolean initialize) {
    lexicalValue = new LexicalValue();

       
        while (currentChar <= ' ' && currentChar > '\0') {
          currentChar = getNextChar(false);
        }
      
        if (currentChar == '&') {currentChar = getNextChar(false); return TOK_AND;};
        if (currentChar == '|') {currentChar = getNextChar(false); return TOK_OR;};
        if (currentChar == '!') {currentChar = getNextChar(false); return TOK_NOT;};
       
        if (currentChar == '=') {
          currentChar = getNextChar(false); 
          return TOK_EQ;
        }
        if (currentChar == '<') {
          currentChar = getNextChar(false);
          if (currentChar == '=') {
            currentChar = getNextChar(false);
            return TOK_LE;
          }
          if (currentChar == '>') {
            currentChar = getNextChar(false);
            return TOK_NE;
          }
          return TOK_LT;
        }
        if (currentChar == '>') {
          currentChar = getNextChar(false);
          if (currentChar == '=') {
            currentChar = getNextChar(false);
            return TOK_GE;
          }
          return TOK_GT;
        }  
      
        if (currentChar == '+') {currentChar = getNextChar(false); return '+';};
        if (currentChar == '-') {currentChar = getNextChar(false); return '-';};
        if (currentChar == '*') {currentChar = getNextChar(false); return '*';};
        if (currentChar == '/') {currentChar = getNextChar(false); return '/';};
        if (currentChar == '(') {currentChar = getNextChar(false); return '(';};
        if (currentChar == ')') {currentChar = getNextChar(false); return ')';};
       
        if (currentChar >= '0' && currentChar <= '9') {
          int number = 0;
          while (currentChar >= '0' && currentChar <= '9') {
            number = number * 10 + currentChar - '0';
            currentChar = getNextChar(false);
          }
          lexicalValue.number = number;
          return TOK_NUMBER;
        }
      
       
      

    return 0; // UNKNOWN
  }

  // Code Generator
  private static final int STACK_DEPTH = 5000;
  LexicalValue stack[] = new LexicalValue[STACK_DEPTH];
  int stackTop;

  int generateCode(int rule) {
    switch(rule){

      case 1:        stack[stackTop-2].number = (stack[stackTop-2] != 0) && (stack[stackTop] != 0) ? 1 : 0;
        break;
      case 2:        stack[stackTop-2].number = (stack[stackTop-2] != 0) || (stack[stackTop] != 0) ? 1 : 0;
        break;
      case 3:        stack[stackTop-1].number = !(stack[stackTop] != 0) ? 1 : 0;
        break;
      case 4:        stack[stackTop-2].number = stack[stackTop-2] <= stack[stackTop] ? 1 : 0;
        break;
      case 5:        stack[stackTop-2].number = stack[stackTop-2] < stack[stackTop] ? 1 : 0;
        break;
      case 6:        stack[stackTop-2].number = stack[stackTop-2] >= stack[stackTop] ? 1 : 0;
        break;
      case 7:        stack[stackTop-2].number = stack[stackTop-2] > stack[stackTop] ? 1 : 0;
        break;
      case 8:        stack[stackTop-2].number = stack[stackTop-2] != stack[stackTop] ? 1 : 0;
        break;
      case 9:        stack[stackTop-2].number = stack[stackTop-2] == stack[stackTop] ? 1 : 0;
        break;
      case 10:        stack[stackTop-2].number = stack[stackTop-2] + stack[stackTop];
        break;
      case 11:        stack[stackTop-2].number = stack[stackTop-2] - stack[stackTop];
        break;
      case 12:        stack[stackTop-2].number = stack[stackTop-2] * stack[stackTop];
        break;
      case 13:        stack[stackTop-2].number = stack[stackTop-2] / stack[stackTop];
        break;
      case 14:        stack[stackTop-1].number = -stack[stackTop];
        break;
      case 15:        stack[stackTop-2].number = stack[stackTop-1];
        break;

    }
    return 1; // OK
  }

  private static final int RECOVERS=0;

  // Contains tokens in compact mode, and column in matrix
  int recoverTable[] = {0};
    0,
    256,
    257,
    258,
    259,
    260,
    261,
    262,
    263,
    264,
    43,
    45,
    42,
    47,
    32768,
    40,
    41,
    32769
  };

