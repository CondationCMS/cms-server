package com.condation.cms.modules.ui.utils;

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

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class RoleUtil {

	public static boolean hasAccess(String[] annotationRoles, String[] userRoles) {
		return hasAccess(Arrays.asList(annotationRoles), Arrays.asList(userRoles));
	}

	public static boolean hasAccess(List<String> roles, String[] userRoles) {
		return hasAccess(roles, Arrays.asList(userRoles));
	}
	
	/**
	 * Checks whether the user has at least one role that is allowed by the
	 * annotation.
	 *
	 * @param annotationRoles The roles defined in the annotation (e.g.,
	 * {"admin", "editor"}).
	 * @param userRoles The roles assigned to the current user (e.g.,
	 * List.of("editor", "user")).
	 * @return true if the user has access, false otherwise.
	 */
	public static boolean hasAccess(List<String> annotationRoles, List<String> userRoles) {
		return annotationRoles.stream()
				.anyMatch(allowed -> userRoles.contains(allowed));
	}
}
