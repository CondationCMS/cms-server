package com.github.thmarx.cms.filesystem.index;

/*-
 * #%L
 * cms-filesystem
 * %%
 * Copyright (C) 2023 Marx-Software
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

import com.github.thmarx.cms.api.db.ContentNode;
import com.github.thmarx.cms.api.utils.MapUtil;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class SecondaryIndexTest {

	@Test
	public void testSomeMethod() {
		SecondaryIndex<String> index = SecondaryIndex.<String>builder()
				.indexFunction(node -> MapUtil.getValue(node.data(), "name", ""))
				.build();
		final ContentNode node1 = new ContentNode("node1", "Eins", Map.of("name", "Eins"));
		final ContentNode node2 = new ContentNode("node2", "Eins", Map.of("name", "Eins"));
		final ContentNode node3 = new ContentNode("node3", "Eins", Map.of("name", "Zwei"));
		
		index.add(node1);
		index.add(node2);
		index.add(node3);
		
		Assertions.assertThat(index.eq(node1, "Eins")).isTrue();
		Assertions.assertThat(index.eq(node2, "Eins")).isTrue();
		Assertions.assertThat(index.eq(node3, "Eins")).isFalse();
	}
	
}
