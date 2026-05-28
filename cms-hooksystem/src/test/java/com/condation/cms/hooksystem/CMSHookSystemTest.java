package com.condation.cms.hooksystem;

/*-
 * #%L
 * CMS Api
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */


import com.condation.cms.api.annotations.Filter;
import com.condation.cms.api.annotations.Action;
import com.condation.cms.api.annotations.Param;
import com.condation.cms.api.hooks.ActionContext;
import com.condation.cms.api.hooks.FilterContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class CMSHookSystemTest {

	private CMSHookSystem hookSystem;
	
	@BeforeEach
	public void setup() {
		hookSystem = new CMSHookSystem();
	}

	@Test
	public void test_single_result() {
		hookSystem.registerAction("test/test1", (context) -> {
			return true;
		});
		var context = hookSystem.doAction("test/test1");
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
		var context = hookSystem.doAction("test/test1");
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
		var context = hookSystem.doAction("test/test1");
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
		var context = hookSystem.doAction("test/test1");
		Assertions.assertThat(context.results()).hasSize(3).containsExactly("test3", "test2", "test1");
	}
	
	@Test
	public void test_filter_reversed () {
		hookSystem.registerFilter("test/list", (context) -> ((List<String>)context.value()).reversed());
		var context = hookSystem.doFilter("test/list", List.of("1", "2", "3"));
		Assertions.assertThat(context.value()).containsExactly("3", "2", "1");
	}
	
	@Test
	public void test_filter_remove () {
		hookSystem.registerFilter("test/list", (FilterContext<List<String>> context) -> {
			context.value().remove("2");
			return context.value();
		});
		var context = hookSystem.doFilter("test/list", new ArrayList<>(List.of("1", "2", "3")));
		Assertions.assertThat(context.value()).containsExactly("1", "3");
	}
	
	@Test
	void test_action_annotation () {
		var actionObject = new MyActions();

		hookSystem.register(actionObject);

		hookSystem.doAction("test/annotation/action1");

		Assertions.assertThat(actionObject.counter).hasValue(2);
	}

	@Test
	void test_filter_annotations () {
		var myFilters = new MyFilters();

		hookSystem.register(myFilters);

		var context = hookSystem.doFilter("test/annotation/filter1", new ArrayList<>(List.of("1", "2", "3")));
		Assertions.assertThat(context.value()).containsExactly("1", "3");
	}

	@Test
	void test_action_named_params () {
		var handler = new MyNamedParamActions();

		hookSystem.register(handler);

		var context = hookSystem.doAction("test/named/action1", Map.of("name", "World", "count", 3));
		Assertions.assertThat(context.results()).hasSize(1).containsExactly("Hello World 3");
	}

	@Test
	void test_filter_direct_value () {
		var handler = new MyDirectValueFilters();

		hookSystem.register(handler);

		var context = hookSystem.doFilter("test/direct/filter1", new ArrayList<>(List.of("1", "2", "3")));
		Assertions.assertThat(context.value()).containsExactly("1", "3");
	}

	public class MyFilters {
		@Filter("test/annotation/filter1")
		public List<String> filter (FilterContext<List<String>> context) {
			context.value().remove("2");
			return context.value();
		}
	}

	public class MyActions {

		private AtomicInteger counter = new AtomicInteger(0);

		@Action("test/annotation/action1")
		public void action1 (ActionContext<?> context) {
			counter.incrementAndGet();
		}
		@Action("test/annotation/action1")
		public void action2 (ActionContext<?> context) {
			counter.incrementAndGet();
		}
	}

	public class MyNamedParamActions {
		@Action("test/named/action1")
		public String greet (@Param("name") String name, @Param("count") Integer count) {
			return "Hello " + name + " " + count;
		}
	}

	public class MyDirectValueFilters {
		@Filter("test/direct/filter1")
		public List<String> filter (List<String> values) {
			values.remove("2");
			return values;
		}
	}
}
