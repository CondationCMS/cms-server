package com.github.thmarx.cms.server.configs;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 Marx-Software
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
import com.github.thmarx.cms.api.ServerProperties;
import com.github.thmarx.cms.api.configuration.Configuration;
import com.github.thmarx.cms.api.db.DB;
import com.github.thmarx.cms.api.eventbus.EventBus;
import com.github.thmarx.cms.api.eventbus.events.SitePropertiesChanged;
import com.github.thmarx.cms.api.theme.Theme;
import com.github.thmarx.cms.media.MediaManager;
import com.github.thmarx.cms.server.jetty.FileFolderPathResource;
import com.github.thmarx.cms.server.handler.media.JettyMediaHandler;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.eclipse.jetty.server.handler.ResourceHandler;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class ThemeModule extends AbstractModule {

	@Provides
	@Singleton
	@Named("theme")
	public MediaManager themeMediaManager (Theme theme, Configuration configuration, DB db, EventBus eventBus) throws IOException {
		var mediaManager =  new MediaManager(theme.assetsPath(), db.getFileSystem().resolve("temp"), theme, configuration);
		eventBus.register(SitePropertiesChanged.class, mediaManager);
		return mediaManager;
	}
	
	@Provides
	@Singleton
	@Named("theme")
	public JettyMediaHandler themeMediaHandler(@Named("theme") MediaManager mediaManager) throws IOException {
		return new JettyMediaHandler(mediaManager);
	}

	@Provides
	@Singleton
	@Named("theme")
	public ResourceHandler resourceHandler(Theme theme, ServerProperties serverProperties) {
		ResourceHandler assetsHandler = new ResourceHandler();
		assetsHandler.setDirAllowed(false);
		assetsHandler.setBaseResource(new FileFolderPathResource(theme.assetsPath()));
		if (serverProperties.dev()) {
			assetsHandler.setCacheControl("no-cache");
		} else {
			assetsHandler.setCacheControl("max-age=" + TimeUnit.HOURS.toSeconds(24));
		}
		return assetsHandler;
	}
}
