package com.condation.cms.modules.ui.extensionpoints.remotemethods;

/*-
 * #%L
 * UI Module
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

import com.condation.cms.api.auth.Permissions;
import com.condation.cms.api.Constants;
import com.condation.cms.api.db.ContentNode;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.eventbus.events.ReIndexContentMetaDataEvent;
import com.condation.cms.api.feature.features.EventBusFeature;
import com.condation.cms.api.feature.features.InjectorFeature;
import com.condation.cms.api.feature.features.WorkflowFeature;
import com.condation.cms.api.ui.annotations.RemoteMethod;
import com.condation.cms.api.ui.extensions.UIRemoteMethodExtensionPoint;
import com.condation.cms.api.ui.rpc.RPCException;
import com.condation.cms.api.utils.PathUtil;
import com.condation.cms.content.VariantResolver;
import com.condation.cms.core.content.io.ContentFileParser;
import com.condation.cms.core.content.io.YamlHeaderUpdater;
import com.condation.cms.modules.ui.extensionpoints.remotemethods.dto.VariantDto;
import com.condation.cms.modules.ui.utils.UIPathUtil;
import com.condation.modules.api.annotation.Extension;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Remote methods for loading the variants of a content node.
 *
 * @author thorstenmarx
 */
@Extension(UIRemoteMethodExtensionPoint.class)
@Slf4j
public class RemoteVariantEndpoint extends AbstractRemoteMethodeExtension {

	@RemoteMethod(name = "variants.create", permissions = {Permissions.CONTENT_EDIT})
	public Object create(Map<String, Object> parameters) throws RPCException {
		var uri = stringParameter(parameters, "uri");
		var id = stringParameter(parameters, "id");
		var title = stringParameter(parameters, "title");
		var template = stringParameter(parameters, "template");
		var copyContent = Boolean.TRUE.equals(parameters.get("copyContent"));

		if (uri.isBlank() || id.isBlank() || title.isBlank() || template.isBlank()) {
			throw new RPCException(400, "uri, id, title and template must not be blank");
		}
		if (uiHooks().contentTypes().getPageTemplates().stream()
				.noneMatch(pageTemplate -> pageTemplate.template().equals(template))) {
			throw new RPCException(400, "unknown page template");
		}

		var db = getDB(parameters);
		var canonicalNode = findContentNode(db, uri);
		var contentBase = db.getFileSystem().resolve(Constants.Folders.CONTENT);
		var canonicalFile = contentBase.resolve(canonicalNode.path());
		var variantId = UIPathUtil.toValidFilename(id);
		if (variantId.isBlank() || ".".equals(variantId) || "..".equals(variantId)) {
			throw new RPCException(400, "invalid variant id");
		}

		var fileName = canonicalFile.getFileName().toString();
		var pageName = fileName.endsWith(".md")
				? fileName.substring(0, fileName.length() - 3)
				: fileName;
		var variantFile = canonicalFile.getParent()
				.resolve(".variants")
				.resolve(pageName)
				.resolve(variantId)
				.resolve(fileName);

		try {
			if (!UIPathUtil.isChild(contentBase, variantFile)) {
				throw new RPCException(400, "invalid variant path");
			}
			if (Files.exists(variantFile)) {
				throw new RPCException(409, "variant already exists");
			}

			var body = copyContent ? new ContentFileParser(canonicalFile.toString()).getContent() : "";
			var sectionCopies = copyContent
					? db.getContent().listSectionEntries(db.getFileSystem().contentBase().resolve(canonicalNode.path()))
							.stream()
							.map(section -> new SectionCopy(
									contentBase.resolve(section.path()),
									variantFile.getParent().resolve(section.name())
							))
							.toList()
					: java.util.List.<SectionCopy>of();
			if (sectionCopies.stream().anyMatch(copy -> Files.exists(copy.target()))) {
				throw new RPCException(409, "variant section already exists");
			}

			Map<String, Object> meta = new HashMap<>();
			meta.put(Constants.MetaFields.TITLE, title);
			meta.put(Constants.MetaFields.TEMPLATE, template);
			meta.put(Constants.MetaFields.STATUS, getContext().get(WorkflowFeature.class)
					.workflow().getStatusProvider().newNodeStatus());
			meta.put("createdAt", Date.from(Instant.now()));
			meta.put("createdBy", getUserName());

			Files.createDirectories(variantFile.getParent());
			var createdFiles = new ArrayList<java.nio.file.Path>();
			try {
				YamlHeaderUpdater.saveMarkdownFileWithHeader(variantFile, meta, body);
				createdFiles.add(variantFile);
				for (var sectionCopy : sectionCopies) {
					Files.copy(
							sectionCopy.source(),
							sectionCopy.target(),
							StandardCopyOption.COPY_ATTRIBUTES
					);
					createdFiles.add(sectionCopy.target());
				}
			} catch (Exception exception) {
				for (var createdFile : createdFiles.reversed()) {
					Files.deleteIfExists(createdFile);
				}
				throw exception;
			}

			var eventBus = getContext().get(EventBusFeature.class).eventBus();
			for (var createdFile : createdFiles) {
				eventBus.syncPublish(new ReIndexContentMetaDataEvent(
						PathUtil.toRelativeFile(createdFile, contentBase)
				));
			}
			db.getFileSystem().flushContentChanges();
			var newUri = PathUtil.toRelativeFile(variantFile, contentBase);

			return Map.of(
					"id", variantId,
					"uri", newUri,
					"url", managerPreviewUrl(PathUtil.toURL(variantFile, contentBase))
			);
		} catch (RPCException exception) {
			throw exception;
		} catch (Exception exception) {
			log.error("Could not create variant '{}' for '{}'", variantId, canonicalNode.path(), exception);
			throw new RPCException(500, exception.getMessage());
		}
	}

	@RemoteMethod(name = "variants.get", permissions = {Permissions.CONTENT_EDIT})
	public Object get(Map<String, Object> parameters) throws RPCException {
		var uri = (String) parameters.getOrDefault("uri", "");
		if (uri.isBlank()) {
			throw new RPCException(400, "uri must not be blank");
		}

		var db = getDB(parameters);
		var contentNode = findContentNode(db, uri);
		var variants = getVariantResolver(db)
				.getVariants(contentNode)
				.stream()
				.sorted(Comparator.comparing(VariantResolver.Variant::id))
				.map(variant -> new VariantDto(
						variant.id(),
						variant.node().uri(),
						managerPreviewUrl(variant.node().url()),
						variant.node().data()
				))
				.toList();

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("uri", contentNode.uri());
		result.put("variants", variants);
		return result;
	}

	private ContentNode findContentNode(DB db, String uri) throws RPCException {
		return db.getContent()
				.byPath(uri)
				.or(() -> db.getContent().byUrl(uri))
				.orElseThrow(() -> new RPCException(
						404,
						"content node for uri %s not found".formatted(uri)
				));
	}

	protected VariantResolver getVariantResolver(DB db) {
		return getContext().get(InjectorFeature.class).injector().getInstance(VariantResolver.class);
	}

	private String stringParameter(Map<String, Object> parameters, String name) {
		var value = parameters.get(name);
		return value instanceof String stringValue ? stringValue.trim() : "";
	}

	private String managerPreviewUrl(String url) {
		return url + (url.contains("?") ? "&" : "?") + "preview=manager";
	}

	private record SectionCopy(java.nio.file.Path source, java.nio.file.Path target) {
	}
}
