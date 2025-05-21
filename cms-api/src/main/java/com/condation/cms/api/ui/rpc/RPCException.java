package com.condation.cms.api.ui.rpc;

/**
 *
 * @author thmar
 */
public class RPCException extends Exception {
	
	private int code = 0;
	
	public RPCException(int code, String message) {
		super(message);
	}
	
	public int getCode () {
		return code;
	}
}
