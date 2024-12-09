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

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class TokenStreamTest {

	@Test
	public void testSomeMethod() {

		var tokenStream = new TokenStream(List.of(
				new Token(Token.Type.TAG_START, "eins", 0, 0),
				new Token(Token.Type.EXPRESSION, "zwei", 0, 0),
				new Token(Token.Type.TAG_START, "drei", 0, 0)
		));

		Assertions.assertThat(tokenStream.peek().value).isEqualTo("eins");
		Assertions.assertThat(tokenStream.next().value).isEqualTo("eins");
		Assertions.assertThat(tokenStream.getPosition()).isEqualTo(1);
		Assertions.assertThat(tokenStream.next().value).isEqualTo("zwei");
		Assertions.assertThat(tokenStream.getPosition()).isEqualTo(2);
		Assertions.assertThat(tokenStream.next().value).isEqualTo("drei");
		
		tokenStream.reset(1);
		Assertions.assertThat(tokenStream.getPosition()).isEqualTo(1);
		Assertions.assertThat(tokenStream.peek().value).isEqualTo("zwei");
		tokenStream.skip();
		Assertions.assertThat(tokenStream.getPosition()).isEqualTo(2);
		Assertions.assertThat(tokenStream.peek().value).isEqualTo("drei");
	}

}
