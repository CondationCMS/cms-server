package com.condation.cms.api.utils;

/*-
 * #%L
 * cms-api
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

import com.condation.cms.api.SiteProperties;
import com.condation.cms.api.feature.features.SitePropertiesFeature;
import com.condation.cms.api.request.RequestContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 *
 * @author thmar
 */
@ExtendWith(MockitoExtension.class)
public class ImageUtilTest {
	
	@Mock
	private RequestContext requestContext;
	
	@Test
	public void test_raw_path_default_context() {
		
		var siteProperties = Mockito.mock(SiteProperties.class);
		Mockito.when(siteProperties.contextPath()).thenReturn("/");
		Mockito.when(requestContext.get(SitePropertiesFeature.class)).thenReturn(new SitePropertiesFeature(siteProperties));
		
		Assertions.assertThat(ImageUtil.getRawPath("/test.jpg", requestContext)).isEqualTo("test.jpg");
		Assertions.assertThat(ImageUtil.getRawPath("/images/test.jpg", requestContext)).isEqualTo("images/test.jpg");
		
		Assertions.assertThat(ImageUtil.getRawPath("/assets/test.jpg", requestContext)).isEqualTo("test.jpg");
		Assertions.assertThat(ImageUtil.getRawPath("/assets/images/test.jpg", requestContext)).isEqualTo("images/test.jpg");
		
		Assertions.assertThat(ImageUtil.getRawPath("/media/test.jpg", requestContext)).isEqualTo("test.jpg");
		Assertions.assertThat(ImageUtil.getRawPath("/media/images/test.jpg", requestContext)).isEqualTo("images/test.jpg");
	}
	
	@Test
	public void test_raw_path_context() {
		
		var siteProperties = Mockito.mock(SiteProperties.class);
		Mockito.when(siteProperties.contextPath()).thenReturn("/de");
		Mockito.when(requestContext.get(SitePropertiesFeature.class)).thenReturn(new SitePropertiesFeature(siteProperties));
		
		Assertions.assertThat(ImageUtil.getRawPath("/de/test.jpg", requestContext)).isEqualTo("test.jpg");
		Assertions.assertThat(ImageUtil.getRawPath("/de/images/test.jpg", requestContext)).isEqualTo("images/test.jpg");
		
		Assertions.assertThat(ImageUtil.getRawPath("/de/assets/test.jpg", requestContext)).isEqualTo("test.jpg");
		Assertions.assertThat(ImageUtil.getRawPath("/de/assets/images/test.jpg", requestContext)).isEqualTo("images/test.jpg");
		
		Assertions.assertThat(ImageUtil.getRawPath("/de/media/test.jpg", requestContext)).isEqualTo("test.jpg");
		Assertions.assertThat(ImageUtil.getRawPath("/de/media/images/test.jpg", requestContext)).isEqualTo("images/test.jpg");
	}
}
