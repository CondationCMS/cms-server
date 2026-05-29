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
import com.condation.cms.api.Constants;
import com.condation.cms.api.auth.Permissions;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.db.Page;
import com.condation.cms.api.eventbus.events.ContentChangedEvent;
import com.condation.cms.api.eventbus.events.ReIndexContentMetaDataEvent;
import com.condation.cms.api.feature.features.EventBusFeature;
import com.condation.cms.api.ui.extensions.UIRemoteMethodExtensionPoint;
import com.condation.cms.api.utils.FileUtils;
import com.condation.modules.api.annotation.Extension;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import com.condation.cms.api.ui.annotations.RemoteMethod;
import com.condation.cms.api.ui.rpc.RPCException;
import com.condation.cms.api.utils.HTTPUtil;
import com.condation.cms.api.utils.PathUtil;
import com.condation.cms.modules.ui.utils.UIPathUtil;
import com.condation.cms.core.content.io.YamlHeaderUpdater;
import com.condation.cms.modules.ui.model.NodeDTO;
import com.condation.cms.modules.ui.utils.NumberUtils;
import com.google.common.base.Strings;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 *
 * @author thorstenmarx
 */
@Slf4j
@Extension(UIRemoteMethodExtensionPoint.class)
public class RemotePageEnpoints extends AbstractRemoteMethodeExtension {

    @RemoteMethod(name = "pages.filter", permissions = {Permissions.CONTENT_EDIT})
    public Object filterPages (Map<String, Object> parameters) throws RPCException {
        
        final DB db = getDB(parameters);
        
        var query = db.getContent().query((node, length) -> node);
        
        if (parameters.containsKey("contentType") && parameters.get("contentType") != null) {
            query.contentType(parameters.get("contentType").toString());
        }
        
        if (parameters.containsKey("query") && parameters.get("query") != null) {
            query.expression(parameters.get("query").toString());
        }
        
        if (parameters.containsKey("excerpt") && parameters.get("excerpt") != null) {
            try {
                long excerpt = Long.parseLong(parameters.get("excerpt").toString());
                query.excerpt(excerpt);
            } catch (NumberFormatException e) {
                log.error("Error parsing excerpt", e);
            }
        }
        
        if (parameters.get("where") instanceof List whereClauses) {
            for (Object clauseObj : whereClauses) {
                if (clauseObj instanceof Map clause) {
                    String field = (String) clause.get("field");
                    Object value = clause.get("value");
                    String operator = (String) clause.getOrDefault("operator", "=");
                    query.where(field, operator, value);
                }
            }
        }
        
        if (parameters.containsKey("orderby") && parameters.get("orderby") != null) {
            String field = parameters.get("orderby").toString();
            String direction = (String) parameters.getOrDefault("order", "asc");
            if ("desc".equalsIgnoreCase(direction)) {
                query.orderby(field).desc();
            } else {
                query.orderby(field).asc();
            }
        }
        
		long page = NumberUtils.toLong(parameters.getOrDefault("page", 1l));
		long size = NumberUtils.toLong(parameters.getOrDefault("size", 5l));
		
        var pageList = query.page(page, size);
		
		var contentBase = db.getReadOnlyFileSystem().contentBase();
		return new Page<>(pageList.getTotalItems(), pageList.getPageSize(), pageList.getTotalPages(), pageList.getPage(), 
				pageList.getItems().stream().map(node -> {
					var temp_path = contentBase.resolve(node.uri());
					var url = PathUtil.toURL(temp_path, contentBase);
					
					url = HTTPUtil.modifyUrl(url, context);
					
					return new NodeDTO(url, node.data());
				}).toList()
		);
    }
    
	@RemoteMethod(name = "page.delete", permissions = {Permissions.CONTENT_EDIT})
	public Object deletePage(Map<String, Object> parameters) throws RPCException {
		final DB db = getDB(parameters);

		Map<String, Object> result = new HashMap<>();

		try {
			var uri = (String) parameters.getOrDefault("uri", "");
			var name = (String) parameters.getOrDefault("name", "");
			var contentBase = db.getReadOnlyFileSystem().contentBase();

			if (Strings.isNullOrEmpty(name)) {
				throw new RPCException(0, "filename can not be null");
			}
			
			var contentFile = contentBase.resolve(uri).resolve(name);

			log.debug("deleting file {}", contentFile.uri());
			var sections = db.getContent().listSectionEntries(contentFile);
			Files.deleteIfExists(db.getFileSystem().resolve(Constants.Folders.CONTENT).resolve(uri).resolve(name));
			sections.forEach(node -> {
				try {
					log.debug("deleting section {}", node.uri());
					FileUtils.deleteFolder(db.getFileSystem().resolve(node.uri()));
				} catch (IOException ioe) {
					log.error("error deleting file {}", node.uri(), ioe);
				}
			});
		} catch (Exception e) {
			log.error("", e);
			throw new RPCException(0, e.getMessage());
		}

		return result;
	}

	@RemoteMethod(name = "page.create", permissions = {Permissions.CONTENT_EDIT})
	public Object createPage(Map<String, Object> parameters) throws RPCException {
		final DB db = getDB(parameters);

		Map<String, Object> result = new HashMap<>();

		try {
			var uri = (String) parameters.getOrDefault("uri", "");
			var name = (String) parameters.getOrDefault("name", "");
			
			if (Strings.isNullOrEmpty(name)) {
				throw new RPCException(1, "name must not be empty");
			}
			
			var contentBase = db.getFileSystem().resolve(Constants.Folders.CONTENT);
			
			var contentType = (String) parameters.getOrDefault("contentType", "");
			
			var pageTemplate = uiHooks().contentTypes().getPageTemplate(contentType);
			
			if (pageTemplate.isEmpty()) {
				throw new RPCException(1, "no contentType selected");
			}
			
			Map<String, Object> meta = new HashMap<>();
			meta.put("createdAt", Date.from(Instant.now()));
			meta.put("createdBy", getUserName());
			meta.put(Constants.MetaFields.TITLE, name);
			meta.put(Constants.MetaFields.TEMPLATE, pageTemplate.get().template());
			meta.put(Constants.MetaFields.PUBLISHED, false);

			name = UIPathUtil.toValidFilename(name);
			
			Path newFile = null;
			if (name.endsWith(".md")) {
				newFile = contentBase.resolve(uri).resolve(name);
			} else {
				newFile = contentBase.resolve(uri).resolve(name).resolve("index.md");
			}
			
			
			if (newFile.isAbsolute()) {
				throw new RPCException(1, "absolut path is not supported");
			} else if (Files.exists(newFile)) {
				throw new RPCException(1, "directory already exists");
			} else if (!UIPathUtil.isChild(contentBase, newFile)) {
				throw new RPCException(1, "invalid path");
			}
			Files.createDirectories(newFile.getParent());
			Files.createFile(newFile);
			var newURI = PathUtil.toRelativeFile(newFile, contentBase);
			getContext().get(EventBusFeature.class).eventBus()
					.syncPublish(new ReIndexContentMetaDataEvent(newURI));

			YamlHeaderUpdater.saveMarkdownFileWithHeader(newFile, meta, "");
			
			String url = PathUtil.toURL(newFile, contentBase);
			result.put("uri", url);
		} catch (Exception e) {
			log.error("", e);
			throw new RPCException(0, e.getMessage());
		}

		return result;
	}
}
