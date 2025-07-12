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
import com.condation.cms.api.db.DB;
import com.condation.cms.api.feature.features.DBFeature;
import com.condation.cms.api.feature.features.HookSystemFeature;
import com.condation.cms.api.ui.extensions.UIRemoteMethodExtensionPoint;
import com.condation.modules.api.annotation.Extension;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import com.condation.cms.api.ui.annotations.RemoteMethod;
import com.condation.cms.api.ui.rpc.RPCException;
import com.condation.cms.modules.ui.utils.UIHooks;

/**
 *
 * @author thorstenmarx
 */
@Slf4j
@Extension(UIRemoteMethodExtensionPoint.class)
public class RemoteManagerEnpoints extends UIRemoteMethodExtensionPoint {

	@RemoteMethod(name = "manager.contentTypes.sections")
	public Object getSectionTemplates(Map<String, Object> parameters) throws RPCException {
		final DB db = getContext().get(DBFeature.class).db();

		Map<String, Object> result = new HashMap<>();

		try {
			var section = (String) parameters.getOrDefault("section", "");

			return uiHooks().contentTypes().getSectionTemplates(section);

		} catch (Exception e) {
			log.error("", e);
			throw new RPCException(0, e.getMessage());
		}
	}

	@RemoteMethod(name = "manager.contentTypes.pages")
	public Object getPageTemplates(Map<String, Object> parameters) throws RPCException {
		try {
			return uiHooks().contentTypes().getPageTemplates();

		} catch (Exception e) {
			log.error("", e);
			throw new RPCException(0, e.getMessage());
		}
	}

	private UIHooks uiHooks() {
		return new UIHooks(getRequestContext().get(HookSystemFeature.class).hookSystem());
	}
}
