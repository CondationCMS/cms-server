package com.condation.cms.tests;

/*-
 * #%L
 * tests
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

import java.util.Arrays;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.MapContext;

/**
 *
 * @author t.marx
 */
public class Tests {

	private static final JexlEngine jexl = new JexlBuilder().cache(512).strict(true).silent(false).create();
	
    public static void main(String[] args) throws Exception {
		JexlContext context = new MapContext();
        var expr = jexl.createExpression("[1, 2, 3]");
		
		int[]value = (int[])expr.evaluate(context);
		
		var list = Arrays.stream(value)
				.boxed().toList();
		
		list.forEach(System.out::println);
		
		
		expr = jexl.createExpression("{'key' : 'value'}");
		
		var map = expr.evaluate(context);
		
		System.out.println(map);
    }
}
