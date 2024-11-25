package com.condation.cms.templates.lexer;

/*-
 * #%L
 * templates
 * %%
 * Copyright (C) 2023 - 2024 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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
