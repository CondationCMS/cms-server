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
import com.condation.cms.api.configuration.configs.SiteConfiguration;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.eventbus.events.ReIndexContentMetaDataEvent;
import com.condation.cms.api.feature.features.ConfigurationFeature;
import com.condation.cms.api.feature.features.DBFeature;
import com.condation.cms.api.feature.features.EventBusFeature;
import com.condation.cms.api.ui.extensions.UIRemoteMethodExtensionPoint;
import com.condation.modules.api.annotation.Extension;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import com.condation.cms.api.ui.annotations.RemoteMethod;
import com.condation.cms.api.ui.rpc.RPCException;
import com.condation.cms.modules.ui.extensionpoints.remotemethods.dto.TranslationDto;
import com.condation.cms.modules.ui.utils.ContentFileParser;
import com.condation.cms.modules.ui.utils.MetaConverter;
import com.condation.cms.modules.ui.utils.TranslationHelper;
import com.condation.cms.modules.ui.utils.YamlHeaderUpdater;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author thorstenmarx
 */
@Slf4j
@Extension(UIRemoteMethodExtensionPoint.class)
public class RemoteTranslationEnpoints extends AbstractRemoteMethodeExtension {

	@RemoteMethod(name = "translations.get", permissions = {Permissions.CONTENT_EDIT})
	public Object get(Map<String, Object> parameters) throws RPCException {
		final DB db = getDB(parameters);

		Map<String, Object> result = new HashMap<>();
		List<TranslationDto> translations = new ArrayList<>();
		result.put("translations", translations);

		var uri = (String) parameters.getOrDefault("uri", "");

		var contentNodeOpt = db.getContent().byUri(uri);
		var contentNode = contentNodeOpt.orElseThrow(() -> new RPCException("content node for uri %s not found".formatted(uri)));
		
		var siteProperties = getContext().get(ConfigurationFeature.class).configuration().get(SiteConfiguration.class).siteProperties();

		var translationHelper = new TranslationHelper(siteProperties);
		translationHelper.getFilteredMapping().forEach(mapping -> {
			var translationUri = contentNode.getMetaValue("translation.%s".formatted(mapping.language()), "");
			translations.add(new TranslationDto(mapping.site(), mapping.language(), translationUri));
		});

		return result;
	}
	
	@RemoteMethod(name = "translation.remove", permissions = {Permissions.CONTENT_EDIT})
	public Object remove(Map<String, Object> parameters) {
		final DB db = getContext().get(DBFeature.class).db();
		var contentBase = db.getReadOnlyFileSystem().resolve(Constants.Folders.CONTENT);

		var uri = (String) parameters.get("uri");
		var language = (String) parameters.get("language");

		var contentFile = contentBase.resolve(uri);

		Map<String, Object> result = new HashMap<>();
		result.put("uri", uri);
		if (contentFile != null) {
			try {
				ContentFileParser parser = new ContentFileParser(contentFile);

				Map<String, Object> meta = parser.getHeader();
				if (meta.containsKey("translations")) {
					var translations = (Map<String, Object>)meta.get("translations");
					if (!translations.containsKey(language)) {
						return result;
					}
					translations.remove(language);
					var filePath = db.getFileSystem().resolve(Constants.Folders.CONTENT).resolve(uri);

					YamlHeaderUpdater.saveMarkdownFileWithHeader(filePath, meta, parser.getContent());
					log.debug("file {} saved", uri);

					getContext().get(EventBusFeature.class).eventBus().publish(new ReIndexContentMetaDataEvent(uri));
				}
			} catch (IOException ex) {
				log.error("", ex);
			}
		}

		return result;
	}
	
	@RemoteMethod(name = "translation.set", permissions = {Permissions.CONTENT_EDIT})
	public Object set(Map<String, Object> parameters) {
		final DB db = getContext().get(DBFeature.class).db();
		var contentBase = db.getReadOnlyFileSystem().resolve(Constants.Folders.CONTENT);

		var uri = (String) parameters.get("uri");
		var language = (String) parameters.get("language");
		var translation_url = (String) parameters.get("translation_url");

		var contentFile = contentBase.resolve(uri);

		Map<String, Object> result = new HashMap<>();
		result.put("uri", uri);
		if (contentFile != null) {
			try {
				ContentFileParser parser = new ContentFileParser(contentFile);

				Map<String, Object> meta = parser.getHeader();
				var translations = (Map<String, Object>)meta.getOrDefault("translations", new HashMap<>());
				translations.put(language, translation_url);
				meta.put("translations", translations);
				
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
}
