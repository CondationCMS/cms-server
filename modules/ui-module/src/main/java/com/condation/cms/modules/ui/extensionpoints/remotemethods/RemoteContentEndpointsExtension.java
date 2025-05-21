package com.condation.cms.modules.ui.extensionpoints.remotemethods;

/*-
 * #%L
 * ui-module
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
import com.condation.cms.api.Constants;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.db.cms.ReadOnlyFile;
import com.condation.cms.api.eventbus.events.ReIndexContentMetaDataEvent;
import com.condation.cms.api.feature.features.DBFeature;
import com.condation.cms.api.feature.features.EventBusFeature;
import com.condation.cms.api.feature.features.RequestFeature;
import com.condation.cms.api.ui.extensions.UIRemoteMethodExtensionPoint;
import com.condation.cms.api.utils.PathUtil;
import com.condation.cms.api.utils.SectionUtil;
import com.condation.cms.content.Section;
import com.condation.cms.modules.ui.utils.ContentFileParser;
import com.condation.cms.modules.ui.utils.YamlHeaderUpdater;
import com.condation.modules.api.annotation.Extension;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import com.condation.cms.api.ui.annotations.RemoteMethod;

/**
 *
 * @author t.marx
 */
@Slf4j
@Extension(UIRemoteMethodExtensionPoint.class)
public class RemoteContentEndpointsExtension extends UIRemoteMethodExtensionPoint {

	@RemoteMethod(name = "content.get")
	public Object getContent(Map<String, Object> parameters) {
		final DB db = getContext().get(DBFeature.class).db();
		var contentBase = db.getReadOnlyFileSystem().resolve(Constants.Folders.CONTENT);

		var uri = (String) parameters.get("uri");

		var contentFile = contentBase.resolve(uri);

		Map<String, Object> result = new HashMap<>();
		result.put("uri", uri);
		if (contentFile != null) {
			try {
				ContentFileParser parser = new ContentFileParser(contentFile);
				result.put("content", parser.getContent());
				result.put("meta", parser.getHeader());
			} catch (IOException ex) {
				log.error("", ex);
			}
		}

		return result;
	}

	@RemoteMethod(name = "content.set")
	public Object setContent(Map<String, Object> parameters) {
		final DB db = getContext().get(DBFeature.class).db();
		var contentBase = db.getReadOnlyFileSystem().resolve(Constants.Folders.CONTENT);

		var updatedContent = (String) parameters.get("content");
		var uri = (String) parameters.get("uri");

		var contentFile = contentBase.resolve(uri);

		Map<String, Object> result = new HashMap<>();
		result.put("uri", uri);
		if (contentFile != null) {
			try {
				ContentFileParser parser = new ContentFileParser(contentFile);

				Map<String, Object> meta = parser.getHeader();

				var filePath = db.getFileSystem().resolve(Constants.Folders.CONTENT).resolve(uri);

				YamlHeaderUpdater.saveMarkdownFileWithHeader(filePath, meta, updatedContent);
				log.debug("file {} saved", uri);
			} catch (IOException ex) {
				log.error("", ex);
			}
		}

		return result;
	}

	@RemoteMethod(name = "meta.set")
	public Object setMeta(Map<String, Object> parameters) {
		final DB db = getContext().get(DBFeature.class).db();
		var contentBase = db.getReadOnlyFileSystem().resolve(Constants.Folders.CONTENT);

		var update = (Map<String, Object>) parameters.get("meta");
		var uri = (String) parameters.get("uri");

		var contentFile = contentBase.resolve(uri);

		Map<String, Object> result = new HashMap<>();
		result.put("uri", uri);
		if (contentFile != null) {
			try {
				ContentFileParser parser = new ContentFileParser(contentFile);

				Map<String, Object> meta = parser.getHeader();
				YamlHeaderUpdater.mergeFlatMapIntoNestedMap(meta, update);

				var filePath = db.getFileSystem().resolve(Constants.Folders.CONTENT).resolve(uri);

				YamlHeaderUpdater.saveMarkdownFileWithHeader(filePath, meta, parser.getContent());
				log.debug("file {} saved", uri);

				getContext().get(EventBusFeature.class).eventBus().publish(new ReIndexContentMetaDataEvent(uri));
			} catch (IOException ex) {
				log.error("", ex);
			}
		}

		return result;
	}

	@RemoteMethod(name = "meta.set.batch")
	public Object setMetaBatch(Map<String, Object> parameters) {
		final DB db = getContext().get(DBFeature.class).db();
		var contentBase = db.getReadOnlyFileSystem().resolve(Constants.Folders.CONTENT);

		Map<String, Object> result = new HashMap<>();
		result.put("endpoint", "meta.set.batch");

		List<Map<String, Object>> updates = (List<Map<String, Object>>) parameters.get("updates");

		updates.forEach(entry -> {
			var update = (Map<String, Object>) entry.get("meta");
			var uri = (String) entry.get("uri");

			var contentFile = contentBase.resolve(uri);

			if (contentFile != null) {
				try {
					ContentFileParser parser = new ContentFileParser(contentFile);

					Map<String, Object> meta = parser.getHeader();
					YamlHeaderUpdater.mergeFlatMapIntoNestedMap(meta, update);

					var filePath = db.getFileSystem().resolve(Constants.Folders.CONTENT).resolve(uri);

					YamlHeaderUpdater.saveMarkdownFileWithHeader(filePath, meta, parser.getContent());
					log.debug("file {} saved", uri);

					getContext().get(EventBusFeature.class).eventBus().publish(new ReIndexContentMetaDataEvent(uri));
				} catch (IOException ex) {
					log.error("", ex);
				}
			}
		});

		return result;
	}

	@RemoteMethod(name = "content.section.add")
	public Object addSection(Map<String, Object> parameters) {
		final DB db = getContext().get(DBFeature.class).db();
		var contentBase = db.getReadOnlyFileSystem().resolve(Constants.Folders.CONTENT);

		var content = (String) parameters.getOrDefault("content", "");
		var uri = (String) parameters.get("uri");
		var template = (String) parameters.get("template");

		var contentFile = contentBase.resolve(uri);

		Map<String, Object> result = new HashMap<>();
		result.put("uri", uri);
		if (contentFile != null) {
			try {
				Map<String, Object> meta = Map.of("template", template);

				var filePath = db.getFileSystem().resolve(Constants.Folders.CONTENT).resolve(uri);

				YamlHeaderUpdater.saveMarkdownFileWithHeader(filePath, meta, content);
				log.debug("file {} saved", uri);

				getContext().get(EventBusFeature.class).eventBus().publish(new ReIndexContentMetaDataEvent(uri));
			} catch (IOException ex) {
				result.put("error", true);
				log.error("", ex);
			}
		}

		return result;
	}
	
	@RemoteMethod(name = "content.node")
	public Object getContentNode (Map<String, Object> parameters) {
		final DB db = getContext().get(DBFeature.class).db();
		var contentBase = db.getReadOnlyFileSystem().resolve(Constants.Folders.CONTENT);
		
		var url = (String) parameters.get("url");

			var path = URI.create(url).getPath();

			var contextPath = requestContext.get(RequestFeature.class).context();
			if (!"/".equals(contextPath) && path.startsWith(contextPath)) {
				path = path.replaceFirst(contextPath, "");
			}

			if (path.startsWith("/")) {
				path = path.substring(1);
			}

			var contentPath = contentBase.resolve(path);
			ReadOnlyFile contentFile = null;
			if (contentPath.exists() && contentPath.isDirectory()) {
				// use index.md
				var tempFile = contentPath.resolve("index.md");
				if (tempFile.exists()) {
					contentFile = tempFile;
				}
			} else {
				var temp = contentBase.resolve(path + ".md");
				if (temp.exists()) {
					contentFile = temp;
				}
			}
			
			Map<String, Object> result = new HashMap<>();
			result.put("url", url);
			if (contentFile != null) {
				result.put("uri", PathUtil.toRelativeFile(contentFile, contentBase));
				
				var sections = db.getContent().listSections(contentFile);
				Map<String, List<Section>> sectionMap = new HashMap<>();
				sections.forEach(section -> {
					String uri = section.uri();
					String name = SectionUtil.getSectionName(section.name());
					var index = section.getMetaValue(Constants.MetaFields.LAYOUT_ORDER, Constants.DEFAULT_SECTION_LAYOUT_ORDER);
					
					sectionMap.computeIfAbsent(name, k -> new ArrayList<>())
						.add(new Section(section.name(), index, "", uri));
				});
				result.put("sections", sectionMap);
			}

			return result;
	}
}
