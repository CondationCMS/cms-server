parser grammar ShortCodeParser;

options {
    tokenVocab = ShortCodeLexer;
}

shortcodes: (shortcode)+ EOF;

shortcode:
	openingTag content? closingTag               # shortcodeWithContent
    | selfClosingTag                             # selfClosingShortcode
	;

openingTag: 
    TAG_OPENING_BRACKET TAG_NAME TAG_CLOSING_BRACKET ;
closingTag: 
    TAG_OPENING_CLOSING_BRACKET TAG_NAME TAG_CLOSING_BRACKET ;
selfClosingTag: 
    TAG_OPENING_BRACKET TAG_NAME TAG_WS? TAG_CLOSING_CLOSING_BRACKET ;

params: param (TAG_WS+ param)* ;
param: TAG_NAME EQUALS value ;

value: TAG_STRING | TAG_NUMBER;
content: (~SINGLE_OPEN_BRAKET | SPACE)+ ; 
