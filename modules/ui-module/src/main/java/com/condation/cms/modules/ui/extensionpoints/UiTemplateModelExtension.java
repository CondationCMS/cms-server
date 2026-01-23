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

import com.condation.cms.api.extensions.TemplateModelExtendingExtensionPoint;
import com.condation.cms.api.feature.features.IsPreviewFeature;
import com.condation.cms.api.module.SiteRequestContext;
import com.condation.cms.api.template.TemplateEngine;
import com.condation.cms.api.utils.JSONUtil;
import com.condation.modules.api.annotation.Extension;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author thorstenmarx
 */
@Extension(TemplateModelExtendingExtensionPoint.class)
public class UiTemplateModelExtension extends TemplateModelExtendingExtensionPoint {

	@Override
	public Map<String, Object> getModel() {
		return Map.of("ui", new UIHelper(getRequestContext()));	
	}	
	
	@RequiredArgsConstructor
	public static class UIHelper {
		
		private final SiteRequestContext requestContext;
		
		public String editMeta (String editor, String element) {
			return editMeta(editor, element, Collections.emptyMap());
		}
		
		public String editMeta (String editor, String element, Map<String, Object> options) {
			if (!requestContext.has(IsPreviewFeature.class)) {
				return "";
			}
			return " data-cms-editor='%s' data-cms-editor-options='%s' data-cms-element='meta' data-cms-meta-element='%s' ".formatted(
					editor, 
					JSONUtil.toJson(options),
					element);
		}
		
		public String editMeta (String editor, String element, String uri, String toolbar) {
			return editMeta(editor, element, uri, toolbar, Collections.emptyMap());
		}
		
		public String editMeta (String editor, String element, String uri, String toolbar, Map<String, Object> options) {
			if (!requestContext.has(IsPreviewFeature.class)) {
				return "";
			}
			return " data-cms-editor='%s' data-cms-editor-options='%s' data-cms-element='meta' data-cms-meta-element='%s' %s ".formatted(
					editor, 
					JSONUtil.toJson(options),
					element, 
					toolbar(toolbar, uri)
			);
		}
		
		
		public String toolbar (String id, String type, String[] actions) {
			return toolbar(id, type, actions, Map.of());
		}
		public String toolbar (String id, String type, String[] actions, Map<String, Object> additional) {
			if (!requestContext.has(IsPreviewFeature.class)) {
				return "";
			}
			Map<String, Object> base = Map.of(
					"id", id,
					"type", type,
					"actions", actions
			);
			HashMap<String, Object> toolbar = new HashMap<>(additional);
			toolbar.putAll(base);
			return " data-cms-toolbar='%s'  ".formatted(
					JSONUtil.toJson(toolbar)
			);
		}
		public String toolbar (String id, String uri) {
			return toolbar(id, uri, new String[0]);
		}
		
		public String mediaToolbar (String [] actions, Map<String, Object> options) {
			if (!requestContext.has(IsPreviewFeature.class)) {
				return "";
			}
			
			Map<String, Object> toolbar = Map.of(
					"actions", actions,
					"options", options
			);
			return " data-cms-media-toolbar='%s'  ".formatted(
					JSONUtil.toJson(toolbar)
			);
		}
		
	}
}
