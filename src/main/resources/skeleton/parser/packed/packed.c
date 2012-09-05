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

    Copyright (c), 1985-2012 Jaime Garza

/*/

/* Define this as a packed parser */
#define STX_PACKED

/* Force an error */
#ifndef STX_ERROR
#define STX_ERROR {ErrorFlag = -1; return FALSE;}
#endif

/* Create generation information if no user code entered */
#ifndef STXCODE_DEFINED
int pStxStack;
TSTACK StxStack[150];

int StxCode(int dummy)
{
    return 1;
}
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

char * StxErrorMessage() {
    short msgIndex = StxParsingTable[StxState].msg;
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
  Main parser routine, uses Shift, Reduce and Recover 
*/
int StxParse(void)
{
    int action;

    pStxStack = 0;
    sStxStack[0] = 0;
    StxChar = StxNextChar();
    StxSym = StxLexer();
    StxState = 0;
    StxErrorFlag = 0;
    StxErrors = 0;

    while(1) {
        action = StxAction(StxState, StxSym);
        if(action == 2147483647) {
#ifdef DEBUG
            printf("Program Accepted\n");
#endif
            return 1;
        }
            
        if(action > 0) {
            if(!StxShift(StxSym, action))
                return 0;
            StxSym = StxLexer();
            if(StxErrorFlag > 0)
                StxErrorFlag--; /* properly recovering from error */
        }
        else if(action < 0) {
            if(!StxReduce(StxSym, -action)){
                if(StxErrorFlag == -1){
                    if(!StxRecover())
                        return 0;
                }else
                    return 0;
            }
        }
        else if(action == 0) {
            if(!StxRecover())
                return 0;
        }
    }
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
