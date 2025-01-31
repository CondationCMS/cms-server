package com.condation.cms.templates;

/*-
 * #%L
 * templates
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
import com.condation.cms.api.cache.CacheProvider;
import com.condation.cms.core.cache.LocalCacheProvider;
import java.time.Duration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

/**
 *
 * @author t.marx
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractTemplateEngineTest {
	
	private CacheProvider cacheProvider = new LocalCacheProvider();
	
	protected CMSTemplateEngine SUT;
	
	public abstract TemplateLoader getLoader ();
	
	public boolean isDevMode () {
		return true;
	}
	
	@BeforeAll
	public void setup () {
		SUT = TemplateEngineFactory
				.newInstance(getLoader())
				.cache(cacheProvider.getCache("templates", new CacheManager.CacheConfig(100l, Duration.ofSeconds(60))))
				.defaultFilters()
				.defaultTags()
				.devMode(isDevMode())
				.create();
		
				
				
	}
}
