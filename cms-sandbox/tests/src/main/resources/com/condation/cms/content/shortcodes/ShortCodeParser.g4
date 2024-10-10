parser grammar ShortCodeParser;

options {
    tokenVocab = ShortCodeLexer;
}

shortcodes: (shortcode | text)+ EOF;

shortcode:
	openingTag content? closingTag               # shortcodeWithContent
    | selfClosingTag                             # selfClosingShortcode
	;

openingTag: 
    TAG_OPENING_BRACKET TAG_NAME SPACE* TAG_WS* params? SPACE* TAG_WS* TAG_CLOSING_BRACKET ;
closingTag: 
    TAG_OPENING_CLOSING_BRACKET TAG_NAME TAG_CLOSING_BRACKET ;
selfClosingTag: 
    TAG_OPENING_BRACKET TAG_NAME SPACE* TAG_WS* params? SPACE* TAG_WS* TAG_CLOSING_CLOSING_BRACKET ;

params: param (TAG_WS+ param)* ;
param: TAG_NAME EQUALS value ;

value: TAG_STRING | TAG_NUMBER;
content: (~SINGLE_OPEN_BRAKET | SPACE | DASH)+ ; 

text2: 
    (.)+?  { _input.LA(1) != '[' || _input.LA(2) != '[' }? 
    ;
