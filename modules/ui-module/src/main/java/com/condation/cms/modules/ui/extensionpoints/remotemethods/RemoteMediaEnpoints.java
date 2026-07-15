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
import com.condation.cms.api.configuration.configs.MediaConfiguration;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.eventbus.events.InvalidateMediaCache;
import com.condation.cms.api.extensions.AbstractExtensionPoint;
import com.condation.cms.api.feature.features.ConfigurationFeature;
import com.condation.cms.api.feature.features.DBFeature;
import com.condation.cms.api.feature.features.EventBusFeature;
import com.condation.cms.api.feature.features.SiteMediaServiceFeature;
import com.condation.cms.api.feature.features.SitePropertiesFeature;
import com.condation.cms.api.ui.extensions.UIRemoteMethodExtensionPoint;
import com.condation.modules.api.annotation.Extension;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import com.condation.cms.api.ui.annotations.RemoteMethod;
import com.condation.cms.api.ui.rpc.RPCException;
import com.condation.cms.api.utils.ImageUtil;
import com.condation.cms.modules.ui.utils.MetaConverter;
import com.condation.cms.core.content.io.YamlHeaderUpdater;
import java.net.URI;
import java.nio.file.Files;
import java.util.HashMap;

/**
 *
 * @author thorstenmarx
 */
@Slf4j
@Extension(UIRemoteMethodExtensionPoint.class)
public class RemoteMediaEnpoints extends AbstractRemoteMethodeExtension implements UIRemoteMethodExtensionPoint {

    @RemoteMethod(name = "media.formats.get", permissions = {Permissions.CONTENT_EDIT})
	public Object getResolutions(Map<String, Object> parameters) throws RPCException {
		try {
			var image = (String) parameters.getOrDefault("image", "");
			
			var imagePath = getMediaPath(image);
			
			var mediaFormats = getRequestContext().get(ConfigurationFeature.class).configuration().get(MediaConfiguration.class).getFormats();
            
            return Map.of("formats", mediaFormats.stream().map(format -> format.name()).toList());
		} catch (Exception e) {
			log.error("", e);
			throw new RPCException(0, e.getMessage());
		}
	}
    
	@RemoteMethod(name = "media.meta.get", permissions = {Permissions.CONTENT_EDIT})
	public Object getMediaMeta(Map<String, Object> parameters) throws RPCException {
		try {
			var image = (String) parameters.getOrDefault("image", "");
			
			var imagePath = getMediaPath(image);
			
			return getRequestContext().get(SiteMediaServiceFeature.class).mediaService().get(imagePath);
		} catch (Exception e) {
			log.error("", e);
			throw new RPCException(0, e.getMessage());
		}
	}
	
	@RemoteMethod(name = "media.meta.set", permissions = {Permissions.CONTENT_EDIT})
	public Object setMediaMeta(Map<String, Object> parameters) throws RPCException {
		try {
			var data = (Map<String, Map<String, Object>>) parameters.getOrDefault("meta", Map.of());
			var image = (String) parameters.getOrDefault("image", "");
			
			var imagePath = getMediaPath(image);
			var media = getRequestContext().get(SiteMediaServiceFeature.class).mediaService().get(imagePath);
			
			var metaData = new HashMap<String, Object>(media.meta());
			YamlHeaderUpdater.mergeFlatMapIntoNestedMap(metaData, MetaConverter.convertMeta(data));
			
			var fs = getContext().get(DBFeature.class).db().getFileSystem();
			var assets = fs.resolve(Constants.Folders.ASSETS);
			var metaFile = assets.resolve(imagePath + ".meta.yaml");
			
			YamlHeaderUpdater.saveMetaData(metaFile, metaData);
			
			getContext().get(EventBusFeature.class).eventBus().publish(new InvalidateMediaCache(assets.resolve(imagePath)));
			
			return Map.of(
					"status", "success"
			);
		} catch (Exception e) {
			log.error("", e);
			throw new RPCException(0, e.getMessage());
		}
	}
    
    @RemoteMethod(name = "media.rename", permissions = {Permissions.CONTENT_EDIT})
	public Object renameFile(Map<String, Object> parameters) throws RPCException {
		final DB db = getDB(parameters);
		Map<String, Object> result = new HashMap<>();

		try {
			var uri = (String) parameters.getOrDefault("uri", "");
			var name = (String) parameters.getOrDefault("name", "");
			var newName = (String) parameters.get("newName");

			if (newName == null || newName.isBlank()) {
				throw new IllegalArgumentException("newName must not be null or blank");
			}

			var mediaBase = getBase(db.getFileSystem(), "assets");
			
			// check if both paths are in host directory
			mediaBase.resolve(uri).resolve(name);
			mediaBase.resolve(uri).resolve(newName);
			
			var writableBase = getWritableBase(db.getFileSystem(), "assets");

			var sourcePath = writableBase.resolve(uri).resolve(name);
			var targetPath = writableBase.resolve(uri).resolve(newName);
            
            var metaSource = writableBase.resolve(uri).resolve(name + ".meta.yaml");
            var metaTarget = writableBase.resolve(uri).resolve(newName + ".meta.yaml");
            
			log.debug("renaming from {} to {}", sourcePath, targetPath);

			if (!Files.exists(sourcePath)) {
				throw new RPCException("Source file not found: " + sourcePath);
			}
			if (Files.exists(targetPath)) {
				throw new RPCException("Target file already exists: " + targetPath);
			}

			Files.move(sourcePath, targetPath);

            if (Files.exists(metaSource)) {
                Files.move(metaSource, metaTarget);
            }
            
			result.put("success", true);
			result.put("newName", newName);

		} catch (Exception e) {
			log.error("Error during rename", e);
			throw new RPCException(0, e.getMessage());
		}

		return result;
	}
	
	private String getMediaPath (String image) {
		var contextPath = getContext().get(SitePropertiesFeature.class).siteProperties().contextPath();
		var baseUrl = getContext().get(SitePropertiesFeature.class).siteProperties().getOrDefault("baseurl", "-----");
		URI uri = URI.create(image);
		var path = uri.getPath();
		return ImageUtil.getRawPath(path, getRequestContext());
	}
}
