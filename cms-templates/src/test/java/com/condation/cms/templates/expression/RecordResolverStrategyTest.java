package com.condation.cms.templates.expression;

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
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.MapContext;
import org.apache.commons.jexl3.introspection.JexlPermissions;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

/**
 *
 * @author t.marx
 */
public class RecordResolverStrategyTest {

	static JexlEngine engine;

	@BeforeAll
	public static void setup() {
		engine = new JexlBuilder()
				.strategy(new RecordResolverStrategy())
				.permissions(JexlPermissions.UNRESTRICTED)
				.create();
	}

	@Test
	public void test_record() {
		MyRecord record = new MyRecord("Test", 42);

        // Ausdruck
        String expression = "record.attr + ' has value ' + record.value";

        // Kontext und Auswertung
        JexlContext context = new MapContext();
        context.set("record", record);

        Object result = engine.createExpression(expression).evaluate(context);
        
		Assertions.assertThat(result).isEqualTo("Test has value 42");
	}

	public record MyRecord(String attr, int value) {

	}
}
