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
import com.condation.cms.api.SiteProperties;
import com.condation.cms.api.auth.Permissions;
import com.condation.cms.api.configuration.configs.SiteConfiguration;
import com.condation.cms.api.eventbus.events.ReIndexContentMetaDataEvent;
import com.condation.cms.api.feature.features.ConfigurationFeature;
import com.condation.cms.api.feature.features.EventBusFeature;
import com.condation.cms.api.feature.features.InjectorFeature;
import com.condation.cms.api.site.SiteService;
import com.condation.cms.api.ui.annotations.RemoteMethod;
import com.condation.cms.api.ui.extensions.UIRemoteMethodExtensionPoint;
import com.condation.cms.api.ui.rpc.RPCException;
import com.condation.cms.api.utils.PathUtil;
import com.condation.cms.core.serivce.ServiceRegistry;
import com.condation.cms.core.serivce.impl.NodeAlternateService;
import com.condation.cms.core.serivce.impl.SiteLinkService;
import com.condation.cms.modules.ui.extensionpoints.remotemethods.dto.AlternateDto;
import com.condation.modules.api.annotation.Extension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Extension(UIRemoteMethodExtensionPoint.class)
public class RemoteAlternateEndpoints extends AbstractRemoteMethodeExtension {

	private static final String CONTENT_NODE_NOT_FOUND = "content node for uri %s not found";

	@RemoteMethod(name = "alternates.get", permissions = {Permissions.CONTENT_EDIT})
	public Object get(Map<String, Object> parameters) throws RPCException {
		var uri = (String) parameters.getOrDefault("uri", "");
		var contentNode = getContentRepository(parameters).get(uri)
				.orElseThrow(() -> new RPCException(CONTENT_NODE_NOT_FOUND.formatted(uri)));
		var properties = siteProperties();
		var alternates = alternateMap(contentNode.data());
		List<AlternateDto> result = new ArrayList<>();

		siteService().relatedSites(properties.id()).forEach(site -> {
			var alternateUri = String.valueOf(alternates.getOrDefault(site.id(), ""));
			var deepLink = alternateUri.isBlank() ? "" : ServiceRegistry.getInstance()
					.get(site.id(), SiteLinkService.class)
					.map(service -> service.managerDeepLink(alternateUri))
					.orElse("");
			result.add(new AlternateDto(
					site.id(), site.locale().toLanguageTag(), alternateUri, deepLink));
		});
		return Map.of("alternates", result);
	}

	@RemoteMethod(name = "alternates.add", permissions = {Permissions.CONTENT_EDIT})
	public Object add(Map<String, Object> parameters) throws RPCException {
		var uri = required(parameters, "uri");
		var targetSite = required(parameters, "targetSite");
		var alternateUri = PathUtil.toURL(required(parameters, "alternateUri"));
		var properties = siteProperties();
		validateTarget(properties.id(), targetSite);

		var targetService = ServiceRegistry.getInstance().get(targetSite, NodeAlternateService.class)
				.orElseThrow(() -> new RPCException("alternate service for site %s not found".formatted(targetSite)));
		var oldAlternateUri = currentAlternate(uri, targetSite);
		if (!oldAlternateUri.isBlank() && !oldAlternateUri.equals(alternateUri)) {
			targetService.removeAlternate(oldAlternateUri, properties.id());
		}

		updateLocal(uri, targetSite, alternateUri);
		if (!targetService.addAlternate(alternateUri, properties.id(), uri)) {
			throw new RPCException("could not update alternate site %s".formatted(targetSite));
		}
		return Map.of("uri", uri);
	}

	@RemoteMethod(name = "alternates.remove", permissions = {Permissions.CONTENT_EDIT})
	public Object remove(Map<String, Object> parameters) throws RPCException {
		var uri = required(parameters, "uri");
		var targetSite = required(parameters, "targetSite");
		var properties = siteProperties();
		validateTarget(properties.id(), targetSite);
		var targetService = ServiceRegistry.getInstance().get(targetSite, NodeAlternateService.class)
				.orElseThrow(() -> new RPCException("alternate service for site %s not found".formatted(targetSite)));

		var repository = getMutableContentRepository(parameters);
		var node = repository.get(uri)
				.orElseThrow(() -> new RPCException(CONTENT_NODE_NOT_FOUND.formatted(uri)));
		var alternates = alternateMap(node.data());
		var alternateUri = String.valueOf(alternates.getOrDefault(targetSite, ""));
		if (alternateUri.isBlank()) {
			return Map.of("uri", uri);
		}

		try {
			var document = repository.load(node).orElseThrow();
			Map<String, Object> meta = new HashMap<>(node.data());
			alternates.remove(targetSite);
			if (alternates.isEmpty()) {
				meta.remove(Constants.MetaFields.ALTERNATES);
			} else {
				meta.put(Constants.MetaFields.ALTERNATES, alternates);
			}
			repository.save(node.path(), meta, document.content());
			getContext().get(EventBusFeature.class).eventBus().publish(new ReIndexContentMetaDataEvent(uri));
		} catch (IOException ex) {
			throw new RPCException(0, ex.getMessage());
		}

		if (!targetService.removeAlternate(alternateUri, properties.id())) {
			throw new RPCException("could not update alternate site %s".formatted(targetSite));
		}
		return Map.of("uri", uri);
	}

	private String currentAlternate(String uri, String targetSite) throws RPCException {
		var node = getContentRepository(Map.of()).get(uri)
				.orElseThrow(() -> new RPCException(CONTENT_NODE_NOT_FOUND.formatted(uri)));
		return String.valueOf(alternateMap(node.data()).getOrDefault(targetSite, ""));
	}

	private void updateLocal(String uri, String targetSite, String alternateUri) throws RPCException {
		var repository = getMutableContentRepository(Map.of());
		var node = repository.get(uri)
				.orElseThrow(() -> new RPCException(CONTENT_NODE_NOT_FOUND.formatted(uri)));
		try {
			var document = repository.load(node).orElseThrow();
			Map<String, Object> meta = new HashMap<>(node.data());
			var alternates = alternateMap(meta);
			alternates.put(targetSite, alternateUri);
			meta.put(Constants.MetaFields.ALTERNATES, alternates);
			repository.save(node.path(), meta, document.content());
			getContext().get(EventBusFeature.class).eventBus().publish(new ReIndexContentMetaDataEvent(uri));
		} catch (IOException ex) {
			throw new RPCException(0, ex.getMessage());
		}
	}

	private void validateTarget(String currentSite, String targetSite) throws RPCException {
		boolean related = siteService().relatedSites(currentSite).anyMatch(site -> site.id().equals(targetSite));
		if (!related) {
			throw new RPCException("site %s is not related to %s".formatted(targetSite, currentSite));
		}
	}

	private SiteProperties siteProperties() {
		return getContext().get(ConfigurationFeature.class).configuration()
				.get(SiteConfiguration.class).siteProperties();
	}

	private SiteService siteService() {
		return getContext().get(InjectorFeature.class).injector().getInstance(SiteService.class);
	}

	private String required(Map<String, Object> parameters, String name) throws RPCException {
		var value = String.valueOf(parameters.getOrDefault(name, ""));
		if (value.isBlank()) {
			throw new RPCException("parameter %s is required".formatted(name));
		}
		return value;
	}

	private Map<String, Object> alternateMap(Map<String, Object> meta) {
		var value = meta.get(Constants.MetaFields.ALTERNATES);
		if (!(value instanceof Map<?, ?> map)) {
			return new HashMap<>();
		}
		Map<String, Object> result = new HashMap<>();
		map.forEach((key, entry) -> result.put(String.valueOf(key), entry));
		return result;
	}
}
