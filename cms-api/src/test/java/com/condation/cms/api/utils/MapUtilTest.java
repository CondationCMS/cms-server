package com.condation.cms.api.utils;

/*-
 * #%L
 * cms-api
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


import com.condation.cms.api.utils.MapUtil;
import java.util.HashMap;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class MapUtilTest {
	

	@Test
	public void test_basic_override() {
		Map<String, Object> data = new HashMap<>();
		data.put("key1", "value1");
		
		MapUtil.deepMerge(data, Map.of("key1", "value2"));
		
		Assertions.assertThat(data).hasSize(1);
		Assertions.assertThat(data.get("key1")).isEqualTo("value2");
	}
	
	@Test
	public void test_deep_override() {
		Map<String, Object> data = new HashMap<>();
		data.put("sub", new HashMap<>(Map.of("key1", "value1")));
		
		MapUtil.deepMerge(data, Map.of("sub", Map.of("key1", "value2")));
		
		Assertions.assertThat(data.get("sub"))
				.asInstanceOf(InstanceOfAssertFactories.MAP)
				.hasSize(1);
		Assertions.assertThat(data.get("sub"))
				.asInstanceOf(InstanceOfAssertFactories.MAP)
				.extracting((map) -> map.get("key1"))
				.isEqualTo("value2");
	}
	
	@Test
	public void test_basic_extends() {
		Map<String, Object> data = new HashMap<>();
		data.put("key1", "value1");
		
		MapUtil.deepMerge(data, Map.of("key2", "value2"));
		
		Assertions.assertThat(data)
				.hasSize(2)
				.containsKeys("key1", "key2");
	}
	
	@Test
	public void test_deep_extends() {
		Map<String, Object> data = new HashMap<>();
		data.put("sub", new HashMap<>(Map.of("key1", "value1")));
		
		MapUtil.deepMerge(data, Map.of("sub", Map.of("key2", "value2")));
		
		Assertions.assertThat(data.get("sub"))
				.asInstanceOf(InstanceOfAssertFactories.MAP)
				.hasSize(2);
		Assertions.assertThat(data.get("sub"))
				.asInstanceOf(InstanceOfAssertFactories.MAP)
				.containsKeys("key1", "key2");
	}
}
