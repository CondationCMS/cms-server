package com.condation.cms.templates.parser;

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

import com.condation.cms.templates.TemplateConfiguration;
import com.condation.cms.templates.exceptions.ParserException;
import com.condation.cms.templates.lexer.Lexer;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ParserStabilityTest {

    private static final JexlEngine jexl = new JexlBuilder()
            .cache(512)
            .strict(true)
            .silent(false)
            .create();

    static Parser parser;
    static Lexer lexer;

    @BeforeAll
    public static void setup() {
        var config = new TemplateConfiguration(true);
        config.registerTag(new com.condation.cms.templates.tags.IfTag());
        config.registerTag(new com.condation.cms.templates.tags.EndIfTag());
        parser = new Parser(config, jexl);
        lexer = new Lexer();
    }

    @Test
    public void testUnclosedTag() {
        var input = "{% if true %}";
        var tokenStream = lexer.tokenize(input);
        assertThrows(ParserException.class, () -> parser.parse(tokenStream));
    }

    @Test
    public void testUnknownTag() {
        var input = "{% unknown %}";
        var tokenStream = lexer.tokenize(input);
        assertThrows(com.condation.cms.templates.exceptions.UnknownTagException.class, () -> parser.parse(tokenStream));
    }

    @Test
    public void testEmptyVariable() {
        var input = "{{}}";
        var tokenStream = lexer.tokenize(input);
        assertDoesNotThrow(() -> parser.parse(tokenStream));
    }

    @Test
    public void testNullIdentifier() {
        var input = "{{ null }}";
        var tokenStream = lexer.tokenize(input);
        assertDoesNotThrow(() -> parser.parse(tokenStream));
    }
}
