  (*
   *
   * Begin of Skeleton
   *
   *)

  (* ****************************************************************
    Pascal Skeleton Parser FOR matrix tables

    This is not a sample program, but rather the parser skeleton
    to be included in the generated code.
    Modify at your own risk.

    Copyright (c), 1985-2016 Jaime Garza
  ***************************************************************** *)
CONST
  ERROR_FAIL = 0;
  ERROR_RE_ATTEMPT = 1;

(* Global variables *)
VAR
    sStxStack   : Array[0..STACK_DEPTH] of integer; (* SState stack. Internal use                     *)
    StxSym      : LongInt;                          (* Actual scanner symbol. Internal usage          *)
    StxState    : integer;                          (* Current automaton state. Internal usage        *)
    StxErrors   : Integer;                          (* Counts the number of errors.  User can read    *)
    StxErrorFlag: integer;                          (* Recuperation machinery state. Internal usage   *)

(* These functions must be provided by the user *)
FUNCTION StxError(StxState:INTEGER; StxSym:INTEGER; pStxStack:INTEGER; aMessage:STRING):INTEGER; FORWARD;
{$IFDEF DEBUG}
FUNCTION StxToString(value:TSTACK):STRING; FORWARD;
{$ENDIF}

(*
 * ==========================================================
 *                  Regular Expressions
 * ==========================================================
*)
  
VAR
    StxEdgeIndex:INTEGER = 0;
  
(*
  checks one transition
*)
FUNCTION StxMatchesWholeTransition:BOOLEAN;
VAR
    transitionSize:INTEGER;
    negate:BOOLEAN;
    matchesTransition:BOOLEAN;
    j:INTEGER;
    rangeStart:CHAR;
    rangeEnd:CHAR;
BEGIN
    transitionSize := StxEdges[StxEdgeIndex];
    StxEdgeIndex := StxEdgeIndex + 1;
    negate := false;
    if   transitionSize < 0 
    then begin
         negate := true;
        transitionSize := -transitionSize;
    end;

    matchesTransition := false;
    if   transitionSize = 0
    then begin (* ANY match *)
         matchesTransition := StxChar <> CHR(0);
    end
    else begin
         (* all elements of one transition *)
         for j := 0 to transitionSize-1 do 
         begin
            rangeStart := CHR(StxEdges[StxEdgeIndex]);
            rangeEnd := CHR(StxEdges[StxEdgeIndex + 1]);
            StxEdgeIndex := StxEdgeIndex + 2;
            if   (StxChar >= rangeStart) AND (StxChar <= rangeEnd)
            then matchesTransition := true;
         end; (*FOR*)
    end;
    
    if   negate
    then matchesTransition := NOT matchesTransition;
    
    if   StxChar = CHR(0)
    then StxMatchesWholeTransition := false
    else StxMatchesWholeTransition := matchesTransition
END;
  
(*
  tries to match a regular expression
*)
FUNCTION StxMatchesRegex(vertex:INTEGER):BOOLEAN;
VAR
    accept:BOOLEAN;
    stop:BOOLEAN;
    numTransitions:INTEGER;
    matchedOneTransition:BOOLEAN;
    i:INTEGER;
    newVertex:INTEGER;
    matchesTransition:BOOLEAN;
BEGIN
    accept := false;
    stop := false;
    
    StxRecognized := '';
    
    repeat
      accept := false;
      StxEdgeIndex := StxVertices[vertex];
      if   StxEdgeIndex < 0
      then begin
           accept := true;
           StxEdgeIndex := -StxEdgeIndex;
      end;
      
      numTransitions := StxEdges[StxEdgeIndex];
      StxEdgeIndex := StxEdgeIndex + 1;
      matchedOneTransition := false;
      for i := 0 to numTransitions-1 do
      begin
        (* each transition *)
        newVertex := StxEdges[StxEdgeIndex];
        StxEdgeIndex := StxEdgeIndex + 1;
        matchesTransition := StxMatchesWholeTransition;
        if   matchesTransition
        then begin
             StxRecognized := StxRecognized + StxChar;
             StxChar := StxNextChar;
             vertex := newVertex;
             matchedOneTransition := true;
             break; (* found a matching transition. new vertex *)
        end;
      end;
      
      if   NOT matchedOneTransition
      then begin
        if   accept
        then begin
             exit(true);
             end
        else begin
          (* backtrack characters *)
          for i := LENGTH(StxRecognized) DOWNTO 1 do
          begin
            StxUngetChar(StxChar);
            StxChar := StxRecognized[i];
          end;
          stop := true;
        end;
      end;
    until stop;
    
    StxMatchesRegex := false;
END;

(*
    returns the name of a token, given the token number
*)
FUNCTION StxGetTokenName(token:INTEGER) : STRING;
VAR
    i : INTEGER;
BEGIN
    FOR i := 0 TO TOKENS-1 DO
        BEGIN
        IF   StxTokenDefs[i].token = token
        THEN BEGIN
             StxGetTokenName := StxTokenDefs[i].name;
             EXIT;
             END;
        END;
    StxGetTokenName := 'UNKNOWN TOKEN';
END;

(*
    Find the index of a token
*)
FUNCTION StxGetTokenIndex(token:LONGINT) : INTEGER;
VAR
    i : INTEGER;
BEGIN
    FOR i := 0 TO TOKENS-1 DO
        BEGIN
        IF StxTokenDefs[i].token = token 
        THEN BEGIN
             StxGetTokenIndex := i;
             EXIT;
             END;
        END;
    StxGetTokenIndex := -1;
END;

(*
  This routine maps a state and a token to a new state on the action table  
*)
FUNCTION StxAction(state:INTEGER; symbol:LONGINT) : LONGINT;
VAR
    index : INTEGER;
BEGIN
    index := StxGetTokenIndex(symbol);
    StxAction := StxParsingTable[state][index];
END;

(*
  This routine maps a origin state to a destination state
  using the symbol position 
*)
FUNCTION StxGoto(state:INTEGER; symbol:INTEGER): INTEGER;
VAR
    index : INTEGER;
BEGIN
    index := symbol;
    StxGoTo := StxParsingTable[state][index];
END;

(*
  This routine prints the contents of the parsing stack 
*)
{$IFDEF DEBUG}
PROCEDURE StxPrintStack;
VAR
    i:integer;
BEGIN
    writeln('Stack pointer = ', pStxStack);
    write('States: [');
    FOR i:=0 to pStxStack DO
        write(sStxStack[i], ' ');
    writeln(']<--Top Of Stack (', pStxStack, ')');
    write('Values: [');
    FOR i:=0 to pStxStack DO
        write('|', StxToString(StxStack[i]),'| ');
    writeln(']<--Top Of Stack (', pStxStack, ')');
END;
{$ENDIF}

(*
    Get the error message FOR the current state
*)
FUNCTION StxErrorMessage: STRING;
VAR
    msgIndex : INTEGER;
BEGIN
    msgIndex := StxParsingError[StxState];
    IF   msgIndex >= 0
    THEN StxErrorMessage := StxErrorTable[msgIndex]
    ELSE StxErrorMessage := 'Syntax error';
END;

(*
   Does a shift operation.  Puts a new state on the top of the stack 
*)
FUNCTION StxShift(sym:LongInt; state:integer):BOOLEAN;
BEGIN
    IF   pStxStack >= STACK_DEPTH-1
    THEN StxShift := FALSE
    ELSE BEGIN
         pStxStack := pStxStack + 1;
         sStxStack[pStxStack] := state;
         StxStack[pStxStack] := StxValue;
         StxState := state;
         StxShift := TRUE;
{$IFDEF DEBUG}
         writeln('Shift to ', state, ' with ', sym);
         StxPrintStack;
{$ENDIF}
         END;
END;

(*
    Recognizes a rule an removes all its elements from the stack
*)
FUNCTION StxReduce(sym:LongInt; rule:integer):BOOLEAN;
BEGIN
{$IFDEF DEBUG}
    writeln('Reduce on rule ', rule, ' with symbol ', sym);
{$ENDIF}
    IF   Not StxCode(rule)
    THEN StxReduce := FALSE
    ELSE BEGIN
         pStxStack := pStxStack - StxGrammarTable[rule].reductions;
         sStxStack[pStxStack+1] :=
            StxGoto(sStxStack[pStxStack], StxGrammarTable[rule].symbol);
         pStxStack := pStxStack+1;
         StxState := sStxStack[pStxStack];
         StxReduce := TRUE;
{$IFDEF DEBUG}
         StxPrintStack;
{$ENDIF}
         END;
END;

(*
    Recover from a syntax error removing stack states/symbols, and removing
    input tokens.  The array StxRecover contains the tokens that bound
    the error 
*)
FUNCTION StxRecover: BOOLEAN;
VAR
    i, acc : INTEGER;
    found  : BOOLEAN;
BEGIN
    StxRecover := TRUE;
    CASE StxErrorFlag OF
        0, 1, 2: (* three attempts before dropping the symbol *)
            BEGIN
            IF   StxErrorFlag = 0
            THEN BEGIN
                 IF   StxError(StxState, StxSym, pStxStack, StxErrorMessage()) = ERROR_FAIL 
                 THEN BEGIN
                      StxRecover := FALSE;
                      EXIT;
                      END;
                 END;

            StxErrorFlag := 3; (* remove the symbol *)

            WHILE pStxStack >= 0 DO
                BEGIN
                (* Look if the state on the stack's top has a transition with one of
                  the recovering elements in StxRecoverTable *)
                found := FALSE;
                FOR i:=0 to RECOVERS-1 DO
                    BEGIN
                    acc := StxAction(StxState, StxRecoverTable[i]);
                    IF   acc > 0 (* shift valido *)
                    THEN BEGIN
                         StxRecover := StxShift(StxRecoverTable[i], acc);
                         found := TRUE;
                         EXIT;
                         END;
                    END;
                IF   NOT found
                THEN BEGIN
{$IFDEF DEBUG}
                     writeln('Recover removing state ', StxState,
                             ' and go to state ', sStxStack[pStxStack-1]);
{$ENDIF}
                     pStxStack := pStxStack - 1;
                     StxState := sStxStack[pStxStack];
                     END; (*IF*)
                END; (*WHILE*)
                pStxStack := 0;
                StxRecover := FALSE;
            END; (*CASE 0, 1 y 2*)

        3: (* I need to drop the current token *)
            BEGIN
{$IFDEF DEBUG}
            writeln('Recover removing symbol ', StxSym);
{$ENDIF}
            IF   StxSym = 0 (* End of input string *)
            THEN StxRecover := FALSE
            ELSE BEGIN
                 StxSym := StxLexer;
                 StxRecover := TRUE;
                 END;
            END; (* CASE *)
    END; (* CASE *)
END; (* StxRecover *)

(*
    Main parser routine, uses Shift, Reduce and Recover 
*)
FUNCTION StxParse: BOOLEAN;
VAR
    action: LongInt;
BEGIN
    pStxStack := 0;
    sStxStack[0] := 0;
    StxChar := StxNextChar;
    StxSym := StxLexer;
    StxState := 0;
    StxErrorFlag := 0;

    WHILE TRUE do
        BEGIN
        action := StxAction(StxState, StxSym);
        IF   action = ACCEPT
        THEN BEGIN
{$IFDEF DEBUG}
             writeln('Accepted');
{$ENDIF}
             StxParse := TRUE;
             EXIT;
             END
        ELSE IF   action > 0
        THEN BEGIN
             IF   Not StxShift(StxSym, action)
             THEN BEGIN
                  StxParse := FALSE;
                  EXIT;
             END;
             StxSym := StxLexer;
             IF   StxErrorFlag > 0
             THEN StxErrorFlag := StxErrorFlag - 1; (* properly recovering from error *)
             END
        ELSE IF   action < 0
        THEN BEGIN
             IF   Not StxReduce(StxSym, -action)
             THEN BEGIN
                  IF   StxErrorFlag = -1
                  THEN BEGIN
                       IF   NOT StxRecover
                       THEN BEGIN
                            StxParse := FALSE;
                            EXIT;
                            END;
                       END
                  ELSE BEGIN
                       StxParse := FALSE;
                       EXIT;
                       END;
                  END
             END
        ELSE BEGIN (* error *)
             IF   not StxRecover
             THEN BEGIN
                  StxParse := FALSE;
                  EXIT;
                  END;
             END;
        END; (* while *)
END;

FUNCTION StxGetResult : TSTACK;
BEGIN
    StxGetResult := StxStack[pStxStack];
END;

(* End of parser *)
