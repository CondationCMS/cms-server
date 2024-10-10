lexer grammar ShortCodes;

TAG_OPENING_BRACKET: '[[' -> pushMode(TAG);

mode TAG;
TAG_CLOSING_BRACKET: ']]';
TAG_CLOSING_CLOSING_BRACKET: '/]]' -> popMode;
TAG_OPENING_CLOSING_BRACKET: '[[/' -> popMode;
TAG_NAME: [a-zA-Z][a-zA-Z0-9]* ;
TAG_STRING: '"' (~["])* '"' ;
TAG_NUMBER: [0-9]+ ;
TAG_WS: [ \t]+ ;
