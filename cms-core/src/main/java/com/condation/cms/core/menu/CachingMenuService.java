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

import com.condation.cms.api.cache.ICache;
import com.condation.cms.api.eventbus.EventBus;
import com.condation.cms.api.eventbus.events.MenuChangedEvent;
import com.condation.cms.api.menu.Menu;
import com.condation.cms.api.menu.MenuService;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Site-local menu service decorator that caches loaded menu documents.
 */
public class CachingMenuService implements MenuService {

	private final MenuService delegate;
	private final ICache<String, Menu> cache;
	private final EventBus eventBus;

	public CachingMenuService(MenuService delegate, ICache<String, Menu> cache, EventBus eventBus) {
		this.delegate = delegate;
		this.cache = cache;
		this.eventBus = eventBus;
		eventBus.register(MenuChangedEvent.class, event -> cache.invalidate(event.menuId()));
	}

	@Override
	public synchronized List<Menu> list() throws IOException {
		List<Menu> menus = delegate.list();
		menus.forEach(menu -> cache.put(menu.id(), menu));
		return menus;
	}

	@Override
	public synchronized Optional<Menu> get(String id) throws IOException {
		Menu cachedMenu = cache.get(id);
		if (cachedMenu != null) {
			return Optional.of(cachedMenu);
		}

		Optional<Menu> menu = delegate.get(id);
		menu.ifPresent(value -> cache.put(id, value));
		return menu;
	}

	@Override
	public Menu create(Menu menu) throws IOException {
		Menu created = delegate.create(menu);
		eventBus.syncPublish(new MenuChangedEvent(created.id()));
		return created;
	}

	@Override
	public Menu update(Menu menu) throws IOException {
		Menu updated = delegate.update(menu);
		eventBus.syncPublish(new MenuChangedEvent(updated.id()));
		return updated;
	}

	@Override
	public boolean delete(String id) throws IOException {
		boolean deleted = delegate.delete(id);
		if (deleted) {
			eventBus.syncPublish(new MenuChangedEvent(id));
		}
		return deleted;
	}
}
