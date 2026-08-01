package com.condation.cms.templates.functions.impl;

/*-
 * #%L
 * CMS Templates
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

import com.condation.cms.api.feature.features.CurrentNodeFeature;
import com.condation.cms.api.feature.features.HookSystemFeature;
import com.condation.cms.api.feature.features.InjectorFeature;
import com.condation.cms.api.feature.features.RequestFeature;
import com.condation.cms.api.feature.features.SitePropertiesFeature;
import com.condation.cms.api.hooks.Hooks;
import com.condation.cms.api.menu.Menu;
import com.condation.cms.api.menu.MenuItem;
import com.condation.cms.api.menu.MenuService;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.utils.HTTPUtil;
import com.condation.cms.templates.functions.TemplateFunction;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Loads a site menu by id for use in templates.
 */
@Slf4j
@RequiredArgsConstructor
public class MenuFunction implements TemplateFunction {

	public static final String NAME = "menu";

	private final RequestContext requestContext;

	@Override
	public Object invoke(Object... params) {
		if (params == null || params.length == 0 || !(params[0] instanceof String menuId)) {
			return null;
		}

		try {
			MenuService menuService = requestContext.get(InjectorFeature.class)
					.injector()
					.getInstance(MenuService.class);
			return menuService.get(menuId)
					.map(this::withCurrentItems)
					.orElse(null);
		} catch (IOException | IllegalArgumentException exception) {
			log.error("Could not load menu '{}'", menuId, exception);
			return null;
		}
	}

	@Override
	public String name() {
		return NAME;
	}

	private Menu withCurrentItems(Menu menu) {
		Set<String> currentUrls = currentUrls();
		List<MenuItem> items = copyItems(menu.items(), currentUrls);
		if (requestContext.has(HookSystemFeature.class)) {
			items = requestContext.get(HookSystemFeature.class)
					.hookSystem()
					.doFilter(Hooks.MENU_FILTER.hook(menu.id()), items);
		}
		return new Menu(menu.id(), menu.name(), enabledItems(items));
	}

	private List<MenuItem> enabledItems(List<MenuItem> items) {
		return items.stream()
				.filter(MenuItem::enabled)
				.map(item -> new MenuItem(
						item.id(),
						item.type(),
						item.label(),
						item.url(),
						item.target(),
						true,
						enabledItems(item.children()),
						item.current()))
				.collect(Collectors.toCollection(ArrayList::new));
	}

	private List<MenuItem> copyItems(List<MenuItem> items, Set<String> currentUrls) {
		return items.stream()
				.map(item -> new MenuItem(
						item.id(),
						item.type(),
						item.label(),
						item.url(),
						item.target(),
						item.enabled(),
						copyItems(item.children(), currentUrls),
						isCurrent(item, currentUrls)))
				.collect(Collectors.toCollection(ArrayList::new));
	}

	private boolean isCurrent(MenuItem item, Set<String> currentUrls) {
		if (!"link".equals(item.type())) {
			return false;
		}
		String itemUrl = normalizeInternalUrl(item.url());
		return itemUrl != null && currentUrls.contains(itemUrl);
	}

	private Set<String> currentUrls() {
		Set<String> urls = new HashSet<>();
		if (requestContext.has(CurrentNodeFeature.class)) {
			String nodeUrl = requestContext.get(CurrentNodeFeature.class).node().url();
			addNormalized(urls, nodeUrl);
			if (nodeUrl != null && requestContext.has(SitePropertiesFeature.class)) {
				addNormalized(urls, HTTPUtil.modifyUrl(nodeUrl, requestContext));
			}
		}
		if (requestContext.has(RequestFeature.class)) {
			addNormalized(urls, requestContext.get(RequestFeature.class).uri());
		}
		return urls;
	}

	private void addNormalized(Set<String> urls, String url) {
		String normalized = normalizeInternalUrl(url);
		if (normalized != null) {
			urls.add(normalized);
		}
	}

	private String normalizeInternalUrl(String url) {
		if (url == null) {
			return null;
		}
		String normalized = url.trim();
		if (normalized.isEmpty()
				|| normalized.startsWith("//")
				|| normalized.contains(":")) {
			return null;
		}

		int queryIndex = normalized.indexOf('?');
		int fragmentIndex = normalized.indexOf('#');
		int suffixIndex = queryIndex < 0
				? fragmentIndex
				: fragmentIndex < 0 ? queryIndex : Math.min(queryIndex, fragmentIndex);
		if (suffixIndex >= 0) {
			normalized = normalized.substring(0, suffixIndex);
		}
		if (normalized.isEmpty()) {
			return null;
		}
		if (!normalized.startsWith("/")) {
			normalized = "/" + normalized;
		}
		while (normalized.length() > 1 && normalized.endsWith("/")) {
			normalized = normalized.substring(0, normalized.length() - 1);
		}
		return normalized;
	}
}
