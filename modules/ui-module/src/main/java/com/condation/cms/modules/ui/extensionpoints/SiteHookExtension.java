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
import com.condation.cms.api.annotations.Action;
import com.condation.cms.api.eventbus.events.InvalidateContentCacheEvent;
import com.condation.cms.api.eventbus.events.InvalidateMediaCache;
import com.condation.cms.api.eventbus.events.InvalidateTemplateCacheEvent;
import com.condation.cms.api.extensions.HookSystemRegisterExtensionPoint;
import com.condation.cms.api.feature.features.EventBusFeature;
import com.condation.cms.api.hooks.ActionContext;
import com.condation.modules.api.annotation.Extension;

/**
 *
 * @author t.marx
 */
@Extension(HookSystemRegisterExtensionPoint.class)
public class SiteHookExtension extends HookSystemRegisterExtensionPoint {

	@Action(value = "ui/manager/tools/media/cache/clear")
	public void clear_media_cache(ActionContext<?> context) {
		getContext().get(EventBusFeature.class).eventBus().publish(new InvalidateMediaCache(null));
	}

	@Action(value = "ui/manager/tools/template/cache/clear")
	public void clear_template_cache(ActionContext<?> context) {
		getContext().get(EventBusFeature.class).eventBus().publish(new InvalidateTemplateCacheEvent());
	}
	
	@Action(value = "ui/manager/tools/content/cache/clear")
	public void clear_content_cache(ActionContext<?> context) {
		getContext().get(EventBusFeature.class).eventBus().publish(new InvalidateContentCacheEvent());
	}
}
