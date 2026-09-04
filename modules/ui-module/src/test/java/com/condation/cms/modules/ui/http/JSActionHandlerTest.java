package com.condation.cms.modules.ui.http;

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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.nio.file.FileSystems;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class JSActionHandlerTest {

	@Test
	void loadsNestedCollectionActionFromBundledResources() {
		var handler = new JSActionHandler(
				FileSystems.getDefault(),
				"/manager/actions",
				mock(com.condation.cms.api.module.SiteModuleContext.class));

		assertThat(handler.getBundledScript("collection/manage-collection"))
				.hasValueSatisfying(script -> assertThat(script).contains("export const runAction"));
		assertThat(handler.getBundledScript("collection/edit-collection-item.js"))
				.hasValueSatisfying(script -> assertThat(script).contains("openCollectionItemEditor"));
	}
}
