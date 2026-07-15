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
import com.condation.cms.api.db.DB;
import com.condation.cms.api.db.DBFileSystem;
import com.condation.cms.api.db.cms.ReadOnlyFile;
import com.condation.cms.api.extensions.AbstractExtensionPoint;
import com.condation.cms.api.feature.features.AuthFeature;
import com.condation.cms.api.feature.features.DBFeature;
import com.condation.cms.api.feature.features.HookSystemFeature;
import com.condation.cms.api.ui.extensions.UIRemoteMethodExtensionPoint;
import com.condation.cms.core.serivce.ServiceRegistry;
import com.condation.cms.core.serivce.impl.SiteDBService;
import com.condation.cms.filesystem.FileSystem;
import com.condation.cms.modules.ui.utils.UIHooks;
import java.nio.file.Path;
import java.util.Map;

/**
 *
 * @author thorstenmarx
 */
public abstract class AbstractRemoteMethodeExtension extends AbstractExtensionPoint implements UIRemoteMethodExtensionPoint {
	protected String getUserName() {
		if (getRequestContext().has(AuthFeature.class)) {
			return getRequestContext().get(AuthFeature.class).username();
		}
		return "";
	}
	
	protected UIHooks uiHooks() {
		return new UIHooks(getRequestContext().get(HookSystemFeature.class).hookSystem());
	}
	
	protected DB getDB (Map<String, Object> parameters) {
		if (parameters.containsKey("siteId")) {
			return ServiceRegistry.getInstance().get(
					(String)parameters.get("siteId"), 
					SiteDBService.class).get().db();
		} else {
			return getContext().get(DBFeature.class).db();
		}
	}
    
    public static ReadOnlyFile getBase(DBFileSystem fileSystem, String type) {
		return switch (type) {
			case "content" ->
				fileSystem.contentBase();
			case "assets" ->
				fileSystem.assetBase();
			default ->
				null;
		};
	}

	public static Path getWritableBase(DBFileSystem fileSystem, String type) {
		return switch (type) {
			case "content" ->
				fileSystem.resolve(Constants.Folders.CONTENT);
			case "assets" ->
				fileSystem.resolve(Constants.Folders.ASSETS);
			default ->
				null;
		};
	}
}
