package com.condation.cms.modules.system.api.helpers;

/*-
 * #%L
 * api-module
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

import java.util.Map;
import java.util.Set;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;


/**
 *
 * @author thorstenmarx
 */
public class WhitelistFilterTest {

	@Test
	public void testSomeMethod() {
		
		var meta = Map.of(
				"title", "the title",
				"meta", Map.of(
						"desc", "the description",
						"author", "its me"
				)
		);
		
		var filteredMeta = WhitelistFilter.applyWhitelist(meta, Set.of("title", "meta.*"));
		
		Assertions.assertThat(filteredMeta)
				.containsKeys("title", "meta");
		Assertions.assertThat((Map<String, Object>) filteredMeta.get("meta"))
				.containsKeys("desc", "author");
	}
	
	@Test
    void testExactMatchWhitelist() {
        Map<String, Object> input = Map.of(
            "title", "My Title",
            "meta", Map.of("author", "Thorsten", "description", "test")
        );

        Set<String> whitelist = Set.of("title", "meta.author");

        Map<String, Object> result = WhitelistFilter.applyWhitelist(input, whitelist);

        Assertions.assertThat(result).containsOnlyKeys("title", "meta");
        Assertions.assertThat((Map<String, ?>) result.get("meta")).containsOnlyKeys("author");
    }

    @Test
    void testWildcardOnNestedMap() {
        Map<String, Object> input = Map.of(
            "meta", Map.of("author", "Thorsten", "description", "foo", "tags", List.of("cms", "java"))
        );

        Set<String> whitelist = Set.of("meta.*");

        Map<String, Object> result = WhitelistFilter.applyWhitelist(input, whitelist);

        Assertions.assertThat(result).containsOnlyKeys("meta");
        Assertions.assertThat((Map<String, ?>) result.get("meta")).containsKeys("author", "description", "tags");
    }

    @Test
    void testWildcardOnListOfPrimitives() {
        Map<String, Object> input = Map.of(
            "tags", List.of("a", "b", "c"),
            "title", "Hello"
        );

        Set<String> whitelist = Set.of("tags.*");

        Map<String, Object> result = WhitelistFilter.applyWhitelist(input, whitelist);

        Assertions.assertThat(result).containsOnlyKeys("tags");
        Assertions.assertThat((List<String>) result.get("tags")).containsExactly("a", "b", "c");
    }

    @Test
    void testListOfMapsFiltered() {
        Map<String, Object> input = Map.of(
            "entries", List.of(
                Map.of("key", "value1", "hidden", "secret"),
                Map.of("key", "value2", "hidden", "secret2")
            )
        );

        Set<String> whitelist = Set.of("entries.key");

        Map<String, Object> result = WhitelistFilter.applyWhitelist(input, whitelist);

        Assertions.assertThat(result).containsOnlyKeys("entries");
        List<?> list = (List<?>) result.get("entries");
        Assertions.assertThat(list).hasSize(2);
        for (Object item : list) {
            Assertions.assertThat(item).isInstanceOf(Map.class);
            Assertions.assertThat((Map<String, ?>) item).containsOnlyKeys("key");
        }
    }

    @Test
    void testWildcardOnListOfMaps() {
        Map<String, Object> input = Map.of(
            "entries", List.of(
                Map.of("key", "value1", "info", "abc"),
                Map.of("key", "value2", "info", "def")
            )
        );

        Set<String> whitelist = Set.of("entries.*");

        Map<String, Object> result = WhitelistFilter.applyWhitelist(input, whitelist);

        Assertions.assertThat(result).containsOnlyKeys("entries");
        List<?> list = (List<?>) result.get("entries");
        Assertions.assertThat(list).hasSize(2);
        Assertions.assertThat((Map<String, ?>) list.get(0)).containsKeys("key", "info");
    }
	
}
