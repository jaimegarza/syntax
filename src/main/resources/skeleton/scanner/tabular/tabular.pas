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
FUNCTION StxGetTokenName(token:LongInt) : STRING;
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
    returns the name of a token, given the token number
*)
FUNCTION StxGetTokenFullName(token:LongInt) : STRING;
VAR
    i : INTEGER;
BEGIN
    FOR i := 0 TO TOKENS-1 DO
        BEGIN
        IF   StxTokenDefs[i].token = token
        THEN BEGIN
             StxGetTokenFullName := StxTokenDefs[i].fullName;
             EXIT;
             END;
        END;
    StxGetTokenFullName := 'UNKNOWN TOKEN';
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
    msgIndex   : INTEGER;
    s, message : STRING;
    i, j, st   : INTEGER;
BEGIN
    msgIndex := StxParsingError[StxState];
    IF   msgIndex >= 0
    THEN s := StxErrorTable[msgIndex]
    ELSE s := 'Syntax error';

    i := pStxStack;
    WHILE i > 0 DO
    BEGIN
      st := sStxStack[i];
      FOR j := 0 TO RECOVERS DO
      BEGIN
        IF   StxAction(st, StxRecoverTable[j]) > 0
        THEN BEGIN
             message := StxGetTokenFullName(StxRecoverTable[j]);
             message := StringReplace(message, '$m', s, [rfReplaceAll]);
             EXIT (message);
        END;
      END;
      i := i-1;
    END;
    
    StxErrorMessage := s;
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
  Initialize the scanner
*)
PROCEDURE StxInit;
BEGIN
    pStxStack := 0;
    sStxStack[0] := 0;
    StxState := 0;
END;
  
(*
    Main parser routine, uses Shift, Reduce and Recover 
*)
FUNCTION StxParse(symbol:LONGINT; value:TSTACK): INTEGER;
VAR
    action: LongInt;
BEGIN
    StxSym := StxGetTokenIndex(symbol);
    StxValue := value;

{$IFDEF DEBUG}
        writeln('Starting to parse symbol ', symbol, '(', StxSym,')');
        StxPrintStack();
{$ENDIF}
    WHILE TRUE do (* forever with break and return below *)
        BEGIN
        action := StxAction(StxState, symbol);
{$IFDEF DEBUG}
        writeln('Action: ', action);
{$ENDIF}
        IF   action = ACCEPT
        THEN BEGIN
{$IFDEF DEBUG}
             writeln('Accepted');
{$ENDIF}
             StxParse := ACCEPTED;
             EXIT;
             END
        ELSE IF   action > 0
        THEN BEGIN
             IF   Not StxShift(StxSym, action)
             THEN BEGIN
                  StxParse := INTERNAL_ERROR;
                  EXIT;
             END;
             StxParse := SHIFTED;
             EXIT;
             END
        ELSE IF   action < 0
        THEN BEGIN
             IF   Not StxReduce(StxSym, -action)
             THEN BEGIN
                  StxParse := INTERNAL_ERROR;
                  EXIT;
                  END;
              END
        ELSE BEGIN (* error *)
             StxError(StxState, StxSym, pStxStack, StxErrorMessage());
             StxParse := PARSING_ERROR;
             EXIT;
             END;
        END; (* while *)
END;

TYPE
  StxTokenArray = ARRAY OF INTEGER;
  
(*
 give me the available actions that can be taken.  I am also returning reduces.
*)
FUNCTION StxValidTokens(VAR count:INTEGER) : StxTokenArray;
VAR
  c       : INTEGER;
  index   : INTEGER;
  i       : INTEGER;
  actions : StxTokenArray;
BEGIN
    c := 0;
    for i := 0 to TOKENS-1 DO
      if StxParsingTable[StxState][i] <> 0 then c := c+1;

    SetLength(actions, c);
    index := 0;
{$IFDEF DEBUG}
    write ('Valid actions:[');
{$ENDIF}
    for i := 0 TO TOKENS-1 DO
        IF   StxParsingTable[StxState][i] <> 0
        THEN BEGIN
{$IFDEF DEBUG}
             if index > 0 then write(', ');
             write(StxTokenDefs[i].token);
{$ENDIF}
             actions[index] := StxTokenDefs[i].token;
             index := index + 1;
             END;
{$IFDEF DEBUG}
    writeln (']');
{$ENDIF}
    count := c;
    StxValidTokens := actions;
END;

FUNCTION StxGetResult : TSTACK;
BEGIN
    StxGetResult := StxStack[pStxStack];
END;

(* End of parser *)
