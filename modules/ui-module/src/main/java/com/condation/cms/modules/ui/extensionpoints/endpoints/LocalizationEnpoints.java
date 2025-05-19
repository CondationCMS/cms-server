package com.condation.cms.modules.ui.extensionpoints.endpoints;

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

import com.condation.cms.api.feature.features.ModuleManagerFeature;
import com.condation.cms.api.ui.annotations.RemoteEndpoint;
import com.condation.cms.api.ui.extensions.UILocalizationExtensionPoint;
import com.condation.cms.api.ui.extensions.UIRemoteEndpointExtensionPoint;
import com.condation.cms.modules.ui.utils.TranslationMerger;
import com.condation.modules.api.annotation.Extension;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author thorstenmarx
 */
@Slf4j
@Extension(UIRemoteEndpointExtensionPoint.class)
public class LocalizationEnpoints extends UIRemoteEndpointExtensionPoint {



	@RemoteEndpoint(endpoint = "i18n.load")
	public Object list(Map<String, Object> parameters) {
		var moduleManager = getContext().get(ModuleManagerFeature.class).moduleManager();
		
		Map<String, Map<String, String>> localizations = new HashMap<>();
		moduleManager.extensions(UILocalizationExtensionPoint.class).forEach(ext -> {
			TranslationMerger.mergeTranslationMaps(ext.getLocalizations(), localizations);
		});
		

		return localizations;
	}
}
