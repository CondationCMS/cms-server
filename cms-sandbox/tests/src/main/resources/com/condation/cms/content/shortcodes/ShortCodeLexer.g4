lexer grammar ShortCodeLexer;

TAG_OPENING_BRACKET: '[[';

EQUALS : '=' ;
SPACE : ' ' ;
SINGLE_OPEN_BRAKET : '[' ;
DASH : '-' ;


TAG_CLOSING_BRACKET: ']]';
TAG_CLOSING_CLOSING_BRACKET: '/]]' ;
TAG_OPENING_CLOSING_BRACKET: '[[/';
TAG_NAME: [a-zA-Z][a-zA-Z0-9]* ;
TAG_STRING: '"' (~["])* '"' ;
TAG_NUMBER: [0-9]+ ;
TAG_WS: [ \t]+ ;