package com.condation.cms.content.markdown;

/*-
 * #%L
 * cms-content
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

import com.condation.cms.content.markdown.Options;
import com.condation.cms.content.markdown.BlockTokenizer;
import com.condation.cms.content.markdown.Block;
import com.condation.cms.content.markdown.rules.block.CodeBlockRule;
import com.condation.cms.content.markdown.rules.block.ParagraphBlockRule;
import java.io.IOException;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

/**
 *
 * @author t.marx
 */
public class BlockTokenizerTest extends MarkdownTest {

	static BlockTokenizer sut;

	@BeforeAll
	public static void setup() {
		Options options = new Options();
		options.addBlockRule(new CodeBlockRule());
		options.addBlockRule(new ParagraphBlockRule());
		sut = new BlockTokenizer(options);
	}

	@Test
	void test_single_line() throws IOException {
		String content = load("block_single_line.md");
		List<Block> blocks = sut.tokenize(content);

		assertThat(blocks).hasSize(1);
		assertThat(blocks.get(0)).isInstanceOf(ParagraphBlockRule.ParagraphBlock.class);
		var pb = (ParagraphBlockRule.ParagraphBlock) blocks.get(0);
		assertThat(pb.content()).isEqualToIgnoringNewLines("Hallo");
	}

	@Test
	void test_two_lines() throws IOException {
		String content = load("block_two_lines.md");
		List<Block> blocks = sut.tokenize(content);

		assertThat(blocks).hasSize(1);
		assertThat(blocks.get(0)).isInstanceOf(ParagraphBlockRule.ParagraphBlock.class);
		var pb = (ParagraphBlockRule.ParagraphBlock) blocks.get(0);
		assertThat(pb.content()).isEqualToIgnoringNewLines("Hallo\nLeute");
	}

	@Test
	void test_two_blocks() throws IOException {
		String content = load("block_two_blocks.md");
		List<Block> blocks = sut.tokenize(content);

		assertThat(blocks).hasSize(2);
		assertThat(blocks.get(0)).isInstanceOf(ParagraphBlockRule.ParagraphBlock.class);
		assertThat(blocks.get(1)).isInstanceOf(ParagraphBlockRule.ParagraphBlock.class);
		var pb = (ParagraphBlockRule.ParagraphBlock) blocks.get(0);
		assertThat(pb.content()).isEqualToIgnoringNewLines("Hallo");
		pb = (ParagraphBlockRule.ParagraphBlock) blocks.get(1);
		assertThat(pb.content()).isEqualToIgnoringNewLines("Leute");
	}
	
	@Test
	void test_code_paragraph() throws IOException {
		String content = load("block_code_paragraph.md");
		List<Block> blocks = sut.tokenize(content);

		assertThat(blocks).hasSize(4);
		assertThat(blocks.get(0)).isInstanceOf(CodeBlockRule.CodeBlock.class);
		assertThat(blocks.get(1)).isInstanceOf(ParagraphBlockRule.ParagraphBlock.class);
		assertThat(blocks.get(2)).isInstanceOf(ParagraphBlockRule.ParagraphBlock.class);
		assertThat(blocks.get(3)).isInstanceOf(CodeBlockRule.CodeBlock.class);
		var cb = (CodeBlockRule.CodeBlock) blocks.get(0);
		assertThat(cb.content()).isEqualToIgnoringNewLines("java.lang.System.out.println(\"Hello world!\");");
		assertThat(cb.language()).isEqualToIgnoringNewLines("java");
		var pb = (ParagraphBlockRule.ParagraphBlock) blocks.get(2);
		assertThat(pb.content()).isEqualToIgnoringNewLines("Hallo");
	}
}
