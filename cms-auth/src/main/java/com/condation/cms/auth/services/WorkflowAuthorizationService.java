package com.condation.cms.auth.services;

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

import com.condation.cms.api.auth.Permissions;
import com.condation.cms.api.workflow.WFTransition;
import java.util.List;

/** Applies the common workflow permission and transition-specific permissions. */
public class WorkflowAuthorizationService {
	private final AuthorizationService authorizationService;

	public WorkflowAuthorizationService(AuthorizationService authorizationService) {
		this.authorizationService = authorizationService;
	}

	public boolean canExecute(User user, WFTransition transition) {
		return user != null
				&& transition != null
				&& authorizationService.hasPermission(user, Permissions.WORKFLOW_EXECUTE)
				&& authorizationService.hasAllPermissions(
						user, transition.permissions().toArray(String[]::new));
	}

	public List<WFTransition> allowedTransitions(User user, List<WFTransition> transitions) {
		if (transitions == null) return List.of();
		return transitions.stream().filter(transition -> canExecute(user, transition)).toList();
	}
}
