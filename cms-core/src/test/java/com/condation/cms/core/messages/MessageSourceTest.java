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
import com.google.common.base.Stopwatch;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

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
public class MessageSourceTest {

	private static DefaultMessageSource messageSource;

	@Mock
	private ExtendedSiteProperties siteProperties;

	CacheManager cacheManager = new CacheManager(new LocalCacheProvider());

	@BeforeEach
	public void setup() {
		Mockito.when(siteProperties.locale()).thenReturn(Locale.getDefault());

		messageSource = new DefaultMessageSource(
				siteProperties,
				Path.of("src/test/resources/messages"),
				cacheManager.get("messages", new CacheManager.CacheConfig(10l, Duration.ofMinutes(1))));
	}

	@Test
	public void bundle_not_found() {
		var label = messageSource.getLabel("wrong_bundle", "a.label");
		Assertions.assertThat(label).isEqualTo("[a.label]");
	}

	@Test
	public void lable_not_found() {
		var label = messageSource.getLabel("abundle", "wrong.label");
		Assertions.assertThat(label).isEqualTo("[wrong.label]");
	}

	@Test
	public void lable_found() {
		var label = messageSource.getLabel("abundle", "button.submit");
		Assertions.assertThat(label).isEqualTo("Absenden");
	}

	@Test
	public void simple_performance() throws MalformedURLException {
		Stopwatch stopwatch = Stopwatch.createStarted();
		for (int i = 0; i < 1000; i++) {
			var label = messageSource.getLabel("abundle", "button.submit");
			Assertions.assertThat(label).isEqualTo("Absenden");
		}
		System.out.println("took %d ms".formatted(stopwatch.elapsed(TimeUnit.MILLISECONDS)));
		stopwatch.stop();
	}
}
