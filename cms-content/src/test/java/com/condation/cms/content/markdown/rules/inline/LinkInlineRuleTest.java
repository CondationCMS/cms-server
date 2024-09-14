package com.condation.cms.content.markdown.rules.inline;

/*-
 * #%L
 * cms-content
 * %%
 * Copyright (C) 2023 - 2024 CondationCMS
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

import com.condation.cms.api.SiteProperties;
import com.condation.cms.api.feature.features.SitePropertiesFeature;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.request.ThreadLocalRequestContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 *
 * @author t.marx
 */
@ExtendWith(MockitoExtension.class)
public class LinkInlineRuleTest {

	LinkInlineRule SUT = new LinkInlineRule();

	@Mock
	SiteProperties siteProperties;
	
	@Test
	public void test_link() {

		var result = SUT.next("[google](https://google.de)");

		Assertions.assertThat(result.render())
				.isEqualTo("<a href=\"https://google.de\" id=\"google\">google</a>");
	}

	@Test
	public void test_link_title() {

		var result = SUT.next("[google](https://google.de \"The Google\")");

		Assertions.assertThat(result.render())
				.isEqualTo("<a href=\"https://google.de\" id=\"google\" title=\"The Google\">google</a>");
	}

	@Test
	public void test_relativ_linking() {

		var result = SUT.next("[relative link](../sibling/test)");

		Assertions.assertThat(result.render())
				.isEqualTo("<a href=\"../sibling/test\" id=\"relative-link\">relative link</a>");
	}

	@Test
	public void test_relativ_linking_with_context() {

		try {
			
			Mockito.when(siteProperties.contextPath()).thenReturn("/de");
			
			RequestContext requestContext = new RequestContext();
			requestContext.add(SitePropertiesFeature.class, new SitePropertiesFeature(siteProperties));
			ThreadLocalRequestContext.REQUEST_CONTEXT.set(requestContext);
			
			var result = SUT.next("[relative link](../sibling/test)");

			Assertions.assertThat(result.render())
					.isEqualTo("<a href=\"../sibling/test\" id=\"relative-link\">relative link</a>");
		} finally {
			ThreadLocalRequestContext.REQUEST_CONTEXT.remove();
		}
	}
}
