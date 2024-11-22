package com.condation.cms.templates.parser;

import com.condation.cms.templates.lexer.Token;
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
