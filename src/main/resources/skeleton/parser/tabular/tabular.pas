(*

Parser para archivos en PASCAL para tablas en forma matricial.
SINTAXPA.PAS

Este programa no es de ejemplo.
Es el fuente del parser que se copia a sus archivos de salida.
Procure no modificarlo.

Jaime Garza V zquez
M‚xico, 1990

*)

(* define la constante de no empacado *)
{$DEFINE STX_NOEMPACADO}

(* Define el tipo del stack si no se ha definido *)
{$IFNDEF TSTACK}
TYPE
	TSTACK = integer;
	PTSTACK = ^TSTACK;
{$ENDIF}

(* Si no hubo generaci¢n de c¢digo usar esta funci¢n *)
{$IFNDEF STXCODE}
VAR
	StxStack:ARRAY [0..149] OF TSTACK;
	pStxStack:INTEGER;

FUNCTION StxCodigo(regla:INTEGER):BOOLEAN;
BEGIN
	StxCodigo := TRUE;
END;(* StxCodigo *)
{$ENDIF}

(* Variables globales *)
VAR
	StxValor	: TSTACK;	(* El valor adicional del scanner. Para usuario   *)
	sStxStack	: Array[0..149] of integer; (* Stack de estados. Uso interno  *)
	StxSym		: integer;	(* El s¡mbolo actual del scanner. Uso interno	  *)
	StxEstado	: integer;	(* El estado actual del aut¢mata. Uso interno	  *)
	StxErrorFlag: integer;	(* Estado del recuperador de errores. Uso interno *)

(* Estas funciones las debe proveer el usuario *)
FUNCTION StxScan:INTEGER; FORWARD;
FUNCTION StxError(StxEstado:INTEGER, StxSym:INTEGER, pStxStack:INTEGER):INTEGER; FORWARD;

(*
	Esta funci¢n llama al scanner
	Convierte el valor de retorno del scanner (un token) en
	la columna correspondiente en la tabla de parsing.

	El arreglo StxTokens Contiene los valores de los tokens esperados.
*)

FUNCTION StxScanner:INTEGER;
Var
	sym, i : integer;
Begin
	sym := StxScan;

	for i:=0 to TOKENS-1
		if	 sym = StxTokens[i]
		then exit;
	if	 i = TOKENS
	then StxScanner := 0
	else StxScanner := i;
End;

(*
	Esta funcion imprime por la salida estandar el contenido
	del stack de parsing
*)
{$IFDEF DEBUG}
PROCEDURE PrintStack;
var
	i:integer;
Begin
	writeln('Stack pointer = ', pStxStack);
	write('Estados: [');
	for i:=0 to pStxStack
		write(' ', sStxStack[i]);
	writeln(']');
End;
{$ENDIF}

(*
	Realiza un shift.
	Pone un estado nuevo en el tope del stack.
*)
FUNCTION StxShift(sym:integer, estado:integer):BOOLEAN;
Begin
	if	 pStxStack >= 149
	then StxShift := FALSE
	else Begin
		 pStxStack := pStxStack + 1;
		 sStxStack[pStxStack] := estado;
		 StxStack[pStxStack] := StxValor;
		 StxEstado := estado;
		 StxShift := TRUE;
{$IFDEF DEBUG}
		 writeln('Shift a ', estado, ' con ', sym);
		 PrintStack;
{$ENDIF}
		 End;
End;

(*
	Reconoce una regla y la saca del stack de estados
*)
FUNCTION StxReduce(sym:integer, regla:integer):BOOLEAN;
Begin
{$IFDEF DEBUG}
	writeln('Reduce con regla ', regla, ' con s¡mbolo ', sym);
{$ENDIF}
	if	 Not StxCodigo(regla)
	then StxReduce := FALSE
	else Begin
		 pStxStack := pStxStack - StxTablaGramatica[regla].reducciones;
		 sStxStack[pStxStack+1] :=
			StxTablaParser[sStxStack[pStxStack],
					StxTablaGramatica[regla].simbolo];
		 pStxStack := pStxStack+1;
		 StxEstado := sStxStack[pStxStack];
		 StxReduce := TRUE;
{$IFDEF DEBUG}
		 PrintStack;
{$ENDIF}
		 End;
End;

(*
	Recuperar de un error de sintaxis quitando s¡mbolos del stack
	y de la cadena.
	StxRecupera contiene los tokens que sirven de delimitadores de error.
*)
FUNCTION StxRecover: BOOLEAN;
Var
	i, acc : integer;
	encontrado : boolean;
Begin
	StxRecover := TRUE;
	case StxErrorFlag of
		0, 1, 2: (* tres intentos de recuperaci¢n antes de tirar el symbolo *)
			Begin
			if	 StxErrorFlag = 0
			then begin
				 if   not StxError(StxEstado, StxSym, pStxStack)
				 then begin
					  StxRecover := FALSE;
					  exit;
					  end;
				 end;

			StxErrorFlag := 3; (* Quitar el simbolo *)

			while pStxStack >= 0
				begin
				(* buscar si el estado en el tope del stack tiene
				   transicion con alguno de los StxRecupera *)
				encontrado := FALSE;
				for i:=0 to RECUPERADORES-1
					begin
					acc := StxTablaParser[StxEstado, StxRecupera[i]];
					if	 acc > 0 (* shift valido *)
					then begin
						 StxRecover := StxShift(StxRecupera[i], acc);
						 encontrado := TRUE;
						 exit;
						 end;
					end;
				if	 not encontrado
				then begin
{$IFDEF DEBUG}
					 writeln('Recupera quitando estado ', StxEstado,
							 ' pasa a estado ', sStxStack[pStxStack-1]);
{$ENDIF}
					 pStxStack := pStxStack - 1;
					 StxEstado := sStxStack[pStxStack];
					 end; (*IF*)
					 pStxStack := 0;
					 StxRecover := FALSE;
				end; (*WHILE*)
			end; (*CASE 0, 1 y 2*)

		3: (* Es necesario tirar el Sym actual *)
			Begin
{$IFDEF DEBUG}
			writeln('Recupera quitando s¡mbolo ', StxSym);
{$ENDIF}
			if	 StxSym = 0 (* Fin de cadena *)
			then StxRecover := FALSE;
			else StxSym := StxScanner;
			End; (* CASE *)
	End; (* CASE *)
End; (* StxRecover *)

(* Rutina principal del parser
	Usa a Shift, Reduce y Recupera *)
FUNCTION StxParse: BOOLEAN;
Begin
	pStxStack := 0;
	sStxStack[0] := 0;
	StxSym := StxScanner;
	StxEstado := 0;
	StxErrorFlag := 0;

	while TRUE do
		Begin
		if	 StxTablaParser[StxEstado,StxSym] = 999
		then Begin
			 StxParse := TRUE;
			 Exit;
			 End;

		if	 StxTablaParser[StxEstado,StxSym] >0
		then Begin
			 if   Not StxShift(StxSym, StxTablaParser[StxEstado,StxSym])
			 then Begin
				  StxParse := FALSE;
				  exit;
				  End;
			 StxSym := StxScanner;
			 if   StxErrorFlag > 0
			 then StxErrorFlag := StxErrorFlag - 1; (* se recupero bien de un error *)
			 End;

		if	 StxTablaParser[StxEstado,StxSym] <0
		then Begin
			 if   Not StxReduce(StxSym, -StxTablaParser[StxEstado,StxSym])
			 then Begin
				  StxParse := FALSE;
				  exit;
				  End;
			 End;

		if	 StxTablaParser[StxEstado,StxSym] = 0
		then Begin
			 if   not StxRecover
			 then Begin
				  StxParse := FALSE;
				  Exit;
				  End;
			 End;
		End;
End;

(* Fin del parser *)
