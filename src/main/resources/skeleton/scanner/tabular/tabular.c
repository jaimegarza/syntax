  /*
   *
   * Begin of Skeleton
   *
   */

  /* ****************************************************************
    C Skeleton Parser for matrix tables

    This is not a sample program, but rather the parser skeleton
    to be included in the generated code.
    Modify at your own risk.

    Copyright (c), 1985-2016 Jaime Garza
  ***************************************************************** */

/* Define this as a matrix parser */
#define STX_TABULAR

/* Force an error */
#ifndef STX_ERROR
#define STX_ERROR {ErrorFlag = -1; return FALSE;}
#endif

/* Global variables */
TSTACK            StxValue;               /* Scanner OUT value. Intended for scanner writer */
char              StxChar;                /* The curent character                           */
int               sStxStack[STACK_DEPTH]; /* State stack. Internal use                      */
unsigned long int StxSym;                 /* Actual scanner symbol. Internal usage          */
int               StxState;               /* Current automaton state. Internal usage        */
int               StxErrors;              /* Counts the number of errors.  User can read    */
int               StxErrorFlag;           /* Recuperation machinery state. Internal usage   */

#define ERROR_FAIL 0
#define ERROR_RE_ATTEMPT 1


/* These functions must be provided by the user */
unsigned long int StxLexer();
int StxError(int StxState, int StxSym, int pStxStack, char * message);
#ifdef DEBUG
char * StxToString(TSTACK value);
#endif

  /*
   * ==========================================================
   *                  Regular Expressions
   * ==========================================================
   */
  
int StxEdgeIndex = 0;
  
/*
 * checks one transition
 */
int StxMatchesWholeTransition() {
    int transitionSize = StxEdges[StxEdgeIndex ++];
    int matchesTransition = 0;
    int negate = 0;
    int j;
    int rangeStart;
    int rangeEnd;
    
    if (transitionSize < 0) {
        negate = 1;
        transitionSize = -transitionSize;
    }

    if (transitionSize == 0) { // ANY match
        matchesTransition = StxChar != '\0';
    } else {
        // all elements of one transition
        for (j = 0; j < transitionSize; j++) {
            rangeStart = StxEdges[StxEdgeIndex ++];
            rangeEnd = StxEdges[StxEdgeIndex ++];
            if (StxChar >= rangeStart && StxChar <= rangeEnd) {
                matchesTransition = 1;
                // no break since the new vertex is at the end using StxEdgeIndex
            }
        }
    }
    if (negate) {
        matchesTransition = !matchesTransition;
    }
    return StxChar == '\0' ? 0 : matchesTransition;
  }


/*
 * tries to match a regular expression
 */
int StxMatchesRegex(int vertex) {
    int accept;
    int matches;
    int goOn;
    int numTransitions;
    int matchedOneTransition;
    int matchesTransition;
    int i;
    int newVertex;
    char *p;
    
    StxRecognized[0] = '\0';
    matches = 0;
    goOn = 1;
    
    do {
        accept = 0;
        StxEdgeIndex = StxVertices[vertex];
        if (StxEdgeIndex < 0) {
            accept = 1;
            StxEdgeIndex = -StxEdgeIndex;
        }
        numTransitions = StxEdges[StxEdgeIndex ++];
        matchedOneTransition = 0;
        for (i = 0; i < numTransitions; i++) {
            // each transition
            newVertex = StxEdges[StxEdgeIndex ++];
            matchesTransition = StxMatchesWholeTransition();
            if (matchesTransition) {
                for (p = StxRecognized; *p; p++);
                *p++ = StxChar;
                *p = '\0';
                StxChar = StxNextChar();
                vertex = newVertex;
                matchedOneTransition = 1;
                break; // found a matching transition. new vertex
            }
      }
      if (!matchedOneTransition) {
          if (accept) {
              return 1;
          } else {
              // backtrack characters
              for (p = StxRecognized; *p; p++);
              for (p--; p >= StxRecognized; p--) {
                  StxUngetChar(StxChar);
                  StxChar = *p;
              }
              goOn = 0;
        }
      }
    } while (goOn);
    
    return 0;
}

/*
  This routine maps a state and a token to a new state on the action table  
*/
int StxAction(int state, int symbol)
{
    int index = StxGetTokenIndex(symbol);
    return StxParsingTable[state][index];
}

/*
  This routine maps a origin state to a destination state
  using the symbol position 
*/
int StxGoto(int state, int symbol)
{
    int index = symbol;
    return StxParsingTable[state][index];
}

/*
  This routine prints the contents of the parsing stack 
*/

#ifdef DEBUG
void StxPrintStack()
{
    int i;

    printf("Stack pointer = %d\n", pStxStack);
    printf("States: [");
    for(i=0;i<=pStxStack;i++)
        printf(" %d", sStxStack[i]);
    printf("]<--Top of Stack (%d)\n", pStxStack);
    printf("Values: [");
    for(i=0;i<=pStxStack;i++)
        printf(" %s", StxToString(StxStack[i]));
    printf("]<--Top of Stack (%d)\n", pStxStack);
}
#endif

char * StxErrorMessage() {
    int msgIndex = StxParsingError[StxState];
    if (msgIndex >= 0) {
        return StxErrorTable[msgIndex];
    } else {
        return "Syntax error";
    }
}

/*
   Does a shift operation.  Puts a new state on the top of the stack 
*/
int StxShift(int sym, int state)
{
    if(pStxStack >= STACK_DEPTH-1)
        return 0;

    sStxStack[++pStxStack] = state;
    StxStack[pStxStack] = StxValue;
    StxState = state;
#ifdef DEBUG
    printf("Shift to %d with %d\n", StxState, sym);
    StxPrintStack();
#endif
    return 1;
}

/*
    Recognizes a rule an removes all its elements from the stack
*/
int StxReduce(int sym, int rule)
{
#ifdef DEBUG
    printf("Reduce on rule %d with symbol %d\n", rule, sym);
#endif
    if(!StxCode(rule))
        return 0;
    pStxStack -= StxGrammarTable[rule].reductions;
    sStxStack[pStxStack+1] =
        StxGoto(sStxStack[pStxStack], StxGrammarTable[rule].symbol);
    StxState = sStxStack[++pStxStack];
#ifdef DEBUG
    StxPrintStack();
#endif
    return 1;
}

/*
  Recover from a syntax error removing stack states/symbols, and removing
  input tokens.  The array StxRecover contains the tokens that bound
  the error 
*/
int StxRecover(void)
{
    int i, acc;

    switch(StxErrorFlag){
        case 0: /* 1st error */
            if(!StxError(StxState, StxSym, pStxStack, StxErrorMessage()))
                return 0;
            StxErrors++;
            /* goes into 1 and 2 */

        case 1:
        case 2: /* three attempts are made before dropping the current token */
            StxErrorFlag = 3; /* Remove token */

            while(pStxStack >= 0){
                /* Look if the state on the stack's top has a transition with one of
                  the recovering elements in StxRecoverTable */
                for(i=0; i<RECOVERS; i++)
                    if((acc = StxAction(StxState, StxRecoverTable[i])) > 0)
                        /* valid shift */
                        return StxShift(StxRecoverTable[i], acc);
#ifdef DEBUG
                printf("Recuperate removing state %d and go to state %d\n",
                            StxState, sStxStack[pStxStack-1]);
#endif
                StxState = sStxStack[--pStxStack];
            }
            pStxStack = 0;
            return 0;

        case 3: /* I need to drop the current token */
#ifdef DEBUG
            printf("Recuperate removing symbol %d\n", StxSym);
#endif
            if(StxSym == 0) /* End of input string */
                return 0;
            StxSym = StxLexer();
            return 1; 
    }
}

/*
  Initialize the scanner
*/
void StxInit() {
    pStxStack = 0;
    sStxStack[0] = 0;
    StxState = 0;
}
  
/* 
  Main parser routine, uses Shift, Reduce and Recover 
*/
int StxParse(int symbol, TSTACK value)
{
    int action;
    StxSym = StxGetTokenIndex(symbol);
    StxValue = value;

#ifdef DEBUG
        printf("Starting to parse symbol %d (%d)\n", symbol, StxSym);
        StxPrintStack();
#endif

    while(1 == 1) { // forever with break and return below
        action = StxAction(StxState, symbol);
#ifdef DEBUG
        printf("Action: %d\n", action);
#endif
        if(action == ACCEPT) {
#ifdef DEBUG
            printf("Program Accepted\n");
#endif
            return ACCEPTED;
        }

        if(action > 0) {
            if(StxShift(StxSym, action) == 0) {
                return INTERNAL_ERROR;
            }
            return SHIFTED;
        } else if(action < 0) {
            if(StxReduce(StxSym, -action) == 0) {
                return INTERNAL_ERROR;
            }
        } else if(action == 0) {
            return PARSING_ERROR;
        }
    }
}

/*
 give me the available actions that can be taken.  I am also returning reduces.
*/
int * StxValidTokens(int * count) {
    int c = 0;
    int i;
    for (i = 0; i < TOKENS; i++) {
      if(StxParsingTable[StxState][i] != 0) {
        c ++;
      }
    }
    
    int * actions = malloc(c * sizeof(int));
    int index = 0;
#ifdef DEBUG
    printf ("Valid actions:[");
#endif
    for(i=0; i < TOKENS; i++) {
        if (StxParsingTable[StxState][i] != 0) {
#ifdef DEBUG
            if (index > 0) printf(", ");
            printf("%d", StxTokenDefs[i].token);
#endif
            actions[index++] = StxTokenDefs[i].token;
        }
    }
#ifdef DEBUG
    printf ("]\n");
#endif
    *count = c;
    return actions;
}

TSTACK StxGetResult() {
    return StxStack[pStxStack];
}

/*
 * returns the name of a token, given the token number
 */
char * StxGetTokenName(int token) {
    int i;
    for (i = 0; i < TOKENS; i++) {
        if (StxTokenDefs[i].token == token) {
            return StxTokenDefs[i].name;
        }
    }
    return "UNKNOWN TOKEN";
}

/*
 * Find the index of a token
 */
int StxGetTokenIndex(int token) {
   int i;
   for (i = 0; i < TOKENS; i++) {
       if (StxTokenDefs[i].token == token) {
           return i;
       }
    }
    return -1;
}

/* End of parser */
