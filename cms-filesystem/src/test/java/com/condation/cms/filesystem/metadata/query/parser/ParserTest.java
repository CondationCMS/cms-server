package com.condation.cms.filesystem.metadata.query.parser;

/*-
 * #%L
 * cms-filesystem
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
import com.condation.cms.filesystem.metadata.query.parser.expressions.Expression;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ParserTest {

	private final Parser parser = new Parser();

	@Test
	void parsesSimpleEqualsString() {
		Expression expr = parser.parse("name = 'Thorsten'");
		assertThat(expr.toString()).isEqualTo("name EQ 'Thorsten'");
	}

	@Test
	void parses_complex_name() {
		Expression expr = parser.parse("name.test.demo = 'Thorsten'");
		assertThat(expr.toString()).isEqualTo("name.test.demo EQ 'Thorsten'");
	}
	
	@Test
	void parsesNumberComparison() {
		Expression expr = parser.parse("age >= 30");
		assertThat(expr.toString()).isEqualTo("age GTE 30");
	}

	@Test
	void parsesBooleanComparison() {
		Expression expr = parser.parse("active = true");
		assertThat(expr.toString()).isEqualTo("active EQ true");
	}

	@Test
	void parsesAndOrOperators() {
		Expression expr = parser.parse("age > 18 AND active = true OR name = 'Anna'");
		assertThat(expr.toString()).isEqualTo("((age GT 18 AND active EQ true) OR name EQ 'Anna')");
	}

	@Test
	void parsesWithParentheses() {
		Expression expr = parser.parse("(age > 18 AND active = true) OR (name = 'Anna' AND country = 'DE')");
		assertThat(expr.toString()).isEqualTo("((age GT 18 AND active EQ true) OR (name EQ 'Anna' AND country EQ 'DE'))");
	}

	@Test
	void parsesInWithStrings() {
		Expression expr = parser.parse("country IN ('DE', 'FR', 'US')");
		assertThat(expr.toString()).isEqualTo("country IN ('DE', 'FR', 'US')");
	}

	@Test
	void parsesInWithNumbers() {
		Expression expr = parser.parse("id IN (1, 2, 3, 4)");
		assertThat(expr.toString()).isEqualTo("id IN (1, 2, 3, 4)");
	}

	@Test
	void parsesNestedParentheses() {
		Expression expr = parser.parse("((age > 18 AND active = true) OR (country = 'DE')) AND premium = false");
		assertThat(expr.toString()).isEqualTo("(((age GT 18 AND active EQ true) OR country EQ 'DE') AND premium EQ false)");
	}

	@Test
	void parsesNotEquals() {
		Expression expr = parser.parse("name != 'Peter'");
		assertThat(expr.toString()).isEqualTo("name NEQ 'Peter'");
	}

	@Test
	void parsesLessOrEqual() {
		Expression expr = parser.parse("age <= 65");
		assertThat(expr.toString()).isEqualTo("age LTE 65");
	}

	@Test
	void parsesGreaterOrEqual() {
		Expression expr = parser.parse("age >= 21");
		assertThat(expr.toString()).isEqualTo("age GTE 21");
	}

	@Test
	void parsesContains() {
		Expression expr = parser.parse("name CONTAINS ('Thor')");
		assertThat(expr.toString()).isEqualTo("name CONTAINS ('Thor')");
	}

	@Test
	void parsesContainsNot() {
		Expression expr = parser.parse("name CONTAINS NOT ('Max')");
		assertThat(expr.toString()).isEqualTo("name CONTAINS NOT ('Max')");
	}

	@Test
	void parsesNotIn() {
		Expression expr = parser.parse("country NOT IN ('DE', 'FR')");
		assertThat(expr.toString()).isEqualTo("country NOT IN ('DE', 'FR')");
	}
}
