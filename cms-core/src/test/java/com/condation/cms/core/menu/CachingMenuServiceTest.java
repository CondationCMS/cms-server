package com.condation.cms.core.menu;

/*-
 * #%L
 * CMS Core
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.condation.cms.api.cache.CacheManager;
import com.condation.cms.api.eventbus.events.MenuChangedEvent;
import com.condation.cms.api.menu.Menu;
import com.condation.cms.api.menu.MenuService;
import com.condation.cms.core.cache.LocalCacheProvider;
import com.condation.cms.core.eventbus.DefaultEventBus;
import java.io.IOException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class CachingMenuServiceTest {

	@Test
	void cachesReadsAndInvalidatesTheChangedMenu() throws Exception {
		var delegate = new InMemoryMenuService();
		var eventBus = new DefaultEventBus();
		var cache = new LocalCacheProvider().<String, Menu>getCache(
				"menus",
				new CacheManager.CacheConfig(100L, Duration.ofMinutes(5)));
		var service = new CachingMenuService(delegate, cache, eventBus);
		var original = new Menu("main", "Main", List.of());
		delegate.create(original);

		Assertions.assertThat(service.get("main")).contains(original);
		Assertions.assertThat(service.get("main")).contains(original);
		Assertions.assertThat(delegate.getCalls).isEqualTo(1);

		var updated = new Menu("main", "Updated", List.of());
		service.update(updated);

		Assertions.assertThat(service.get("main")).contains(updated);
		Assertions.assertThat(delegate.getCalls).isEqualTo(2);

		eventBus.syncPublish(new MenuChangedEvent("main"));
		Assertions.assertThat(service.get("main")).contains(updated);
		Assertions.assertThat(delegate.getCalls).isEqualTo(3);
	}

	@Test
	void listPopulatesTheMenuCache() throws Exception {
		var delegate = new InMemoryMenuService();
		var eventBus = new DefaultEventBus();
		var cache = new LocalCacheProvider().<String, Menu>getCache(
				"menus",
				new CacheManager.CacheConfig(100L, Duration.ofMinutes(5)));
		var service = new CachingMenuService(delegate, cache, eventBus);
		var menu = new Menu("main", "Main", List.of());
		delegate.create(menu);

		Assertions.assertThat(service.list()).containsExactly(menu);
		Assertions.assertThat(service.get("main")).contains(menu);
		Assertions.assertThat(delegate.getCalls).isZero();
	}

	private static class InMemoryMenuService implements MenuService {

		private final Map<String, Menu> menus = new LinkedHashMap<>();
		private int getCalls;

		@Override
		public List<Menu> list() {
			return List.copyOf(menus.values());
		}

		@Override
		public Optional<Menu> get(String id) {
			getCalls++;
			return Optional.ofNullable(menus.get(id));
		}

		@Override
		public Menu create(Menu menu) throws IOException {
			if (menus.putIfAbsent(menu.id(), menu) != null) {
				throw new IOException("Menu already exists");
			}
			return menu;
		}

		@Override
		public Menu update(Menu menu) throws IOException {
			if (menus.replace(menu.id(), menu) == null) {
				throw new IOException("Menu does not exist");
			}
			return menu;
		}

		@Override
		public boolean delete(String id) {
			return menus.remove(id) != null;
		}
	}
}
