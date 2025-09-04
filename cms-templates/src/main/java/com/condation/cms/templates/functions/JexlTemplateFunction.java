package com.condation.cms.templates.functions;

/*-
 * #%L
 * cms-templates
 * %%
 * Copyright (C) 2023 - 2025 CondationCMS
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlException;
import org.apache.commons.jexl3.introspection.JexlMethod;

/**
 *
 * @author thorstenmarx
 */
@RequiredArgsConstructor
@Slf4j
public class JexlTemplateFunction implements JexlMethod {

	private final TemplateFunction function;

	@Override
	public Class<?> getReturnType() {
		return Object.class;
	}

	@Override
	public Object invoke(Object obj, Object... params) throws Exception {
		return function.invoke(params);
	}

	@Override
	public boolean isCacheable() {
		return false;
	}

	@Override
	public boolean tryFailed(Object rval) {
		return JexlEngine.TRY_FAILED.equals(rval);
	}

	@Override
	public Object tryInvoke(String name, Object obj, Object... params) throws JexlException.TryFailed {
		try {
			return invoke(obj, params);
		} catch (Exception ex) {
			log.error("error calling macro", ex);
		}
		return JexlEngine.TRY_FAILED;
	}

}
