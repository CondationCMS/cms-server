package com.condation.cms.filesystem.usage;

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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author thorstenmarx
 */
public class LuceneUsageIndexTest {
	
	private static LuceneUsageIndex usage;

	@BeforeAll
	public static void setup() throws IOException {

		Path parent = Path.of("target", "usage" + System.currentTimeMillis());
		if (!Files.exists(parent)) {
			Files.createDirectory(parent);
		}
		usage = new LuceneUsageIndex(parent);
	}

	@AfterAll
	public static void shutdown() throws IOException {
		usage.close();
	}

	@Test
	public void test_usage_index() throws IOException {
		usage.addUsage(new UsageIndex.Reference("1", "page", "1", "section", "reference"));
		usage.addUsage(new UsageIndex.Reference("1", "page", "2", "section", "reference"));
		usage.addUsage(new UsageIndex.Reference("2", "page", "1", "section", "reference"));
		usage.addUsage(new UsageIndex.Reference("2", "page", "1", "header", "uses"));

		List<UsageIndex.Reference> references = usage.getUses("2", "page");
		assertThat(references)
				.hasSize(2)
				.containsExactlyInAnyOrder(new UsageIndex.Reference("2", "page", "1", "section", "reference"),
						new UsageIndex.Reference("2", "page", "1", "header", "uses"));

		references = usage.getUses("2", "page", "uses");
		assertThat(references)
				.hasSize(1)
				.containsExactlyInAnyOrder(new UsageIndex.Reference("2", "page", "1", "header", "uses"));

		references = usage.getUsedBy("1", "section");
		assertThat(references)
				.hasSize(2)
				.containsExactlyInAnyOrder(
						new UsageIndex.Reference("1", "page", "1", "section", "reference"),
						new UsageIndex.Reference("2", "page", "1", "section", "reference")
				);

		references = usage.getUsedBy("2", "section");
		assertThat(references)
				.hasSize(1)
				.containsExactlyInAnyOrder(new UsageIndex.Reference("1", "page", "2", "section", "reference"));
	}
}
