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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.condation.cms.api.SiteProperties;
import com.condation.cms.api.auth.Permissions;
import com.condation.cms.api.feature.features.SitePropertiesFeature;
import com.condation.cms.api.module.SiteModuleContext;
import com.condation.cms.api.ui.action.UIScriptAction;
import com.condation.cms.api.ui.apps.App;
import com.condation.cms.api.ui.apps.AppExtensionPoint;
import com.condation.cms.auth.services.User;
import com.condation.modules.api.ModuleManager;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ActionFactoryAppsTest {

	@Test
	void createsAuthorizedAppsWithContextAwareIconAndScriptAction() {
		SiteProperties siteProperties = Mockito.mock(SiteProperties.class);
		Mockito.when(siteProperties.contextPath()).thenReturn("/de");
		SiteModuleContext context = Mockito.mock(SiteModuleContext.class);
		Mockito.when(context.get(SitePropertiesFeature.class))
				.thenReturn(new SitePropertiesFeature(siteProperties));

		AppExtensionPoint extension = Mockito.mock(AppExtensionPoint.class);
		Mockito.when(extension.getApps()).thenReturn(List.of(
				new App(
						"menu-manager",
						"Menu Manager",
						"/manager/public/apps/menu-manager.svg",
						new UIScriptAction("/manager/actions/menu/manage-menus", Map.of()),
						List.of(Permissions.CONTENT_EDIT)),
				new App(
						"admin-only",
						"Admin only",
						"/manager/public/apps/admin.svg",
						new UIScriptAction("/manager/actions/admin", Map.of()),
						List.of(Permissions.CACHE_INVALIDATE))));
		ModuleManager moduleManager = Mockito.mock(ModuleManager.class);
		Mockito.when(moduleManager.extensions(AppExtensionPoint.class))
				.thenReturn(List.of(extension));

		ActionFactory factory = new ActionFactory(
				context,
				siteProperties,
				null,
				moduleManager,
				new User("editor", "hash", new String[]{"editor"}));

		Assertions.assertThat(factory.createApps()).singleElement().satisfies(app -> {
			Assertions.assertThat(app.id()).isEqualTo("menu-manager");
			Assertions.assertThat(app.icon()).isEqualTo("/de/manager/public/apps/menu-manager.svg");
			Assertions.assertThat(app.action()).isInstanceOfSatisfying(
					UIScriptAction.class,
					action -> Assertions.assertThat(action.getModule())
							.isEqualTo("/de/manager/actions/menu/manage-menus"));
		});
	}
}
