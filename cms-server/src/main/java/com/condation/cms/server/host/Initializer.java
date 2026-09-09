package com.condation.cms.server.host;

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

import com.condation.cms.api.configuration.Configuration;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.eventbus.EventBus;
import com.condation.cms.api.eventbus.events.CollectionChangedEvent;
import com.condation.cms.api.eventbus.events.ConfigurationReloadEvent;
import com.condation.cms.api.eventbus.events.ContentChangedEvent;
import com.condation.cms.api.eventbus.events.ContentTypesChangedEvent;
import com.condation.cms.api.eventbus.events.lifecycle.HostReadyEvent;
import com.condation.cms.api.eventbus.events.lifecycle.HostReloadedEvent;
import com.condation.cms.api.eventbus.events.lifecycle.HostStoppedEvent;
import com.condation.cms.content.usage.EditorialUsageIndex;
import com.condation.cms.core.serivce.ServiceRegistry;
import com.condation.cms.core.serivce.impl.NodeTranslationService;
import com.condation.cms.core.serivce.impl.SiteDBService;
import com.condation.cms.core.serivce.impl.SiteLinkService;
import com.condation.cms.core.serivce.impl.SitePropertiesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author thorstenmarx
 */
@Slf4j
@RequiredArgsConstructor
public class Initializer {

	private final VHost host;

	void initServices () {
		var db = host.injector.getInstance(DB.class);
		ServiceRegistry.getInstance().register(host.id(), SiteDBService.class, new SiteDBService(db));
		
		var config = host.injector.getInstance(Configuration.class);
		ServiceRegistry.getInstance().register(host.id(), SiteLinkService.class, new SiteLinkService(config));
		
		ServiceRegistry.getInstance().register(host.id(), SitePropertiesService.class, new SitePropertiesService(config));
		
		ServiceRegistry.getInstance().register(host.id(), NodeTranslationService.class, new NodeTranslationService(db, host.injector.getInstance(EventBus.class)));
		initUsageIndex();
	}

	private void initUsageIndex() {
		var index = host.injector.getInstance(EditorialUsageIndex.class);
		var events = host.injector.getInstance(EventBus.class);
		events.register(ContentChangedEvent.class,
				event -> index.refresh(event.contentPath()));
		events.register(CollectionChangedEvent.class,
				event -> index.refresh(event.path()));
		events.register(HostReadyEvent.class, event -> index.synchronize());
		events.register(HostReloadedEvent.class, event -> index.synchronize());
		events.register(ConfigurationReloadEvent.class, event -> index.synchronize());
		events.register(ContentTypesChangedEvent.class, event -> index.synchronize());
		events.register(HostStoppedEvent.class, event -> index.close());
	}
}
