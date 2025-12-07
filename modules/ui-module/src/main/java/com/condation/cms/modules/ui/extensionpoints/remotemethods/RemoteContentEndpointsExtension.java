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
import com.condation.cms.api.auth.Permissions;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.db.cms.ReadOnlyFile;
import com.condation.cms.api.eventbus.events.InvalidateContentCacheEvent;
import com.condation.cms.api.eventbus.events.ReIndexContentMetaDataEvent;
import com.condation.cms.api.extensions.AbstractExtensionPoint;
import com.condation.cms.api.feature.features.DBFeature;
import com.condation.cms.api.feature.features.EventBusFeature;
import com.condation.cms.api.feature.features.RequestFeature;
import com.condation.cms.api.ui.extensions.UIRemoteMethodExtensionPoint;
import com.condation.cms.api.utils.PathUtil;
import com.condation.cms.api.utils.SectionUtil;
import com.condation.cms.content.Section;
import com.condation.cms.core.content.io.ContentFileParser;
import com.condation.cms.core.content.io.YamlHeaderUpdater;
import com.condation.modules.api.annotation.Extension;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import com.condation.cms.api.ui.annotations.RemoteMethod;
import com.condation.cms.modules.ui.utils.FormHelper;
import com.condation.cms.modules.ui.utils.MetaConverter;
import com.condation.cms.modules.ui.utils.UIFileNameUtil;
import com.condation.cms.modules.ui.utils.UIPathUtil;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 * @author t.marx
 */
@Slf4j
@Extension(UIRemoteMethodExtensionPoint.class)
public class RemoteContentEndpointsExtension extends AbstractExtensionPoint implements UIRemoteMethodExtensionPoint {

	@RemoteMethod(name = "content.get", permissions = {Permissions.CONTENT_EDIT})
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

	@RemoteMethod(name = "content.set", permissions = {Permissions.CONTENT_EDIT})
	public Object setContent(Map<String, Object> parameters) {
		final DB db = getContext().get(DBFeature.class).db();
		var contentBase = db.getReadOnlyFileSystem().resolve(Constants.Folders.CONTENT);

		var updatedContent = FormHelper.getContent(parameters.get("content"));
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

	@RemoteMethod(name = "meta.set", permissions = {Permissions.CONTENT_EDIT})
	public Object setMeta(Map<String, Object> parameters) {
		final DB db = getContext().get(DBFeature.class).db();
		var contentBase = db.getReadOnlyFileSystem().resolve(Constants.Folders.CONTENT);

		var updateParam = (Map<String, Map<String, Object>>) parameters.get("meta");
		var update = MetaConverter.convertMeta(updateParam);
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

	private record Update (String uri, Map<String, Map<String, Object>> meta) {}
	
	@RemoteMethod(name = "meta.set.batch", permissions = {Permissions.CONTENT_EDIT})
	public Object setMetaBatch(Map<String, Object> parameters) {
		final DB db = getContext().get(DBFeature.class).db();
		var contentBase = db.getReadOnlyFileSystem().resolve(Constants.Folders.CONTENT);

		Map<String, Object> result = new HashMap<>();
		result.put("endpoint", "meta.set.batch");

		List<Map<String, Object>> updatesParam = (List<Map<String, Object>>) parameters.get("updates");
		
		var updates = updatesParam.stream().map(update -> {
			return new Update(
					(String)update.get("uri"), 
					(Map<String, Map<String, Object>>)update.get("meta"));
		}).toList();
		
		updates.forEach(update -> {
			var contentFile = contentBase.resolve(update.uri);

			if (contentFile != null) {
				try {
					ContentFileParser parser = new ContentFileParser(contentFile);

					Map<String, Object> fileMeta = parser.getHeader();
					var metaUpdated = MetaConverter.convertMeta(update.meta);
					YamlHeaderUpdater.mergeFlatMapIntoNestedMap(fileMeta, metaUpdated);

					var filePath = db.getFileSystem().resolve(Constants.Folders.CONTENT).resolve(update.uri);

					YamlHeaderUpdater.saveMarkdownFileWithHeader(filePath, fileMeta, parser.getContent());
					log.debug("file {} saved", update.uri);

					getContext().get(EventBusFeature.class).eventBus().publish(new ReIndexContentMetaDataEvent(update.uri));
				} catch (IOException ex) {
					log.error("", ex);
				}
			}
		});

		return result;
	}

	@RemoteMethod(name = "content.section.delete", permissions = {Permissions.CONTENT_EDIT})
	public Object deleteSection(Map<String, Object> parameters) {
		final DB db = getContext().get(DBFeature.class).db();
		var uri = (String) parameters.get("uri");
		final Path contentBase = db.getFileSystem().resolve(Constants.Folders.CONTENT);

		var contentFile = contentBase.resolve(uri);

		Map<String, Object> result = new HashMap<>();
		result.put("uri", uri);
		if (contentFile != null 
				&& PathUtil.isChild(contentBase, contentFile)
				&& Files.exists(contentFile)
				&& !Files.isDirectory(contentFile)
				) {
			try {
				Files.delete(contentFile);
				getContext().get(EventBusFeature.class).eventBus().publish(new InvalidateContentCacheEvent());
			} catch (Exception ex) {
				result.put("error", true);
				log.error("", ex);
			}
		}

		return result;
	}
	
	@RemoteMethod(name = "content.section.add", permissions = {Permissions.CONTENT_EDIT})
	public Object addSection(Map<String, Object> parameters) {
		final DB db = getContext().get(DBFeature.class).db();
		var contentBase = db.getReadOnlyFileSystem().resolve(Constants.Folders.CONTENT);

		var content = (String) parameters.getOrDefault("content", "");
		var parentUri = (String) parameters.get("parentUri");
		var parentSectionName = (String) parameters.get("parentSectionName");
		var sectionItemName = (String) parameters.get("sectionItemName");
		var template = (String) parameters.get("template");

		var title = sectionItemName;
		sectionItemName = UIPathUtil.slugify(sectionItemName);
		
		var uri = UIFileNameUtil.createSectionFileName(parentUri, parentSectionName, sectionItemName);
		
		var contentFile = contentBase.resolve(uri);
		
		Map<String, Object> result = new HashMap<>();
		result.put("uri", uri);
		if (contentFile != null) {
			try {
				Map<String, Object> meta = Map.of(
						"template", template,
						"title", title,
						"layout", Map.of(
								"order", 1000)
				);

				var filePath = db.getFileSystem().resolve(Constants.Folders.CONTENT).resolve(uri);

				YamlHeaderUpdater.saveMarkdownFileWithHeader(filePath, meta, content);
				log.debug("file {} saved", uri);

				getContext().get(EventBusFeature.class).eventBus().publish(new ReIndexContentMetaDataEvent(uri));
			} catch (IOException ex) {
				result.put("error", true);
				log.error("", ex);
			}
		} else {
			result.put("error", true);
		}

		return result;
	}
	
	@RemoteMethod(name = "content.node", permissions = {Permissions.CONTENT_EDIT})
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
						.add(new Section(section.name(), index, "", section.data(), uri));
				});
				result.put("sections", sectionMap);
			}

			return result;
	}
}
