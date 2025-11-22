package com.condation.cms.content.markdown;

/*-
 * #%L
 * cms-content
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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.condation.cms.content.markdown.rules.block.CodeBlockRule;
import com.condation.cms.content.markdown.rules.block.ParagraphBlockRule;

public class LargeBlockTokenizerTest extends MarkdownTest {

    static BlockTokenizer sut;

    @BeforeAll
    public static void setup() {
        Options options = new Options();
        options.addBlockRule(new CodeBlockRule());
        options.addBlockRule(new ParagraphBlockRule());
        sut = new BlockTokenizer(options);
    }

    @Test
    void test_large_file() throws IOException {
        String content = load("large_block.md");
        List<Block> blocks = sut.tokenize(content);
        assertThat(blocks).isNotEmpty();
    }
}
