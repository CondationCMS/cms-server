package com.condation.cms.api.workflow;

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

import com.condation.cms.api.db.ContentNode;

/**
 *
 * @author thorstenmarx
 */
public record WFTransition(
		String id, 
		String label,
		String toStage,
		WFTransitionAction action,
		WFTransitionGuard guard) {
	
	public WFTransition {
		if (action == null) {
			action = (node) -> {};
		}
		if (guard == null) {
			guard = (node) -> true;
		}
	}
	
	void execute (ContentNode node) {
		if (!guard.isAllowed(node)) {
			throw new WFTransitionException("transition not allowed");
		}
		action.execute(node);
	}
}
