package com.condation.cms.core.messages;

/*-
 * #%L
 * cms-core
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


import com.condation.cms.api.cache.CacheManager;
import com.condation.cms.core.cache.LocalCacheProvider;
import com.condation.cms.core.configuration.properties.ExtendedSiteProperties;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Locale;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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
public class ThemeMessageSourceTest {

	private static DefaultMessageSource messageSource;
	private static ThemeMessageSource themeMessageSource;
	
	@Mock
	ExtendedSiteProperties siteProperties;
	
	CacheManager cacheManager = new CacheManager(new LocalCacheProvider());
	
	@BeforeEach
	public void setup() {
		Mockito.when(siteProperties.locale()).thenReturn(Locale.getDefault());
		messageSource = new DefaultMessageSource(
				siteProperties, 
				Path.of("src/test/resources/messages"),
				cacheManager.get("messages", new CacheManager.CacheConfig(10l, Duration.ofMinutes(1)))
		);
		
		themeMessageSource = new ThemeMessageSource(
				siteProperties, 
				Path.of("src/test/resources/parent_messages"), 
				messageSource,
				cacheManager.get("theme-messages", new CacheManager.CacheConfig(10l, Duration.ofMinutes(1)))
		);
	}

	@Test
	public void from_child() {
		var label = themeMessageSource.getLabel("abundle", "message.child");
		Assertions.assertThat(label).isEqualTo("hello child");
	}
	
	@Test
	public void from_parent() {
		var label = themeMessageSource.getLabel("abundle", "message.parent");
		Assertions.assertThat(label).isEqualTo("hello parent");
	}
	
	@Test
	public void override_parent() {
		var label = themeMessageSource.getLabel("abundle", "parent");
		Assertions.assertThat(label).isEqualTo("I override the parent");
	}
}
