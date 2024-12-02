package com.condation.cms.templates;

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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlException;
import org.apache.commons.jexl3.MapContext;
import org.apache.commons.jexl3.introspection.JexlMethod;
import org.apache.commons.jexl3.introspection.JexlPermissions;
import org.apache.commons.jexl3.introspection.JexlUberspect;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class JexlTest {

	private static final JexlEngine jexl = new JexlBuilder()
			.cache(512)
			.strict(true)
			.silent(false)
			.permissions(JexlPermissions.UNRESTRICTED)
			.create();
	
	@RequiredArgsConstructor
	public static class Loop {
		public final int index;
	}
	
	@Test
	void test_bean () {
		JexlContext context = new MapContext();
		
		
		context.set("loop", new Loop(0));
		
		var exp = jexl.createExpression("loop.index");
		
		Assertions.assertThat(exp.evaluate(context)).isEqualTo(0);
	}
	
	@Test
 	void test_fn_wrapper () {
		
		JexlContext context = new MapContext();
		
		
		context.set("fn", new HelloFunction());
		
		var exp = jexl.createExpression("fn('CondationCMS')");
		
		exp.evaluate(context);
	}
	
	public static class HelloFunction implements JexlMethod {

		
		
		@Override
		public Class<?> getReturnType() {
			return String.class;
		}

		@Override
		public Object invoke(Object obj, Object... params) throws Exception {
			return "Hello " + params[0];
		}

		@Override
		public boolean isCacheable() {
			return false;
		}

		@Override
		public boolean tryFailed(Object rval) {
			return rval == JexlEngine.TRY_FAILED;
		}

		@Override
		public Object tryInvoke(String name, Object obj, Object... params) throws JexlException.TryFailed {
			try {
				return invoke(obj, params);
			} catch (Exception e) {
				
			}
			return this;
		}
	
	}
}
