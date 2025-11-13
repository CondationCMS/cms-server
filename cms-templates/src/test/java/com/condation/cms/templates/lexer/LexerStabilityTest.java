package com.condation.cms.templates.lexer;

/*-
 * #%L
 * cms-templates
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

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LexerStabilityTest {

    @Test
    public void testEscapedQuotes() {
        var input = "{{ \"hello \\\"world\\\"\" }}";
        var lexer = new Lexer();
        var tokenStream = lexer.tokenize(input);
        tokenStream.next(); // Skip VARIABLE_START
        var token = tokenStream.next();
        assertEquals(Token.Type.IDENTIFIER, token.type);
        assertEquals("\"hello \"world\"\"", token.value);
    }

    @Test
    public void testMismatchedQuotes() {
        var input = "{{ \"hello }}";
        var lexer = new Lexer();
        var tokenStream = lexer.tokenize(input);
        tokenStream.next(); // Skip VARIABLE_START
        var token = tokenStream.next();
        assertEquals(Token.Type.IDENTIFIER, token.type);
        assertEquals("\"hello }}", token.value);
    }

    @Test
    public void testAdjacentVariables() {
        var input = "{{ var1 }}{{ var2 }}";
        var lexer = new Lexer();
        var tokenStream = lexer.tokenize(input);
        tokenStream.next(); // Skip VARIABLE_START
        var token1 = tokenStream.next();
        assertEquals("var1", token1.value);
        tokenStream.next(); // Skip VARIABLE_END
        tokenStream.next(); // Skip VARIABLE_START
        var token2 = tokenStream.next();
        assertEquals("var2", token2.value);
    }
}
