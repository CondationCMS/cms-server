package com.condation.cms.core.serivce;

/*-
 * #%L
 * CMS Core
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
import com.condation.cms.api.repository.ContentDocument;
import com.condation.cms.api.repository.MutableContentRepository;
import com.condation.cms.core.serivce.impl.NodeAlternateService;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NodeAlternateServiceTest {

	@Test
	void addsAnAlternateUsingTheTargetSiteAsKey() throws Exception {
		var repository = mock(MutableContentRepository.class);
		var node = new ContentNode("about.md", "/about", "about.md", Map.of("title", "About"));
		when(repository.findByUrl("/about")).thenReturn(Optional.of(node));
		when(repository.load(node)).thenReturn(Optional.of(new ContentDocument(node, "body")));

		var service = new NodeAlternateService(repository);
		Assertions.assertThat(service.addAlternate("/about", "demo-de-site", "/ueber")).isTrue();

		var metadata = ArgumentCaptor.<Map<String, Object>>captor();
		verify(repository).save(eq("about.md"), metadata.capture(), eq("body"));
		Assertions.assertThat((Map<String, Object>) metadata.getValue().get(Constants.MetaFields.ALTERNATES))
				.containsEntry("demo-de-site", "/ueber");
	}

	@Test
	void removesTheAlternatesFieldWhenTheLastLinkIsRemoved() throws Exception {
		var repository = mock(MutableContentRepository.class);
		var node = new ContentNode(
				"about.md",
				"/about",
				"about.md",
				Map.of(Constants.MetaFields.ALTERNATES, Map.of("demo-de-site", "/ueber")));
		when(repository.findByUrl("/about")).thenReturn(Optional.of(node));
		when(repository.load(node)).thenReturn(Optional.of(new ContentDocument(node, "body")));

		var service = new NodeAlternateService(repository);
		Assertions.assertThat(service.removeAlternate("/about", "demo-de-site")).isTrue();

		var metadata = ArgumentCaptor.<Map<String, Object>>captor();
		verify(repository).save(eq("about.md"), metadata.capture(), eq("body"));
		Assertions.assertThat(metadata.getValue()).doesNotContainKey(Constants.MetaFields.ALTERNATES);
	}
}
