package com.condation.cms.modules.ui.extensionpoints;

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
import com.condation.cms.api.feature.features.CacheManagerFeature;
import com.condation.cms.api.feature.features.DBFeature;
import com.condation.cms.api.module.CMSModuleContext;
import com.condation.cms.modules.ui.services.LockService;
import com.condation.cms.modules.ui.utils.TemplateEngine;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import lombok.Getter;
import org.simplejavamail.config.ConfigLoader;

/**
 *
 * @author t.marx
 */
public class UILifecycleExtension {

	@Getter
	private LockService lockService;
	@Getter
	private TemplateEngine templateEngine;

	private static UILifecycleExtension INSTANCE = null;

	public static UILifecycleExtension getInstance(CMSModuleContext context) {
		if (INSTANCE == null) {
			INSTANCE = new UILifecycleExtension(context);
		}
		return INSTANCE;
	}

	private UILifecycleExtension(CMSModuleContext context) {
		lockService = new LockService();
		templateEngine = new TemplateEngine(context.get(CacheManagerFeature.class).cacheManager());
	}
}
