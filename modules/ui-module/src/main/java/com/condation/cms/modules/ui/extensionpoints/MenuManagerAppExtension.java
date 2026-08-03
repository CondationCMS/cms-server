package com.condation.cms.modules.ui.extensionpoints;

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
import com.condation.cms.api.auth.Permissions;
import com.condation.cms.api.extensions.AbstractExtensionPoint;
import com.condation.cms.api.ui.action.UIScriptAction;
import com.condation.cms.api.ui.apps.App;
import com.condation.cms.api.ui.apps.AppExtensionPoint;
import com.condation.cms.api.ui.extensions.UIActionsExtensionPoint;
import com.condation.modules.api.annotation.Extension;
import java.util.List;
import java.util.Map;

/**
 * Makes the menu manager available as the first manager app.
 */
@Extension(AppExtensionPoint.class)
public class MenuManagerAppExtension extends AbstractExtensionPoint implements AppExtensionPoint, UIActionsExtensionPoint {

    @Override
    public List<App> getApps() {
        return List.of(new App(
                "menu-manager",
                "Menu Manager",
                "/manager/assets/apps/menu-manager.svg",
                new UIScriptAction("/manager/actions/menu/manage-menus", Map.of()),
                List.of(Permissions.MENU_MANAGE)));
    }
}
