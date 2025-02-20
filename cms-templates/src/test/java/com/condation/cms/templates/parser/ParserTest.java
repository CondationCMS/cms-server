package com.condation.cms.templates.parser;

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
import com.condation.cms.templates.TemplateConfiguration;
import com.condation.cms.templates.lexer.Lexer;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

/**
 *
 * @author t.marx
 */
public class ParserTest {

	private static final JexlEngine jexl = new JexlBuilder()
			.cache(512)
			.strict(true)
			.silent(false)
			.create();

	static Parser parser;
	static Lexer lexer;

	@BeforeAll
	public static void setup() {
		var config = new TemplateConfiguration();
		parser = new Parser(config, jexl);
		lexer = new Lexer();
	}

	@Test
	public void test_filters() {
		var input = "{{ content | raw | trim }}";

		var tokenStream = lexer.tokenize(input);

		var ast = parser.parse(tokenStream);

		Assertions.assertThat(ast.getChildren().getFirst()).isInstanceOf(VariableNode.class);

		var variableNode = (VariableNode) ast.getChildren().getFirst();

		Assertions.assertThat(variableNode.hasFilters()).isEqualTo(true);
		Assertions.assertThat(variableNode.getFilters()).containsExactly(
				new Filter("raw"), new Filter("trim")
		);
	}

	@Test
	public void test_complex_filters() {
		var input = "{{ meta['date'] | date}}";

		var tokenStream = lexer.tokenize(input);

		var ast = parser.parse(tokenStream);

		Assertions.assertThat(ast.getChildren().getFirst()).isInstanceOf(VariableNode.class);

		var variableNode = (VariableNode) ast.getChildren().getFirst();

		Assertions.assertThat(variableNode.hasFilters()).isEqualTo(true);
		Assertions.assertThat(variableNode.getFilters()).containsExactly(
				new Filter("date")
		);
	}

}
