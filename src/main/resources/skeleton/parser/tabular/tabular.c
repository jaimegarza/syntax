/*

    Pascal Skeleton Parser for matrix tables

    This is not a sample program, but rather the parser skeleton
    top be included in the generated code.
    Modify at your own risk.

    Copyright (c), 1985-199 Jaime Garza V zquez

*/

/* Define this as an unpacked parser */
#define STX_UNPACKED

/* Force an error */
#ifndef STX_ERROR
#define STX_ERROR {ErrorFlag = -1; return FALSE;}
#endif

/* Define stack type if not defined by the user */
#ifndef TSTACK
#define TSTACK int
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
TSTACK  StxValue;       /* Scanner OUT value. Intended for scanner writer */
int     sStxStack[150]; /* State stack. Internal use                      */
int     StxSym;         /* Actual scanner symbol. Internal usage          */
int     StxState;       /* Current automaton state. Internal usage        */
int     StxErrors;      /* Counts the number of errors.  User can read    */
int     StxErrorFlag;   /* Recuperation machinery state. Internal usage */

/* These functions must be provided by the user */
int StxScan(void);
int StxError(int StxState, int StxSym, int pStxStack);

/*
    This function calls the user provided scanner routine
    Converts the returned token intio the equivalent column number for parser
    matrix.

    The StxTokens contains the tokens available
*/
int StxScanner(void)
{
    int sym = StxScan();
    int i;

    for(i=0; i<TOKENS; i++)
        if(sym == StxTokens[i])
            break;
    if(i == TOKENS)
        return 0;
    else
        return i;
}

/*
  This routine prints the contents of the parsing stack
*/

#ifdef DEBUG
void PrintStack()
{
    int i;

    printf("Stack pointer = %d ErrorFlag = %d\n", pStxStack, StxErrorFlag);
    printf("States: [");
    for(i=0;i<=pStxStack;i++)
        printf(" %d", sStxStack[i]);
    printf("]\n");
}
#endif

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
    PrintStack();
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
        StxParsingTable  [sStxStack[pStxStack]]
            [StxGrammarTable[rule].symbol];
    StxState = sStxStack[++pStxStack];
#ifdef DEBUG
    PrintStack();
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
            if(!StxError(StxState, StxSym, pStxStack))
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
                    if((acc = StxParsingTable[StxState][StxRecoverTable[i]]) > 0)
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
            StxSym = StxScanner();
            return 1;
    }
}

/*
  Main parser routine, uses Shift, Reduce and Recover
*/
int StxParse(void)
{
    pStxStack = 0;
    sStxStack[0] = 0;
    StxSym = StxScanner();
    StxState = 0;
    StxErrorFlag = 0;
    StxErrors = 0;

    while(1){
        if(StxParsingTable[StxState][StxSym] == 9999) {
#ifdef DEBUG
            printf("Program Accepted\n");
#endif
            return 1;
        }

        if(StxParsingTable[StxState][StxSym] > 0) {
            if(!StxShift(StxSym, StxParsingTable[StxState][StxSym]))
                return 0;
            StxSym = StxScanner();
            if(StxErrorFlag > 0)
                StxErrorFlag--; /* properly recovering from error */
        }

        if(StxParsingTable[StxState][StxSym] < 0){
            if(!StxReduce(StxSym, -StxParsingTable[StxState][StxSym])){
                if(StxErrorFlag == -1){
                    if(!StxRecover())
                        return 0;
                }else
                    return 0;
                return 0;
            }
        }

        if(StxParsingTable[StxState][StxSym] == 0){
            if(!StxRecover())
                return 0;
        }
    }
}

/* End of parser */
