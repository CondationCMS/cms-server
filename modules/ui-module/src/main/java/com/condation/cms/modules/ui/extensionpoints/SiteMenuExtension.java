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
import com.condation.cms.api.auth.Permissions;
import com.condation.cms.api.extensions.AbstractExtensionPoint;
import com.condation.cms.api.feature.features.InjectorFeature;
import com.condation.cms.api.site.Site;
import com.condation.cms.api.site.SiteService;
import com.condation.cms.api.ui.action.UIScriptAction;
import com.condation.cms.api.ui.annotations.HookAction;
import com.condation.cms.api.ui.annotations.MenuEntry;
import com.condation.cms.api.ui.annotations.ShortCut;
import com.condation.cms.api.ui.elements.Menu;
import com.condation.cms.api.ui.extensions.UIActionsExtensionPoint;
import com.condation.cms.api.utils.HTTPUtil;
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

	@MenuEntry(
			id = "toolMenu",
			name = "Tools",
			position = 10,
			permissions = {Permissions.CACHE_INVALIDATE}
	)
	public void parentDefinition() {

	}

	@MenuEntry(
			parent = "toolMenu",
			id = "media-cache-clear",
			name = "Clear media cache",
			permissions = {Permissions.CACHE_INVALIDATE},
			position = 1,
			hookAction = @HookAction(value = "ui/manager/tools/media/cache/clear")
	)
	@ShortCut(
			id = "media-cache-clear",
			title = "Clear media cache",
			permissions = {Permissions.CACHE_INVALIDATE},
			section = "tools",
			hookAction = @HookAction(value = "ui/manager/tools/media/cache/clear")
	)
	public void clear_media_cache() {
	}

	@MenuEntry(
			parent = "toolMenu",
			id = "content-cache-clear",
			name = "Clear content cache",
			permissions = {Permissions.CACHE_INVALIDATE},
			position = 2,
			hookAction = @HookAction(value = "ui/manager/tools/content/cache/clear")
	)
	@ShortCut(
			id = "content-cache-clear",
			title = "Clear content cache",
			permissions = {Permissions.CACHE_INVALIDATE},
			section = "tools",
			hookAction = @HookAction(value = "ui/manager/tools/content/cache/clear")
	)
	public void clear_content_cache() {
	}

	@MenuEntry(
			parent = "toolMenu",
			id = "template-cache-clear",
			name = "Clear template cache",
			permissions = {Permissions.CACHE_INVALIDATE},
			position = 3,
			hookAction = @HookAction(value = "ui/manager/tools/template/cache/clear")
	)
	@ShortCut(
			id = "template-cache-clear",
			title = "Clear template cache",
			permissions = {Permissions.CACHE_INVALIDATE},
			section = "tools",
			hookAction = @HookAction(value = "ui/manager/tools/template/cache/clear")
	)
	public void clear_template_cache() {
	}

	
	@Override
	public void addMenuItems(Menu menu) {
		menu.addMenuEntry(com.condation.cms.api.ui.elements.MenuEntry.builder()
				.id("site-menu")
				.name("Sites")
				.position(1)
				.permissions(List.of(Permissions.CONTENT_EDIT))
				.children(siteMenus())
				.build());
	}

	private List<com.condation.cms.api.ui.elements.MenuEntry> siteMenus() {
		var siteService = getContext().get(InjectorFeature.class).injector().getInstance(SiteService.class);

		var counter = new AtomicInteger(1);
		return new ArrayList<>(siteService.sites()
				.filter(site -> site.manager())
				.map(site -> {
			return com.condation.cms.api.ui.elements.MenuEntry.builder()
					.id("site-" + site.id())
					.name(site.id())
					.action(new UIScriptAction(
							HTTPUtil.modifyUrl("/manager/actions/site-change", getContext())
							, Map.of("href", site.realUrl() + "manager/index.html")))
					.position(counter.getAndIncrement())
					.build();
		}).toList());
	}
}
