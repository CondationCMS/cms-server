package com.condation.cms.api.auth;

/*-
 * #%L
 * CMS Api
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
public class Permissions {
	public static final String CONTENT_EDIT = "content.edit";
	public static final String CACHE_INVALIDATE = "cache.invalidate";
	public static final String USER_MANAGE = "user.manage";
	public static final String ROLE_MANAGE = "role.manage";
	public static final String MENU_MANAGE = "menu.manage";
	public static final String WORKFLOW_EXECUTE = "workflow.execute";
	public static final String WORKFLOW_PUBLISH = "workflow.publish";

	private Permissions() {
	}
}
