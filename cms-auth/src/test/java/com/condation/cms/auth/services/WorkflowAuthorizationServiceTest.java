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
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class WorkflowAuthorizationServiceTest {
	private final WorkflowAuthorizationService service =
			new WorkflowAuthorizationService(new AuthorizationService());
	private final WFTransition edit = new WFTransition("review", "Review", "", "review", null, null);
	private final WFTransition publish = new WFTransition("release", "Release", "", "live", null, null,
			Set.of(Permissions.WORKFLOW_PUBLISH));

	@Test
	void editorCanExecuteRegularButNotPublishingTransition() {
		User editor = new User("editor", "hash", new String[]{"editor"});
		assertThat(service.canExecute(editor, edit)).isTrue();
		assertThat(service.canExecute(editor, publish)).isFalse();
		assertThat(service.allowedTransitions(editor, List.of(edit, publish))).containsExactly(edit);
	}

	@Test
	void managerCanExecutePublishingTransition() {
		User manager = new User("manager", "hash", new String[]{"manager"});
		assertThat(service.canExecute(manager, publish)).isTrue();
	}

	@Test
	void userWithoutWorkflowExecuteCannotExecuteAnyTransition() {
		User unknown = new User("unknown", "hash", new String[]{"unknown"});
		assertThat(service.allowedTransitions(unknown, List.of(edit, publish))).isEmpty();
	}
}
