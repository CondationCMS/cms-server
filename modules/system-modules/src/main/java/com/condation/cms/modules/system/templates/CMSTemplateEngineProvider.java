package com.condation.cms.modules.system.templates;

/*-
 * #%L
 * cms-system-modules
 * %%
 * Copyright (C) 2023 - 2024 CondationCMS
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

import com.condation.cms.api.extensions.TemplateEngineProviderExtensionPoint;
import com.condation.cms.api.feature.features.CacheManagerFeature;
import com.condation.cms.api.feature.features.DBFeature;
import com.condation.cms.api.feature.features.ServerPropertiesFeature;
import com.condation.cms.api.feature.features.ThemeFeature;
import com.condation.cms.api.template.TemplateEngine;
import com.condation.cms.templates.module.CMSModuleTemplateEngine;
import com.condation.modules.api.annotation.Extension;

/**
 *
 * @author t.marx
 */
@Extension(TemplateEngineProviderExtensionPoint.class)
public class CMSTemplateEngineProvider extends TemplateEngineProviderExtensionPoint {

	private CMSModuleTemplateEngine templateEngine;
	
	@Override
	public String getName() {
		return "system";
	}

	@Override
	public TemplateEngine getTemplateEngine() {
		if (templateEngine == null) {
			createTemplateEngine();
		}
		return templateEngine;
	}

	private void createTemplateEngine() {
		var db = getContext().get(DBFeature.class).db();
		var serverProps = getContext().get(ServerPropertiesFeature.class).serverProperties();
		var theme = getContext().get(ThemeFeature.class).theme();
		var cacheManager = getContext().get(CacheManagerFeature.class).cacheManager();
		
		templateEngine = CMSModuleTemplateEngine.create(db, theme, cacheManager, serverProps.dev());
	}
	
}
