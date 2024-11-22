package com.condation.cms.templates.parser;

import com.condation.cms.templates.TemplateConfiguration;
import com.condation.cms.templates.lexer.Token;
import com.condation.cms.templates.tags.ElseIfTag;
import com.condation.cms.templates.tags.ElseTag;
import com.condation.cms.templates.tags.IfTag;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class ParserTest {

	@Test
	public void testSomeMethod() {
		TemplateConfiguration config = new TemplateConfiguration();
		config.registerTag(new IfTag());
		config.registerTag(new ElseIfTag());
		config.registerTag(new ElseTag());

		List<Token> tokens = List.of(
				new Token(Token.Type.TAG_START, "if", 0,0),
				new Token(Token.Type.EXPRESSION, "condition", 0,0),
				new Token(Token.Type.TAG_START, "elseif", 0,0),
				new Token(Token.Type.EXPRESSION, "condition2", 0,0),
				new Token(Token.Type.TAG_START, "else", 0,0),
				new Token(Token.Type.TEXT, "fallback", 0,0),
				new Token(Token.Type.TAG_END, "endif", 0,0)
		);

		Parser parser = new Parser(tokens, config);
		ASTNode ast = parser.parse();
		
		System.out.println(ast);
	}

}
