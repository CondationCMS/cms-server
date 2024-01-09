package com.github.thmarx.cms.markdown;

/*-
 * #%L
 * cms-markdown
 * %%
 * Copyright (C) 2023 - 2024 Marx-Software
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
		sut = new BlockTokenizer();
	}

	@Test
	void test_single_line() throws IOException {
		String content = load("block_single_line.md");
		List<Block> blocks = sut.tokenize(content);

		assertThat(blocks).hasSize(1);
		assertThat(blocks.get(0).getContent()).isEqualToIgnoringNewLines("Hallo");
	}

	@Test
	void test_two_lines() throws IOException {
		String content = load("block_two_lines.md");
		List<Block> blocks = sut.tokenize(content);

		assertThat(blocks).hasSize(1);
		assertThat(blocks.get(0).getContent()).isEqualToIgnoringNewLines("Hallo\nLeute");
	}

	@Test
	void test_two_blocks() throws IOException {
		String content = load("block_two_blocks.md");
		List<Block> blocks = sut.tokenize(content);

		assertThat(blocks).hasSize(2);
		assertThat(blocks.get(0).getContent()).isEqualToIgnoringNewLines("Hallo");
		assertThat(blocks.get(1).getContent()).isEqualToIgnoringNewLines("Leute");
	}
}
