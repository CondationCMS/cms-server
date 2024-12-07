package com.condation.cms.templates.renderer;

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

import lombok.RequiredArgsConstructor;
import org.apache.commons.jexl3.JexlContext;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class ScopeContext implements JexlContext {

	private final ScopeStack scopeStack;
	
	@Override
	public Object get(String name) {
		if (has(name)) {
			return scopeStack.getVariable(name).get();
		}
		return null;
	}

	@Override
	public boolean has(String name) {
		return scopeStack.getVariable(name).isPresent();
	}

	@Override
	public void set(String name, Object value) {
		scopeStack.setVariable(name, value);
	}
	
}
