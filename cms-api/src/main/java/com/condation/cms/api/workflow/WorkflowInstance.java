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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author thorstenmarx
 */
public class WorkflowInstance implements Workflow {

	private final String id;
	private final String name;
	private final WFStatusProvider statusProvider;
	
	private final List<WFTransition> transitions = new ArrayList<>();
	
	public WorkflowInstance(String id, String name, WFStatusProvider statusProvider) {
		this.id = id;
		this.name = name;
		this.statusProvider = statusProvider;
	}
	
	public void addTransition (WFTransition transition) {
		if (findTransition(transition.id()).isPresent()) {
			throw new WFTransitionException("duplicated transition id");
		}
		transitions.add(transition);
	}
	
	@Override
	public WFStatusProvider getStatusProvider() {
		return statusProvider;
	}

	@Override
	public String getId() {
		return id;
	}
	
	

	@Override
	public List<WFTransition> getNextTransitions(ContentNode node) {
		return transitions.stream().filter(transition -> transition.guard().isAllowed(node)).toList();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void transit(String transitionId, ContentNode node) {
		var transition = findTransition(transitionId);
		if (transition.isEmpty()) {
			throw new WFTransitionException("unknown transition " + transitionId);
		}
		
		transition.get().execute(node);
	}
	
	private Optional<WFTransition> findTransition (String transitionId) {
		return transitions.stream().filter(transition -> transition.id().equals(transitionId)).findFirst();
	}
}
