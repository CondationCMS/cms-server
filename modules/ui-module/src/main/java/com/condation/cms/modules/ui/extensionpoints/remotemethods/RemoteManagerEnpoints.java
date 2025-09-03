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
import com.condation.cms.api.auth.Permissions;
import com.condation.cms.api.configuration.configs.MediaConfiguration;
import com.condation.cms.api.feature.features.ConfigurationFeature;
import com.condation.cms.api.feature.features.HookSystemFeature;
import com.condation.cms.api.ui.extensions.UIRemoteMethodExtensionPoint;
import com.condation.modules.api.annotation.Extension;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import com.condation.cms.api.ui.annotations.RemoteMethod;
import com.condation.cms.api.ui.rpc.RPCException;
import com.condation.cms.content.RenderContext;
import com.condation.cms.modules.ui.utils.UIHooks;

/**
 *
 * @author thorstenmarx
 */
@Slf4j
@Extension(UIRemoteMethodExtensionPoint.class)
public class RemoteManagerEnpoints extends AbstractRemoteMethodeExtension {

	@RemoteMethod(name = "manager.content.tags", permissions = {Permissions.CONTENT_EDIT})
	public Object getShortCodeNames (Map<String, Object> parameters) throws RPCException {
		return getRequestContext().get(RenderContext.class).tags().getTagNames();
	}
	
	@RemoteMethod(name = "manager.media.form", permissions = {Permissions.CONTENT_EDIT})
	public Object getMediaForm(Map<String, Object> parameters) throws RPCException {
		try {
			var form = (String) parameters.getOrDefault("form", "");
			return uiHooks().mediaForms().getMetaForms().get(form);
		} catch (Exception e) {
			log.error("", e);
			throw new RPCException(0, e.getMessage());
		}
	}
	
	@RemoteMethod(name = "manager.media.formats", permissions = {Permissions.CONTENT_EDIT})
	public Object getMediaFormats (Map<String, Object> parameters) throws RPCException {
		var configuration = getContext().get(ConfigurationFeature.class).configuration();
		return configuration.get(MediaConfiguration.class).getFormats();
	}
	
	@RemoteMethod(name = "manager.contentTypes.sections", permissions = {Permissions.CONTENT_EDIT})
	public Object getSectionTemplates(Map<String, Object> parameters) throws RPCException {
		try {
			var section = (String) parameters.getOrDefault("section", "");
			return uiHooks().contentTypes().getSectionTemplates(section);
		} catch (Exception e) {
			log.error("", e);
			throw new RPCException(0, e.getMessage());
		}
	}

	@RemoteMethod(name = "manager.contentTypes.pages", permissions = {Permissions.CONTENT_EDIT})
	public Object getPageTemplates(Map<String, Object> parameters) throws RPCException {
		try {
			return uiHooks().contentTypes().getPageTemplates();
		} catch (Exception e) {
			log.error("", e);
			throw new RPCException(0, e.getMessage());
		}
	}
}
