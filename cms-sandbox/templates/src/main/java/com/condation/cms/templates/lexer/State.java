package com.condation.cms.templates.lexer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author thmar
 */
public class State {
	public enum Type {
		NONE,
		TAG,
		VARIABLE,
		COMMENT
	}
	
	private Type current = Type.NONE;
	
	
	public void set (Type type) {
		current = type;
	}
	
	public boolean is (Type... types) {
		if (types == null || types.length == 0) {
			return false;
		}
		
		List<Type> candidates = new ArrayList<>();
		candidates.addAll(Arrays.asList(types));
		
		return candidates.contains(current);
	}
}
