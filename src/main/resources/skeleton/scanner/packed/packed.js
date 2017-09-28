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
        var position = parsingTable[state].position;
        var i;

        // Look in actions if there is a transaction with the token
        for(i = 0; i < parsingTable[state].elements; i++) {
            if (actionTable[position+i].symbol == symbol) {
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
     * @param position is the position in the goto table
     * @return the next state
     */
    function parserGoto(state, position) {
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
        var msgIndex = parsingTable[state].msg;
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
                parserError(state, lexicalToken, stackTop, getErrorMessage());
                return PARSING_ERROR;
            }
        }
    }

    /**
     * give me the available actions that can be taken.  I am also returning reduces.
     */
    function getValidTokens() {
      var position = parsingTable[state].position;

      var actions = [];
      for(var i=0; i < parsingTable[state].elements; i++) {
          actions.push(actionTable[position+i].symbol);
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
     * Perform a round of tokenization and dump the results. void
     */
    function dumpTokens() {
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
