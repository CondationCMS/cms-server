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
import com.condation.cms.api.eventbus.events.InvalidateMediaCache;
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
import java.util.HashMap;

/**
 *
 * @author thorstenmarx
 */
@Slf4j
@Extension(UIRemoteMethodExtensionPoint.class)
public class RemoteMediaEnpoints extends UIRemoteMethodExtensionPoint {

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
	
	private String getMediaPath (String image) {
		var contextPath = getContext().get(SitePropertiesFeature.class).siteProperties().contextPath();
		var baseUrl = getContext().get(SitePropertiesFeature.class).siteProperties().getOrDefault("baseurl", "-----");
		URI uri = URI.create(image);
		var path = uri.getPath();
		return ImageUtil.getRawPath(path, requestContext);
	}
}
