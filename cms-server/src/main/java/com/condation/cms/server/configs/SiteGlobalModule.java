package com.condation.cms.server.configs;

/*-
 * #%L
 * CMS Server
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
import com.condation.cms.api.SiteProperties;
import com.condation.cms.api.cache.CacheManager;
import com.condation.cms.api.cache.CacheProvider;
import com.condation.cms.api.extensions.CacheProviderExtensionPoint;
import com.condation.cms.api.scheduler.CronJobContext;
import com.condation.cms.core.cache.LocalCacheProvider;
import com.condation.cms.core.scheduler.SiteCronJobScheduler;
import com.condation.modules.api.ModuleManager;
import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;

/**
 *
 * @author t.marx
 */
@Slf4j
public class SiteGlobalModule implements com.google.inject.Module {

	@Override
	public void configure(Binder binder) {

	}
	
	@Provides
	@Singleton
	public SiteCronJobScheduler cronJobScheduler (Scheduler scheduler, CronJobContext context, SiteProperties siteProperties) {
		return new SiteCronJobScheduler(scheduler, context, siteProperties);
	}
	
	@Provides
	@Singleton
	public CacheManager cacheManager (CacheProvider cacheProvider) {
		return new CacheManager(cacheProvider);
	}
	
	@Provides
	@Singleton
	public CacheProvider cacheProvider (ModuleManager moduleManager, SiteProperties siteProperties) {
		var cacheEngine = siteProperties.cacheEngine();
		if (Constants.DEFAULT_CACHE_ENGINE.equals(cacheEngine)) {
			return new LocalCacheProvider();
		}
		List<CacheProviderExtensionPoint> extensions = moduleManager.extensions(CacheProviderExtensionPoint.class);
		Optional<CacheProviderExtensionPoint> extOpt = extensions.stream().filter((ext) -> ext.getName().equals(cacheEngine)).findFirst();

		if (extOpt.isPresent()) {
			return extOpt.get().getCacheProvider();
		}
		return new LocalCacheProvider();
	}

	@Provides
	@Singleton
	public SiteConfigInitializer siteConfigInitializer (Injector injector) {
		var configInitializer = new SiteConfigInitializer(injector);
		return configInitializer;
	}
}
