package com.condation.cms.filesystem.usage;

/*-
 * #%L
 * cms-filesystem
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

import com.condation.cms.api.configuration.Configuration;
import com.condation.cms.api.configuration.configs.SiteConfiguration;
import com.condation.cms.api.content.DefaultContentResponse;
import com.condation.cms.api.content.RenderContentFunction;
import com.condation.cms.api.eventbus.EventListener;
import com.condation.cms.api.eventbus.events.ContentChangedEvent;
import com.condation.cms.api.feature.features.ContentRenderFeature;
import com.condation.cms.api.feature.features.SitePropertiesFeature;
import com.condation.cms.api.utils.HTTPUtil;
import com.condation.cms.api.utils.PathUtil;
import com.google.inject.Injector;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;

@Slf4j
@RequiredArgsConstructor
public class UsageIndexContentUpdater implements EventListener<ContentChangedEvent> {

    private final UsageIndex usageIndex;

	private final Injector injector;
	
	private final Path contentBase;
	
    @Override
    public void consum(ContentChangedEvent event) {
        log.info("processing {}", event.contentPath());

        var renderedContent = renderContent(event.contentPath());
		if (renderedContent.isEmpty()) {
			return;
		}
        var doc = Jsoup.parse(renderedContent.get().content());
        doc.select("a[href]").forEach(element -> {
            var href = element.attr("href");
            try {
                usageIndex.addUsage(new UsageIndex.Reference(event.contentPath().toString(), "page", href, "page", "link"));
            } catch (Exception e) {
                log.error("error adding usage", e);
            }
        });
    }

	
	
    private Optional<DefaultContentResponse> renderContent(Path path) {
		var uri = "/" + PathUtil.toRelativeFile(path, contentBase);

		uri = uri.substring(0, uri.lastIndexOf("."));
		
		var contentResponse = injector.getInstance(RenderContentFunction.class).render(uri, Collections.emptyMap());		
		return Optional.ofNullable((DefaultContentResponse) contentResponse.get());
	}
	
	private String uri (Path contentFile) {
		var uri = PathUtil.toURL(contentFile, contentBase);
		return HTTPUtil.modifyUrl(uri, injector.getInstance(Configuration.class).get(SiteConfiguration.class).siteProperties());
	}
}
