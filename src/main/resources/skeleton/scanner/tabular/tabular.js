  /*
   *
   * Begin of Skeleton
   *
   */

    /* ****************************************************************
       Javascript Skeleton Parser for packed tables

       This is not a sample program, but rather the parser skeleton
       to be included in the generated code.
       Modify at your own risk.

       Copyright (c), 1985-2016 Jaime Garza
       ***************************************************************** */

    var ERROR_FAIL = 0;
    var ERROR_RE_ATTEMPT = 1;

    var stateStack = new Array(STACK_DEPTH);
    var state;
    var lexicalValue;
    var lexicalToken;
    var errorCount;
    var errorFlag;
    var verbose = false;

    /**
     * Change the verbose flag
     */
    function setVerbose(v) {
        verbose = v;
    }

    /**
     * Obtain the verbose flag
     * @return true if verbose
     */
    function isVerbose() {
        return verbose;
    }

    /*
     * ==========================================================
     *                  Regular Expressions
     * ==========================================================
     */
  
    var edgeIndex = 0;
  
    /**
     * checks one transition
     */
    function matchesWholeTransition() {
        var transitionSize = edgeTable[edgeIndex ++];
        var negate = false;
        if (transitionSize < 0) {
            negate = true;
            transitionSize = -transitionSize;
        }

        var matchesTransition = false;
        if (transitionSize == 0) { // ANY match
            matchesTransition = currentChar != '\0';
        } else {
            // all elements of one transition
            for (var j = 0; j < transitionSize; j++) {
                var rangeStart = edgeTable[edgeIndex ++];
                var rangeEnd = edgeTable[edgeIndex ++];
                if (currentChar >= String.fromCharCode(rangeStart) && currentChar <= String.fromCharCode(rangeEnd)) {
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
    function matchesRegex(vertex) {
        var accept = false;
        var goOn = true;
    
        recognized = "";
    
        do {
            accept = false;
            edgeIndex = vertexTable[vertex];
            if (edgeIndex < 0) {
                accept = true;
                edgeIndex = -edgeIndex;
            }
            var numTransitions = edgeTable[edgeIndex ++];
            var matchedOneTransition = false;
            for (var i = 0; i < numTransitions; i++) {
                // each transition
                var newVertex = edgeTable[edgeIndex ++];
                var matchesTransition = matchesWholeTransition();
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
                    for (var i = recognized.length() -1; i >= 0; i--) {
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
     * @param token is the number of the token
     * @return the name of a token, given the token number
     */
    function getTokenName(token) {
        for (var i = 0; i < tokenDefs.length; i++) {
            if (tokenDefs[i].token == token) {
                return tokenDefs[i].name;
            }
        }
        if (token < 256) {
            return "\'" + String.fromCharCode(token) + "\'";
        } else {
            return "UNKNOWN TOKEN";
        }
    }

    /**
     * @param token is the number of the token
     * @return the full name of a token, given the token number
     */
    function getTokenFullName(token) {
        for (var i = 0; i < tokenDefs.length; i++) {
            if (tokenDefs[i].token == token) {
                return tokenDefs[i].fullName;
            }
        }
        if (token < 256) {
            return "\'" + String.fromCharCode(token) + "\'";
        } else {
            return "UNKNOWN TOKEN";
        }
    }

    /**
     * @param token is the number of the token
     * @return the name of a token, given the token number
     */
    function getTokenIndex(token) {
        for (var i = 0; i < tokenDefs.length; i++) {
            if (tokenDefs[i].token == token) {
                return i;
            }
        }
        return -1;
    }

    /**
     * This routine maps a state and a token to a new state on the action table
     * @param state is the current state
     * @param symbol is the given symbol to find (if not found, defa will be used
     * @return the parsing action
     */
    function parserAction(state, symbol) {
        var index = getTokenIndex(symbol);
        return parsingTable[state][index];
    }

    /**
     * This routine maps a origin state to a destination state
     * using the symbol position
     * @param state is the current state
     * @param symbol non terminal that causes the transition
     * @return the next state
     */
    function parserGoto(state, symbol) {
        var index = symbol;
        return parsingTable[state][index];
    }

    /**
     * This routine prints the contents of the parsing stack
     */
    function parserPrintStack() {
        var i;

        console.log("States: [");
        for(i=stackTop; i>=0; i--) {
            var ln = "  " + stateStack[i];
            if (i == stackTop) {
                ln = ln + "<--Top Of Stack (" + stackTop + ")";
            }
            console.log(ln);
        }
        console.log("]");
        console.log("Values: [");
        for (i=stackTop; i >=0; i--) {
            var ln = "  " + (stack[i] != null ? stack[i] : "(null)");
            if (i == stackTop) {
                ln = ln + "<--Top Of Stack (" + stackTop + ")";
            }
            console.log(ln);
        }
        console.log("]");
    }

    /**
     * Does a shift operation.  Puts a new state on the top of the stack
     * @param sym is the symbol causing the shift
     * @param state is the current state
     * @return 1 if OK
     */
    function parserShift(sym, st) {
        if(stackTop >= STACK_DEPTH-1) {
            return 0;
        }

        stateStack[++stackTop] = st;
        stack[stackTop] = lexicalValue;
        state = st;
        if (isVerbose()) {
          console.log("Shift to " + state + " with " + sym);
          parserPrintStack();
        }
        return 1;
    }

    /**
     * Recognizes a rule an removes all its elements from the stack
     * @param sym is the symbol causing the shift
     * @param rule is the number of rule being used
     * @return 1 if OK
     */
    function parserReduce(sym, rule) {
        if (isVerbose()) {
            console.log("Reduce on rule " + rule + " with symbol " + sym);
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
     * @return the error message
     */
    function getErrorMessage() {
        var msgIndex = parsingError[state];
        var s;
        if (msgIndex >= 0) {
            s = errorTable[msgIndex];
        } else {
            s = "Syntax error on state " + state + " with token " + getTokenName(lexicalToken);
        }

        var i = stackTop;
        while (i > 0) {
            var st = stateStack[i];
            for (var j=0; j<RECOVERS; j++) {
                if (recoverTable[j] > 0 && parserAction(st, recoverTable[j]) > 0) {
                    var message = getTokenFullName(recoverTable[j]);
                    message = message.replaceAll("\\$m", s);
                    return message;
                }
            }
            i--;
        }
    
        return s;
    }

    /**
     * Recover from a syntax error removing stack states/symbols, and removing
     * input tokens.  The array StxRecover contains the tokens that bound
     * the error
     * @return 1 if OK
     */
    function parserRecover() {
        var i, action;

        switch(errorFlag) {
            case 0: // 1st error
                if(parserError(state, lexicalToken, stackTop, getErrorMessage()) == 0) {
                    return 0;
                }
                errorCount++;
                // continues and goes into 1 and 2.  No break on purpose

            case 1:
            case 2: // three attempts are made before dropping the current token
                errorFlag = 3; // Remove token

                while(stackTop > 0) {
                    // Look if the state on the stack's top has a transition with one of
                    // the recovering elements in StxRecoverTable
                    for (i=0; i<RECOVERS; i++) {
                        action = parserAction(state, recoverTable[i]);
                        if(action > 0) {
                            // valid shift
                            return parserShift(recoverTable[i], action);
                        }
                    }
                    if (isVerbose()) {
                        console.log("Recuperate removing state " + state + " and going to state " +
                            stack[stackTop-1]);
                    }
                    state = stateStack[--stackTop];
                }
                stackTop = 0;
                return 0;

            case 3: // I need to drop the current token
                if (isVerbose()) {
                    console.log("Recuperate removing symbol " + lexicalToken);
                }
                if(lexicalToken == 0) { // end of file
                    return 0;
                }
                lexicalToken = parserElement(false);
                return 1;
        }
        // should never reach
        console.log("ASSERTION FAILED ON PARSER");
        return 0;
    }

    /**
     * initialize the parser.  Caller (or constructor) must call it
     */
    function init() {
      stackTop = 0;
      stateStack[0] = 0;
      stack[0] = null;
      state = 0;
    }
    
    var ACCEPTED = 1;
    var SHIFTED = 2;
    var PARSING_ERROR = 3;
    var INTERNAL_ERROR = 4;

    /**
     * send and parse one token.  main routine of the scanner driven recognizer
     */
    function parse(symbol, value) {
        var action;
        lexicalToken = getTokenIndex(symbol);
        lexicalValue = value;

        if (isVerbose()) {
            console.log("Starting to parse symbol " + symbol + "(" + lexicalToken + ":" + lexicalValue + ")");
            parserPrintStack();
        }

        while(2 != 1) { // forever with break and return below
            action = parserAction(state, symbol);
            if (isVerbose()) {
                console.log("Action: " + action);
            }
            if (action == ACCEPT) {
                if (isVerbose()) {
                    console.log("Program Accepted");
                }
                return ACCEPTED;
            }

            if (action > 0) {
                if (parserShift(lexicalToken, action) == 0) {
                    return INTERNAL_ERROR;
                }
                return SHIFTED;
            } else if(action < 0) {
                if (parserReduce(lexicalToken, -action) == 0) {
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
    function getValidTokens() {
        var actions = [];
        for (var i = 0; i < TOKENS; i++) {
            if (parsingTable[state][i] != 0) {
                actions.push(tokenDefs[i].token);
            }
        }
        return actions;
}

    /**
     * @return the current lexical value
     */
    function getResult() {
        return stack[stackTop];
    }

    /**
     * Perform a round of tokenization and dump the results
     */
    function dumpTokens() {
        lexicalToken = parserElement(true);
        lexicalValue = null;
        while (lexicalToken != 0) {
            console.log("Token: " + getTokenName(lexicalToken) + "(" + lexicalToken + "):" + (lexicalValue == null? "null": lexicalValue));
            lexicalValue = null;
            lexicalToken = parserElement(false);
        }
    }

    function findReservedWord(word) {
        for (var i = 0; i < tokenDefs.length; i++) {
            if (tokenDefs[i].reserved && tokenDefs[i].name === word) {
                return tokenDefs[i].token;
            }
        }
        return -1;
    }

    function findReservedWordIgnoreCase(word) {
        for (var i = 0; i < tokenDefs.length; i++) {
            if (tokenDefs[i].reserved && tokenDefs[i].name.toUpperCase() === word.toUpperCase()) {
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
