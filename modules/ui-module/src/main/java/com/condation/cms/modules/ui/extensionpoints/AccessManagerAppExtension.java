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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.condation.cms.api.auth.Permissions;
import com.condation.cms.api.extensions.AbstractExtensionPoint;
import com.condation.cms.api.ui.action.UIScriptAction;
import com.condation.cms.api.ui.apps.App;
import com.condation.cms.api.ui.apps.AppExtensionPoint;
import com.condation.modules.api.annotation.Extension;
import java.util.List;
import java.util.Map;

/** Registers role and manager-user administration apps. */
@Extension(AppExtensionPoint.class)
public class AccessManagerAppExtension extends AbstractExtensionPoint implements AppExtensionPoint {
	@Override
	public List<App> getApps() {
		return List.of(
				new App("role-manager", "Roles", "/manager/assets/apps/role-manager.svg",
						new UIScriptAction("/manager/actions/access/manage-roles", Map.of()),
						List.of(Permissions.ROLE_MANAGE)),
				new App("user-manager", "Users", "/manager/assets/apps/user-manager.svg",
						new UIScriptAction("/manager/actions/access/manage-users", Map.of()),
						List.of(Permissions.USER_MANAGE)));
	}
}
