package com.condation.cms.templates.utils;

/*-
 * #%L
 * cms-templates
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

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.MapContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class ParameterUtilTest {

	JexlEngine engine = new JexlBuilder().create();

	@Test
	public void testSomeMethod() {
		
		var context = new MapContext();
		context.set("variable", "CMS");
		
		var parameters = ParameterUtil.parseAndEvaluate("param1=\"CondationCMS\" param2=30 param3=variable", context, engine);
		
		Assertions.assertThat(parameters)
				.hasSize(3)
				.containsEntry("param1", "CondationCMS")
				.containsEntry("param2", 30)
				.containsEntry("param3", "CMS");
	}

}
