import java.util.regex.PatternSyntaxException;

%% // Options of the scanner

%class LexicalAnalyzer	//Name
%unicode	            //Use unicode
%line         	//Use line counter (yyline variable)
%column       	//Use character counter by line (yycolumn variable)
%type Symbol  	//Says that the return type is Symbol
%standalone             //Without Parser
%yylexthrow PatternSyntaxException

// Return value of the program
%eofval{
	return new Symbol(LexicalUnit.EOS);
%eofval}

// Declare states
%state YYINITIAL

//Extended Regular Expressions

Space		        = "\t" | " "
Number              = [1-9][0-9]* | 0
BadNumber     	    = (0[0-9]+)
Progname            = [A-Z][A-Za-z0-9]*[a-z]+[A-Za-z0-9]*
Comment             = "%%"[^%%]*"%%" | "::".*
VarName             = [a-z]+[a-z0-9]*
EndOfLine	        = "\r"?"\n"

%%

<YYINITIAL> {   // contains all tokens execution.
    "BEGIN"             { return new Symbol(LexicalUnit.BEGIN, yyline, yycolumn, yytext());}
    "END"               { return new Symbol(LexicalUnit.END, yyline, yycolumn, yytext());}
    ","                 { return new Symbol(LexicalUnit.COMMA, yyline, yycolumn, yytext());}
    ":="                { return new Symbol(LexicalUnit.ASSIGN, yyline, yycolumn, yytext());}
    "("                 { return new Symbol(LexicalUnit.LPAREN, yyline, yycolumn, yytext());}
    ")"                 { return new Symbol(LexicalUnit.RPAREN, yyline, yycolumn, yytext());}
    "-"                 { return new Symbol(LexicalUnit.MINUS, yyline, yycolumn, yytext());}
    "+"                 { return new Symbol(LexicalUnit.PLUS, yyline, yycolumn, yytext());}
    "*"                 { return new Symbol(LexicalUnit.TIMES, yyline, yycolumn, yytext());}
    "/"                 { return new Symbol(LexicalUnit.DIVIDE, yyline, yycolumn, yytext());}
    "IF"                { return new Symbol(LexicalUnit.IF, yyline, yycolumn, yytext());}
    "THEN"              { return new Symbol(LexicalUnit.THEN, yyline, yycolumn, yytext());}
    "ENDIF"             { return new Symbol(LexicalUnit.ENDIF, yyline, yycolumn, yytext());}
    "ELSE"              { return new Symbol(LexicalUnit.ELSE, yyline, yycolumn, yytext());}
    "="                 { return new Symbol(LexicalUnit.EQUAL, yyline, yycolumn, yytext());}
    ">"                 { return new Symbol(LexicalUnit.GREATER, yyline, yycolumn, yytext());}
    "<"                 { return new Symbol(LexicalUnit.SMALLER, yyline, yycolumn, yytext());}
    "WHILE"             { return new Symbol(LexicalUnit.WHILE, yyline, yycolumn, yytext());}
    "DO"                { return new Symbol(LexicalUnit.DO, yyline, yycolumn, yytext());}
    "PRINT"             { return new Symbol(LexicalUnit.PRINT, yyline, yycolumn, yytext());}
    "READ"              { return new Symbol(LexicalUnit.READ, yyline, yycolumn, yytext());}
    {Progname}          { return new Symbol(LexicalUnit.PROGNAME, yyline, yycolumn, yytext());}
    {VarName}           { return new Symbol(LexicalUnit.VARNAME, yyline, yycolumn, yytext());}
    {Number}            { return new Symbol(LexicalUnit.NUMBER, yyline, yycolumn, yytext());}
    {BadNumber}		    { System.err.println("Warning! Numbers with leading zeros are not permitted: " + yytext()); 
    			          return new Symbol(LexicalUnit.NUMBER, yyline, yycolumn, Integer.valueOf(yytext()));}
    {Comment}           {}
    {Space}             {}
    {EndOfLine}         {}
    .                   { throw new PatternSyntaxException("Unknown symbol(s) detected", yytext(),yyline);}
}
