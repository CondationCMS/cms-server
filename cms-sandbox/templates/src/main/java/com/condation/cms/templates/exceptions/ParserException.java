package com.condation.cms.templates.exceptions;

/**
 *
 * @author t.marx
 */
public class ParserException extends RuntimeException {

	private final int line;
	private final int column;
	
	public ParserException(String message, int line, int column) {
		super(message);
		this.line = line;
		this.column = column;
	}
	
	
}
