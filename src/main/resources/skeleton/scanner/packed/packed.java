  /*
   *
   * Begin of Skeleton
   *
   */

  /* ****************************************************************
    Java Skeleton Parser for packed tables

    This is not a sample program, but rather the parser skeleton
    to be included in the generated code.
    Modify at your own risk.

    Copyright (c), 1985-2016 Jaime Garza
  ***************************************************************** */

  private static final int ERROR_FAIL = 0;
  private static final int ERROR_RE_ATTEMPT = 1;

  private int    stateStack[] = new int[STACK_DEPTH];
  int            state;
  StackElement   lexicalValue;
  int            lexicalToken;
  boolean        verbose = false;

  /**
   * Change the verbose flag
   */
  public void setVerbose(boolean verbose) {
    this.verbose = verbose;
  }

  /**
   * Obtain the verbose flag
   */
  public boolean isVerbose() {
    return this.verbose;
  }

  /*
   * ==========================================================
   *                  Regular Expressions
   * ==========================================================
   */
  
  private int edgeIndex = 0;
  
  /**
   * checks one transition
   */
  private boolean matchesWholeTransition() {
    int transitionSize = edgeTable[edgeIndex ++];
    boolean negate = false;
    if (transitionSize < 0) {
      negate = true;
      transitionSize = -transitionSize;
    }

    boolean matchesTransition = false;
    if (transitionSize == 0) { // ANY match
      matchesTransition = currentChar != '\0';
    } else {
      // all elements of one transition
      for (int j = 0; j < transitionSize; j++) {
        int rangeStart = edgeTable[edgeIndex ++];
        int rangeEnd = edgeTable[edgeIndex ++];
        if (currentChar >= rangeStart && currentChar <= rangeEnd) {
          matchesTransition = true;
          // no break since the new vertex is at the end using edgeIndex
        }
      }
    }
    if (negate) {
      matchesTransition = !matchesTransition;
    }
    return currentChar == '\0' ? false : matchesTransition;
  }
  
  /**
   * tries to match a regular expression
   */
  private boolean matchesRegex(int vertex) {
    boolean accept = false;
    boolean goOn = true;
    
    recognized = "";
    
    do {
      accept = false;
      edgeIndex = vertexTable[vertex];
      if (edgeIndex < 0) {
        accept = true;
        edgeIndex = -edgeIndex;
      }
      int numTransitions = edgeTable[edgeIndex ++];
      boolean matchedOneTransition = false;
      for (int i = 0; i < numTransitions; i++) {
        // each transition
        int newVertex = edgeTable[edgeIndex ++];
        boolean matchesTransition = matchesWholeTransition();
        if (matchesTransition) {
          recognized += currentChar;
          currentChar = getNextChar(false);
          vertex = newVertex;
          matchedOneTransition = true;
          break; // found a matching transition. new vertex
        }
      }
      if (!matchedOneTransition) {
        if (accept) {
          return true;
        } else {
          // backtrack characters
          for (int i = recognized.length() -1; i >= 0; i--) {
            ungetChar(currentChar);
            currentChar = recognized.charAt(i);
          }
          goOn = false;
        }
      }
    } while (goOn);
    
    return false;
 }

  /**
   * This routine maps a state and a token to a new state on the action table
   * @param state is the current state
   * @param sym is the given symbol to find (if not found, defa will be used
   */
  private int parserAction(int state, int sym) {
    int position = parsingTable[state].position;
    int i;

    // Look in actions if there is a transaction with the token
    for(i=0; i < parsingTable[state].elements; i++) {
      if(actionTable[position+i].symbol == sym) {
        return actionTable[position+i].state;
      }
    }
    // otherwise
    return parsingTable[state].defa;
  }

  /**
   * This routine maps a origin state to a destination state
   * using the symbol position
   * @param state is the current state
   *@param position is the position in the goto table
   */
  private int parserGoto(int state, int position) {
    // Search in gotos if there is a state transition
    for(; gotoTable[position].origin != -1; position++) {
        if(gotoTable[position].origin == state) {
            return gotoTable[position].destination;
        }
    }
    // default
    return gotoTable[position].destination;
  }

  /**
   * This routine prints the contents of the parsing stack
   */
  private void parserPrintStack() {
    int i;

    System.out.println("States: [");
    for(i=stackTop; i>=0; i--) {
      System.out.print("  " + stateStack[i]);
      if (i == stackTop) {
        System.out.println("<--Top Of Stack (" + stackTop + ")");
      }
      else {
        System.out.println();
      }
    }
    System.out.println("]");
    System.out.println("Values: [");
    for(i=stackTop;i >=0; i--) {
      System.out.print("  " + (stack[i] != null ? stack[i].toString() : "(null)"));
      if (i == stackTop) {
        System.out.println("<--Top Of Stack (" + stackTop + ")");
      }
      else {
        System.out.println();
      }
    }
    System.out.println("]");
  }

  /**
   * Does a shift operation.  Puts a new state on the top of the stack
   * @param sym is the symbol causing the shift
   * @param state is the current state
   */
  private int parserShift(int sym, int state) {
    if(stackTop >= STACK_DEPTH-1) {
      return 0;
    }

    stateStack[++stackTop] = state;
    stack[stackTop] = lexicalValue;
    this.state = state;
    if (isVerbose()) {
      System.out.println("Shift to " + state + " with " + sym);
      parserPrintStack();
    }
    return 1;
  }

  /**
   * Recognizes a rule an removes all its elements from the stack
   * @param sym is the symbol causing the shift
   * @param rule is the number of rule being used
   */
  int parserReduce(int sym, int rule) {
    if (isVerbose()) {
      System.out.println("Reduce on rule " + rule + " with symbol " + sym);
    }
    if(generateCode(rule) == false) {
      return 0;
    }
    stackTop -= grammarTable[rule].reductions;
    stateStack[stackTop+1] =
        parserGoto(stateStack[stackTop], grammarTable[rule].symbol);
    state = stateStack[++stackTop];
    if (isVerbose()) {
        parserPrintStack();
    }
    return 1;
  }

  /**
   * Get the error message for a state
   */
  private String getErrorMessage() {
    int msgIndex = parsingTable[state].msg;
    String s;
    if (msgIndex >= 0) {
      s = errorTable[msgIndex];
    } else {
      s = "Syntax error on state " + state + " with token " + getTokenName(lexicalToken);
    }

    int i = stackTop;
    while (i > 0) {
      int st = stateStack[i];
      for (int j=0; j<RECOVERS; j++) {
        if(parserAction(st, recoverTable[j]) > 0) {
          String message = getTokenFullName(recoverTable[j]);
          message = message.replaceAll("\\$m", s);
          return message;
        }
      }
      i--;
    }
    
    return s;
  }

  /**
   * initialize the parser.  Caller (or constructor) must call it
   */
  public void init() {
    stackTop = 0;
    stateStack[0] = 0;
    stack[0] = null;
    state = 0;
  }
  
  public static final int ACCEPTED = 1;
  public static final int SHIFTED = 2;
  public static final int PARSING_ERROR = 3;
  public static final int INTERNAL_ERROR = 4;
  
  /**
   * send and parse one token.  main routine of the scanner driven recognizer
   */
  public int parse(int symbol, StackElement value) {
    int action;
    lexicalToken = getTokenIndex(symbol);
    lexicalValue = value;

    if (isVerbose()) {
      System.out.println("Starting to parse symbol " + symbol + "(" + lexicalToken + ":" + lexicalValue.toString() + ")");
      parserPrintStack();
    }

    while(2 != 1) { // forever with break and return below
      action = parserAction(state, symbol);
      if (isVerbose()) {
        System.out.println("Action: " + action);
      }
      if(action == ACCEPT) {
        if (isVerbose()) {
          System.out.println("Program Accepted");
        }
        return ACCEPTED;
      }

      if(action > 0) {
        if(parserShift(lexicalToken, action) == 0) {
          return INTERNAL_ERROR;
        }
        return SHIFTED;
      } else if(action < 0) {
        if(parserReduce(lexicalToken, -action) == 0) {
          return INTERNAL_ERROR;
        }
      } else if(action == 0) {
        return PARSING_ERROR;
      }
    }
  }
  
  /**
   * give me the available actions that can be taken.  I am also returning reduces.
   */
  public int[] getValidTokens() {
    int position = parsingTable[state].position;

    int actions[] = new int[parsingTable[state].elements];
    int index = 0;
    for(int i=0; i < parsingTable[state].elements; i++) {
      actions[index++] = actionTable[position+i].symbol;
    }
    return actions;
  }

  /**
   * @returns the current lexical value
   */
  public StackElement getResult() {
    return stack[stackTop];
  }

  /**
   * @param token is the number of the token
   * @returns the name of a token, given the token number
   */
  public String getTokenName(int token) {
    for (int i = 0; i < tokenDefs.length; i++) {
      if (tokenDefs[i].token == token) {
        return tokenDefs[i].name;
      }
    }
    if (token < 256) {
      return "\'" + (char) token + "\'";
    } else {
      return "UNKNOWN TOKEN";
    }
  }

  /**
   * @param token is the number of the token
   * @return the full name of a token, given the token number
   */
  public String getTokenFullName(int token) {
    for (int i = 0; i < tokenDefs.length; i++) {
      if (tokenDefs[i].token == token) {
        return tokenDefs[i].fullName;
      }
    }
    if (token < 256) {
      return "\'" + (char) token + "\'";
    } else {
      return "UNKNOWN TOKEN";
    }
  }

  /**
   * @param token is the number of the token
   * @returns the name of a token, given the token number
   */
  public int getTokenIndex(int token) {
    for (int i = 0; i < tokenDefs.length; i++) {
      if (tokenDefs[i].token == token) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Perform a round of tokenization and dump the results
   */
  public void dumpTokens() {
    lexicalToken = parserElement(true);
    lexicalValue = null;
    while (lexicalToken != 0) {
      System.out.println("Token: " + getTokenName(lexicalToken) + "(" + lexicalToken + "):" + (lexicalValue == null? "null": lexicalValue.toString()));
      lexicalValue = null;
      lexicalToken = parserElement(false);
    }
  }

  int findReservedWord(String word) {
    for (int i = 0; i < tokenDefs.length; i++) {
      if (tokenDefs[i].reserved && tokenDefs[i].name.equals(word)) {
        return tokenDefs[i].token;
      }
    }
    return -1;
  }

  int findReservedWordIgnoreCase(String word) {
    for (int i = 0; i < tokenDefs.length; i++) {
      if (tokenDefs[i].reserved && tokenDefs[i].name.equalsIgnoreCase(word)) {
        return tokenDefs[i].token;
      }
    }
    return -1;
  }

  /*
   *
   * End of packed skeleton for java
   *
   */

