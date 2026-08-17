package com.condation.cms.modules.ui.utils;

/*-
 * #%L
 * UI Module
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
import com.condation.cms.api.SiteProperties;
import com.condation.cms.api.auth.Permissions;
import com.condation.cms.api.cache.CacheManager;
import com.condation.cms.api.hooks.FilterContext;
import com.condation.cms.api.hooks.HookSystem;
import com.condation.cms.api.module.SiteModuleContext;
import com.condation.cms.api.ui.action.UIScriptAction;
import com.condation.cms.api.ui.elements.Menu;
import com.condation.cms.api.ui.elements.MenuEntry;
import com.condation.cms.modules.ui.utils.ActionFactory.AppHolder;
import com.condation.cms.modules.ui.utils.template.UILinkFunction;
import com.condation.cms.auth.services.User;
import com.condation.cms.core.cache.LocalCacheProvider;
import com.condation.cms.hooksystem.CMSHookSystem;
import com.condation.modules.api.ModuleManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 *
 * @author thorstenmarx
 */
@ExtendWith(MockitoExtension.class)
public class TemplateEngineTest {

	@Mock
	private ModuleManager moduleManager;
	@Mock
    private SiteProperties siteProperties;
	@Mock
	private SiteModuleContext context;
    
    @Test
	public void testSomeMethod() {
		CacheManager cacheManager = new CacheManager(new LocalCacheProvider());
		TemplateEngine templateEngine = new TemplateEngine(cacheManager);
        
        
		var hookSystem = new CMSHookSystem();
		hookSystem.registerFilter("module/ui/menu", (FilterContext<Menu> context)
				-> {
			var menu = context.value();
			menu.addMenuEntry(MenuEntry.builder()
					.children(new ArrayList<>(
							List.of(
									MenuEntry.builder().id("child1").permissions(List.of(Permissions.CONTENT_EDIT)).name("Child 1").position(0).build(),
									MenuEntry.builder().id("div1").permissions(List.of(Permissions.CONTENT_EDIT)).divider(true).position(1).build(),
									MenuEntry.builder().id("child2").name("Child 2")
											.permissions(List.of(Permissions.CONTENT_EDIT))
											.position(2)
											.action(new UIScriptAction("module/ui/demo/menu/action", Map.of("name", "CondationCMS")))
											.build()
							)))
					.name("ExampleMenu")
					.id("example-menu")
					.build());

			return menu;
		}
		);

		Assertions.assertThatCode(() -> {
			templateEngine.render("test.html", Map.of("actionFactory", new ActionFactory(context, siteProperties, hookSystem, moduleManager, new User("test", "asdasdfasdf", new String[]{"manager"}))));
		}).doesNotThrowAnyException();
	}

	@Test
	void rendersAppLauncher() throws Exception {
		CacheManager cacheManager = new CacheManager(new LocalCacheProvider());
		TemplateEngine templateEngine = new TemplateEngine(cacheManager);
		ActionFactory actionFactory = Mockito.mock(ActionFactory.class);
		Mockito.when(actionFactory.createApps()).thenReturn(List.of(new AppHolder(
				"menu-manager",
				"Menu Manager",
				"/de/manager/public/apps/menu-manager.svg",
				new UIScriptAction("/de/manager/actions/menu/manage-menus", Map.of()))));
		Mockito.when(actionFactory.createContentTypeMenu()).thenReturn(new Menu());
		Mockito.when(actionFactory.createMenu()).thenReturn(new Menu());
		Mockito.when(actionFactory.createShortCuts()).thenReturn(List.of());
		UILinkFunction links = Mockito.mock(UILinkFunction.class);
		Mockito.when(links.createUrl(Mockito.anyString()))
				.thenAnswer(invocation -> invocation.getArgument(0));
		TranslationHelper translation = Mockito.mock(TranslationHelper.class);

		String result = templateEngine.render("index.html", Map.of(
				"actionFactory", actionFactory,
				"csrfToken", "csrf",
				"links", links,
				"managerBaseURL", "/de/manager",
				"previewToken", "preview",
				"contextPath", "/de",
				"siteId", "demo",
				"translation", translation));

		Assertions.assertThat(result)
				.contains("<script src=\"/manager/js/ui-actions.js\" type=\"module\"></script>")
				.contains("bi-grid-3x3-gap-fill")
				.contains("cms-app-card")
				.contains("/de/manager/public/apps/menu-manager.svg")
				.contains("/de/manager/actions/menu/manage-menus");
	}

}
