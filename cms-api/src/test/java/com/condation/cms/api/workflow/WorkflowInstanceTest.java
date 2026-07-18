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

import com.condation.cms.api.Constants;
import com.condation.cms.api.db.ContentNode;
import java.util.HashMap;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author thorstenmarx
 */
public class WorkflowInstanceTest {

	private WorkflowInstance wf;

	@BeforeEach
	void setup() {
		wf = new WorkflowInstance("simple", "Simple Workflow", new DefaultWFStatusProvider());

		wf.addTransition(new WFTransition(
				"publish",
				"Publish",
                "Sets the state of the node to published",
				"published",
				(node) -> node.data().put("status", DefaultWFStatusProvider.STATUS_PUBLISHED),
				(node) -> node.data().getOrDefault("status", DefaultWFStatusProvider.STATUS_DRAFT).equals(DefaultWFStatusProvider.STATUS_DRAFT)
		));

		wf.addTransition(new WFTransition(
				"unpublish",
				"Unpublish",
                "Sets the state of the node to draft",
				"draft",
				(node) -> node.data().put("status", DefaultWFStatusProvider.STATUS_DRAFT),
				(node) -> node.data().getOrDefault("status", DefaultWFStatusProvider.STATUS_DRAFT).equals(DefaultWFStatusProvider.STATUS_PUBLISHED)
		));
	}

	@Test
	void simple_wf_test () {
		ContentNode node = new ContentNode("/", "/", "Node", new HashMap<>());
		
		var transitions = wf.getNextTransitions(node);
		
		Assertions.assertThat(transitions).hasSize(1);
		Assertions.assertThat(transitions.getFirst().id()).isEqualTo("publish");
		
		wf.transit("publish", node);
		
		Assertions.assertThat(node.data().get(Constants.MetaFields.STATUS)).isNotNull().isEqualTo(DefaultWFStatusProvider.STATUS_PUBLISHED);
		
		transitions = wf.getNextTransitions(node);
		
		Assertions.assertThat(transitions).hasSize(1);
		Assertions.assertThat(transitions.getFirst().id()).isEqualTo("unpublish");
		
		wf.transit("unpublish", node);
		
		Assertions.assertThat(node.data().get(Constants.MetaFields.STATUS)).isNotNull().isEqualTo(DefaultWFStatusProvider.STATUS_DRAFT);
	}

	@Test
	void transit_with_unknown_id_throws() {
		ContentNode node = new ContentNode("/", "/", "Node", new HashMap<>());

		Assertions.assertThatThrownBy(() -> wf.transit("nonexistent", node))
				.isInstanceOf(WFTransitionException.class)
				.hasMessageContaining("nonexistent");
	}

	@Test
	void transit_blocked_by_guard_throws() {
		// node is in draft — "unpublish" guard requires published status
		ContentNode node = new ContentNode("/", "/", "Node", new HashMap<>());

		Assertions.assertThatThrownBy(() -> wf.transit("unpublish", node))
				.isInstanceOf(WFTransitionException.class)
				.hasMessageContaining("not allowed");
	}

	@Test
	void adding_duplicate_transition_id_throws() {
		Assertions.assertThatThrownBy(() ->
				wf.addTransition(new WFTransition(
						"publish",
						"Duplicate Publish",
                        "the description",
						"published",
						(node) -> {},
						null
				))
		).isInstanceOf(WFTransitionException.class);
	}

}
