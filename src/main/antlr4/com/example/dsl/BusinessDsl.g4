grammar BusinessDsl;

// 解析规则
program
    : statement+ EOF
    ;

statement
    : functionDecl
    | variableDecl
    | assignmentStmt
    | ifStatement
    | forStatement
    | returnStatement
    | functionCall SEMICOLON
    | SEMICOLON
    ;

functionDecl
    : 'function' ID '(' paramList? ')' block
    ;

paramList
    : ID (',' ID)*
    ;

variableDecl
    : 'var' ID ('=' expr)? SEMICOLON
    ;

assignmentStmt
    : ID '=' expr SEMICOLON
    ;

ifStatement
    : 'if' '(' expr ')' block ('else' elseIfBlock)?
    ;

elseIfBlock
    : ifStatement
    | block
    ;

forStatement
    : 'for' '(' ID 'in' expr ')' block
    ;

returnStatement
    : 'return' expr? SEMICOLON
    ;

block
    : '{' statement* '}'
    ;

expr
    : expr '||' expr                        # orExpr
    | expr '&&' expr                        # andExpr
    | expr op=('==' | '!=') expr            # equalityExpr
    | expr op=('<' | '>' | '<=' | '>=') expr # comparisonExpr
    | expr op=('+' | '-') expr              # addSubExpr
    | expr op=('*' | '/' | '%') expr        # mulDivExpr
    | op=('-' | '!') expr                   # unaryExpr
    | expr '[' expr ']'                     # indexExpr
    | expr '.' ID                           # memberExpr
    | functionCall                          # funcExpr
    | '(' expr ')'                          # parenExpr
    | ID                                    # idExpr
    | STRING                                # stringExpr
    | NUMBER                                # numberExpr
    | array                                 # arrayExpr
    | object                                # objectExpr
    | BOOLEAN                               # booleanExpr
    | NULL                                  # nullExpr
    ;

functionCall
    : ID '(' argumentList? ')'
    ;

argumentList
    : expr (',' expr)*
    ;

array
    : '[' (expr (',' expr)*)? ']'
    ;

object
    : '{' (pair (',' pair)*)? '}'
    ;

pair
    : STRING ':' expr
    | ID ':' expr
    ;

// 词法规则
BOOLEAN : 'true' | 'false' ;
NULL    : 'null' ;

ID      : [a-zA-Z_][a-zA-Z_0-9]* ;
NUMBER  : INT ('.' DIGIT*)? ([eE] [+-]? DIGIT+)? ;
fragment INT    : '0' | [1-9] DIGIT* ;
fragment DIGIT  : [0-9] ;

STRING  : '"' (ESC | ~["\\])* '"' ;
fragment ESC    : '\\' (["\\bfnrt] | UNICODE) ;
fragment UNICODE: 'u' HEX HEX HEX HEX ;
fragment HEX    : [0-9a-fA-F] ;

SEMICOLON : ';' ;

BLOCK_COMMENT : '/*' .*? '*/' -> skip ;
LINE_COMMENT  : '//' ~[\r\n]* -> skip ;
WS            : [ \t\r\n]+ -> skip ;
