package com.condation.cms.filesystem.usage;

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
