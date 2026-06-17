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
import com.condation.cms.api.ServerProperties;
import com.condation.cms.api.configuration.Configuration;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.eventbus.EventBus;
import com.condation.cms.api.eventbus.events.ConfigurationReloadEvent;
import com.condation.cms.api.theme.Theme;
import com.condation.cms.media.ThemeMediaManager;
import com.condation.cms.server.handler.media.JettyMediaHandler;
import com.condation.cms.server.FileFolderPathResource;
import com.condation.cms.server.handler.StaticFileHandler;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.ResourceFactory;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class ThemeModule extends AbstractModule {

    @Provides
    @Singleton
    public ThemeMediaManager themeMediaManager(Theme theme, Configuration configuration, DB db, EventBus eventBus) throws IOException {
        var mediaManager = new ThemeMediaManager(db.getFileSystem().resolve("temp"), theme, configuration);
        eventBus.register(ConfigurationReloadEvent.class, mediaManager);
        return mediaManager;
    }

    @Provides
    @Singleton
    @Named("theme.media")
    public JettyMediaHandler themeMediaHandler(ThemeMediaManager mediaManager) throws IOException {
        return new JettyMediaHandler(mediaManager);
    }

    @Provides
    @Singleton
    @Named("theme.assets")
    public ResourceHandler themeAssetsHandler(Theme theme, ServerProperties serverProperties) {
        ResourceHandler assetsHandler = new ResourceHandler();
        assetsHandler.setDirAllowed(false);
        assetsHandler.setEtags(true);

        if (theme.getParentTheme() != null) {
            assetsHandler.setBaseResource(ResourceFactory.combine(
                    new FileFolderPathResource(theme.assetsPath()),
                    new FileFolderPathResource(theme.getParentTheme().assetsPath())
            ));
        } else {
            assetsHandler.setBaseResource(new FileFolderPathResource(theme.assetsPath()));
        }

        if (serverProperties.dev()) {
            assetsHandler.setCacheControl("no-cache");
        } else {
            assetsHandler.setCacheControl(
                "public, max-age=0, must-revalidate"
        );
        }
        return assetsHandler;
    }

    @Provides
    @Singleton
    @Named("theme.public")
    public StaticFileHandler themePublicHandler(Theme theme, ServerProperties serverProperties) {
        List<Path> paths = new ArrayList<>();
        paths.add(theme.publicPath());
        
        if (theme.getParentTheme() != null) {
            paths.add(theme.getParentTheme().publicPath());
        }

        return new StaticFileHandler(paths);
    }
}
