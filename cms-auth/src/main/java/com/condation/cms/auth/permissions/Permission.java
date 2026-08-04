package com.condation.cms.auth.permissions;

/*-
 * #%L
 * CMS Auth
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

/**
 *
 * @author thmar
 */
public record Permission(String key, String description) {
	public static final Permission CONTENT_EDIT = new Permission("content.edit", "Edit content");
	public static final Permission CACHE_INVALIDATE = new Permission("cache.invalidate", "Invalidate caches");
	public static final Permission USER_MANAGE = new Permission("user.manage", "Manage manager users");
	public static final Permission ROLE_MANAGE = new Permission("role.manage", "Manage roles and permissions");
	public static final Permission MENU_MANAGE = new Permission("menu.manage", "Manage navigation menus");
	public static final Permission WORKFLOW_EXECUTE = new Permission("workflow.execute", "Execute workflow transitions");
	public static final Permission WORKFLOW_PUBLISH = new Permission("workflow.publish", "Execute transitions that publish content");
}
