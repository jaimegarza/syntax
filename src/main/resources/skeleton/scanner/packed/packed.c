  /*
   *
   * Begin of Skeleton
   *
   */

/*/        

    C Skeleton Parser for compact tables

    This is not a sample program, but rather the parser skeleton
    top be included in the generated code.
    Modify at your own risk.

    Copyright (c), 1985-2016 Jaime Garza

/*/

/* Define this as a packed parser */
#define STX_PACKED

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
 * returns the long name of a token, given the token number
 */
char * StxGetTokenFullName(int token) {
    int i;
    for (i = 0; i < TOKENS; i++) {
        if (StxTokenDefs[i].token == token) {
            return StxTokenDefs[i].fullName;
        }
    }
    return "UNKNOWN TOKEN";
}

/*
  This routine maps a state and a token to a new state on the action table  
*/
int StxAction(int state, int sym)
{
    int position = StxParsingTable[state].position;
    int i;

    /* Look in actions if there is a transaction with the token */
    for(i=0; i < StxParsingTable[state].elements; i++)
        if(StxActionTable[position+i].symbol == sym)
            return StxActionTable[position+i].state;
    /* otherwise */
    return StxParsingTable[state].defa;
}

/*
  This routine maps a origin state to a destination state
  using the symbol position 
*/
int StxGoto(int state, int position)
{
    /* Search in gotos if there is a state transition */
    for(; StxGotoTable[position].origin != -1; position++)
        if(StxGotoTable[position].origin == state)
            return StxGotoTable[position].destination;
    /* default */
    return StxGotoTable[position].destination;
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

char errormsgbuffer[1024];
char * StxReplaceErrorMessage(char *s, char *statemsg) {
  char *buff = errormsgbuffer;
  while (*s) {
    if (*s == '$') {
      s++;
      if (*s == 'm') {
        s++;
        while (*statemsg) {
          *buff++ = *statemsg++;
        }
      } else {
        *buff++ = '$';
        *buff++ = *s++;
      }
    } else {
      *buff++ = *s++;
    }
  }
  *buff = '\0';
  return errormsgbuffer;
}

char * StxErrorMessage() {
    short msgIndex = StxParsingTable[StxState].msg;
    char *s;
    if (msgIndex >= 0) {
      s = StxErrorTable[msgIndex];
    } else {
      s = "Syntax error";
    }
    
    int i = pStxStack;
    while (i > 0) {
      int st = sStxStack[i];
      for (int j=0; j<RECOVERS; j++) {
        if(StxAction(st, StxRecoverTable[j]) > 0) {
          char * message = StxGetTokenFullName(StxRecoverTable[j]);
          message = StxReplaceErrorMessage(message, s);
          return message;
        }
      }
      i--;
    }
    
    return s;
}

/*
   Does a shift operation.  Puts a new state on the top of the stack 
*/
int StxShift(int sym, int state)
{
    if(pStxStack >= 149)
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
            StxError(StxState, StxSym, pStxStack, StxErrorMessage());
            return PARSING_ERROR;
        }
    }
}

/*
 give me the available actions that can be taken.  I am also returning reduces.
*/
int * StxValidTokens(int * count) {
    int position = StxParsingTable[StxState].position;

    int * actions = malloc(StxParsingTable[StxState].elements * sizeof(int));
    int index = 0;
    int i;
#ifdef DEBUG
    printf ("Valid actions:[");
#endif
    for(i=0; i < StxParsingTable[StxState].elements; i++) {
#ifdef DEBUG
        if (i > 0) printf(", ");
        printf("%d", StxActionTable[position+i].symbol);
#endif
        actions[index++] = StxActionTable[position+i].symbol;
    }
#ifdef DEBUG
    printf ("]\n");
#endif
    *count = StxParsingTable[StxState].elements;
    return actions;
}

TSTACK StxGetResult() {
    return StxStack[pStxStack];
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
