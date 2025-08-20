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
import com.condation.cms.api.feature.features.HookSystemFeature;
import com.condation.cms.api.feature.features.ModuleManagerFeature;
import com.condation.cms.api.ui.extensions.UILocalizationExtensionPoint;
import com.condation.cms.api.ui.extensions.UIRemoteMethodExtensionPoint;
import com.condation.cms.modules.ui.utils.TranslationMerger;
import com.condation.modules.api.annotation.Extension;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import com.condation.cms.api.ui.annotations.RemoteMethod;
import com.condation.cms.modules.ui.utils.UIHooks;

/**
 *
 * @author thorstenmarx
 */
@Slf4j
@Extension(UIRemoteMethodExtensionPoint.class)
public class LocalizationEnpoints extends UIRemoteMethodExtensionPoint {



	@RemoteMethod(name = "i18n.load", permissions = {Permissions.CONTENT_EDIT})
	public Object list(Map<String, Object> parameters) {
		
		var moduleManager = getContext().get(ModuleManagerFeature.class).moduleManager();
		
		Map<String, Map<String, String>> localizations = new HashMap<>();
		try {
			moduleManager.extensions(UILocalizationExtensionPoint.class).forEach(ext -> {
				TranslationMerger.mergeTranslationMaps(ext.getLocalizations(), localizations);
			});

			UIHooks uiHooks = new UIHooks(getRequestContext().get(HookSystemFeature.class).hookSystem());

			TranslationMerger.mergeTranslationMaps(uiHooks.translations(), localizations);
		} catch (Exception e) {
			log.error("error loading translation", e);
		}
		
		return localizations;
	}
}
