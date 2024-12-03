package com.condation.cms.templates.lexer;

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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author t.marx
 */
public class LexerTest {
	

	@Test
	public void testSomeMethod() {
		
		var lexer = new Lexer();
		
		var tokens = lexer.tokenize("{{ \"}}\" }}");
		
		Token token = tokens.next();
		Assertions.assertThat(token.type).isEqualTo(Token.Type.VARIABLE_START);
		token = tokens.next();
		Assertions.assertThat(token.type).isEqualTo(Token.Type.IDENTIFIER);
		Assertions.assertThat(token.value).isEqualTo("\"}}\"");
		token = tokens.next();
		Assertions.assertThat(token.type).isEqualTo(Token.Type.VARIABLE_END);
		
	}
	
}
