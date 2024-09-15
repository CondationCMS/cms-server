package com.condation.cms.api.hooks;

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


import com.condation.cms.api.hooks.HookSystem;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class HookSystemTest {

	private HookSystem hookSystem;
	
	@BeforeEach
	public void setup() {
		hookSystem = new HookSystem();
	}

	@Test
	public void test_single_result() {
		hookSystem.registerAction("test/test1", (context) -> {
			return true;
		});
		var context = hookSystem.execute("test/test1");
		Assertions.assertThat(context.results()).hasSize(1).contains(true);
	}
	
	@Test
	public void test_multiple_result() {
		hookSystem.registerAction("test/test1", (context) -> {
			return "test1";
		});
		hookSystem.registerAction("test/test1", (context) -> {
			return "test2";
		});
		var context = hookSystem.execute("test/test1");
		Assertions.assertThat(context.results()).hasSize(2).contains("test1", "test2");
	}
	
	@Test
	public void test_multiple_result_with_priority() {
		hookSystem.registerAction("test/test1", (context) -> {
			return "test3";
		}, 300);
		hookSystem.registerAction("test/test1", (context) -> {
			return "test1";
		}, 100);
		hookSystem.registerAction("test/test1", (context) -> {
			return "test2";
		}, 200);
		var context = hookSystem.execute("test/test1");
		Assertions.assertThat(context.results()).hasSize(3).containsExactly("test1", "test2", "test3");
	}
	
	@Test
	public void test_multiple_result_with_priority_reversed() {
		hookSystem.registerAction("test/test1", (context) -> {
			return "test3";
		}, 100);
		hookSystem.registerAction("test/test1", (context) -> {
			return "test1";
		}, 300);
		hookSystem.registerAction("test/test1", (context) -> {
			return "test2";
		}, 200);
		var context = hookSystem.execute("test/test1");
		Assertions.assertThat(context.results()).hasSize(3).containsExactly("test3", "test2", "test1");
	}
	
	@Test
	public void test_filter_reversed () {
		hookSystem.registerFilter("test/list", (context) -> ((List<String>)context.value()).reversed());
		var context = hookSystem.filter("test/list", List.of("1", "2", "3"));
		Assertions.assertThat(context.value()).containsExactly("3", "2", "1");
	}
	
	@Test
	public void test_filter_remove () {
		hookSystem.registerFilter("test/list", (FilterContext<List<String>> context) -> {
			context.value().remove("2");
			return context.value();
		});
		var context = hookSystem.filter("test/list", new ArrayList<>(List.of("1", "2", "3")));
		Assertions.assertThat(context.value()).containsExactly("1", "3");
	}
}
