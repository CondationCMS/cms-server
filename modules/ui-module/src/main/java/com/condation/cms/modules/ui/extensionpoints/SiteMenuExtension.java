package com.condation.cms.modules.ui.extensionpoints;

/*-
 * #%L
 * ui-module
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
import com.condation.cms.api.extensions.AbstractExtensionPoint;
import com.condation.cms.api.feature.features.InjectorFeature;
import com.condation.cms.api.site.Site;
import com.condation.cms.api.site.SiteService;
import com.condation.cms.api.ui.action.UIScriptAction;
import com.condation.cms.api.ui.elements.Menu;
import com.condation.cms.api.ui.elements.MenuEntry;
import com.condation.cms.api.ui.extensions.UIActionsExtensionPoint;
import com.condation.modules.api.annotation.Extension;
import com.condation.modules.api.annotation.Extensions;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author t.marx
 */
@Extensions({
	@Extension(UIActionsExtensionPoint.class),})
public class SiteMenuExtension extends AbstractExtensionPoint implements UIActionsExtensionPoint {

	@Override
	public void addMenuItems(Menu menu) {
		menu.addMenuEntry(MenuEntry.builder()
				.id("site-menu")
				.name("Sites")
				.position(1)
				.roles(List.of("admin"))
				.children(siteMenus())
				.build());
	}

	private List<MenuEntry> siteMenus() {
		var siteService = getContext().get(InjectorFeature.class).injector().getInstance(SiteService.class);

		var counter = new AtomicInteger(1);
		return new ArrayList<>(siteService.sites().map(site -> {
			return MenuEntry.builder()
					.id("site-" + site.id())
					.name(site.id())
					.position(counter.getAndIncrement())
					.children(siteSubMenu(site))
					.build();
		}).toList());
	}

	private List<MenuEntry> siteSubMenu(Site site) {
		List<MenuEntry> entries = new ArrayList<>();

		var url = site.baseurl().endsWith("/") ? site.baseurl() : site.baseurl() + "/";
		entries.add(MenuEntry.builder()
				.id("www-" + site.id())
				.name("Website")
				.action(new UIScriptAction("/manager/actions/site-change", Map.of("href", url)))
				.position(1)
				.build());

		if (site.manager()) {
			entries.add(MenuEntry.builder()
					.id("manager-" + site.id())
					.name("Manager")
					.action(new UIScriptAction("/manager/actions/site-change", Map.of("href", url + "manager/index.html")))
					.position(2)
					.build());
		}

		return entries;
	}

}
