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
import com.condation.cms.api.ui.annotations.App;
import com.condation.cms.api.ui.annotations.ScriptAction;
import com.condation.cms.api.ui.annotations.ShortCut;
import com.condation.cms.api.ui.extensions.UIActionsExtensionPoint;
import com.condation.modules.api.annotation.Extension;

/** Registers role and manager-user administration apps. */
@Extension(UIActionsExtensionPoint.class)
public class AccessManagerAppExtension extends AbstractExtensionPoint implements UIActionsExtensionPoint {

	@App(
			id = "role-manager",
			title = "Roles",
			icon = "/manager/public/apps/role-manager.svg",
			permissions = Permissions.ROLE_MANAGE)
	@ShortCut(section = "Apps")
	@ScriptAction(module = "/manager/actions/access/manage-roles")
	public void manageRoles() {
		// Marker method for manager UI annotations.
	}

	@App(
			id = "user-manager",
			title = "Users",
			icon = "/manager/public/apps/user-manager.svg",
			permissions = Permissions.USER_MANAGE)
	@ShortCut(section = "Apps")
	@ScriptAction(module = "/manager/actions/access/manage-users")
	public void manageUsers() {
		// Marker method for manager UI annotations.
	}
}
