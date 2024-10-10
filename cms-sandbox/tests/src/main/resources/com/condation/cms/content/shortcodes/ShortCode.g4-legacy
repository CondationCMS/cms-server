grammar ShortCode;

shortcodes: (shortcode | text)+ EOF;

shortcode:
	openingTag content? closingTag               # shortcodeWithContent
    | selfClosingTag                             # selfClosingShortcode
	;

openingTag: OPENING_BRACKET NAME params? CLOSING_BRACKET ;
closingTag: OPENING_BRACKET '/' NAME CLOSING_BRACKET ;
selfClosingTag: OPENING_BRACKET NAME params? '/' CLOSING_BRACKET ;

params: param (WS+ param)* ;
param: NAME '=' value ;

value: STRING | NUMBER;

// Der Inhalt muss mindestens ein Zeichen sein, das nicht '[' ist
//content: (text | shortcode)* ;
content: (~'[' | ' ')+ ; 
//content: (~OPENING_BRACKET | ' ')+ ;

//text: (~'[' | ' ')+ ; 
text: (~('[' | ']') | ' ' | '-')+ ;
//text: (~OPENING_BRACKET)+ ;

//TEXT: .;
NAME: [a-zA-Z_][a-zA-Z0-9_]* ;
STRING: '"' (~["])* '"' ;
NUMBER: [0-9]+ ;
WS: [ \t\r\n]+ ;

OPENING_BRACKET: '[[' ;
CLOSING_BRACKET: ']]' ;