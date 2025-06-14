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
import com.condation.cms.api.module.CMSRequestContext;
import com.condation.cms.api.template.TemplateEngine;
import com.condation.cms.api.utils.JSONUtil;
import com.condation.modules.api.annotation.Extension;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author thorstenmarx
 */
@Extension(TemplateModelExtendingExtensionPoint.class)
public class UiTemplateModelExtension extends TemplateModelExtendingExtensionPoint {

	@Override
	public void extendModel(TemplateEngine.Model model) {
	}

	@Override
	public Map<String, Object> getModel() {
		return Map.of("ui", new UIHelper(getRequestContext()));	
	}
	
	
	@RequiredArgsConstructor
	public static class UIHelper {
		
		private final CMSRequestContext requestContext;
		
		public String editContent (String editor) {
			if (!requestContext.has(IsPreviewFeature.class)) {
				return "";
			}
			return " data-cms-edit='true' data-cms-editor='%s' data-cms-element='content' ".formatted(editor);
		}
		
		public String editMeta (String editor, String element) {
			return editMeta(editor, element, Collections.emptyMap());
		}
		
		public String editMeta (String editor, String element, Map<String, Object> options) {
			if (!requestContext.has(IsPreviewFeature.class)) {
				return "";
			}
			return " data-cms-edit='true' data-cms-editor='%s' data-cms-editor-options='%s' data-cms-element='meta' data-cms-meta-element='%s' ".formatted(
					editor, 
					JSONUtil.toJson(options),
					element);
		}
		
		public String editSections (String sectionName) {
			if (!requestContext.has(IsPreviewFeature.class)) {
				return "";
			}
			return " data-cms-edit-sections='true' data-cms-section-name='%s' ".formatted(sectionName);
		}
		
		public String addSection () {
			if (!requestContext.has(IsPreviewFeature.class)) {
				return "";
			}
			return " data-cms-add-section='true' ";
		}
		
		public String editForm () {
			if (!requestContext.has(IsPreviewFeature.class)) {
				return "";
			}
			return " data-cms-edit='true' data-cms-editor='form' data-cms-element='meta' ";
		}
		
		public String editFormElement (String editor, String element) {
			return editFormElement(editor, element, Collections.emptyMap());
		}
		
		public String editFormElement (String editor, String element, Map<String, Object> options) {
			if (!requestContext.has(IsPreviewFeature.class)) {
				return "";
			}
			return " data-cms-editor='%s' data-cms-editor-options='%s' data-cms-element='meta' data-cms-meta-element='%s' ".formatted(
					editor,
					JSONUtil.toJson(options),
					element
			);
		}
		
		public String editContent (String editor, String uri) {
			if (!requestContext.has(IsPreviewFeature.class)) {
				return "";
			}
			return " data-cms-edit='true' data-cms-editor='%s' data-cms-element='content' data-cms-content-uri='%s' ".formatted(editor, uri);
		}
		
		public String editMeta (String editor, String element, String uri) {
			return editMeta(editor, element, uri, Collections.emptyMap());
		}
		
		public String editMeta (String editor, String element, String uri, Map<String, Object> options) {
			if (!requestContext.has(IsPreviewFeature.class)) {
				return "";
			}
			return " data-cms-edit='true' data-cms-editor='%s' data-cms-editor-options='%s' data-cms-element='meta' data-cms-meta-element='%s' data-cms-content-uri='%s' ".formatted(
					editor, 
					JSONUtil.toJson(options),
					element, 
					uri
			);
		}
		
		public String editForm (String uri) {
			if (!requestContext.has(IsPreviewFeature.class)) {
				return "";
			}
			return " data-cms-edit='true' data-cms-editor='form' data-cms-element='meta' data-cms-content-uri='%s' ".formatted(uri);
		}
		
		public String editFormElement (String editor, String element, String uri) {
			return editFormElement(editor, element, uri, Collections.emptyMap());
		}
		
		public String editFormElement (String editor, String element, String uri, Map<String, String> options) {
			if (!requestContext.has(IsPreviewFeature.class)) {
				return "";
			}
			return " data-cms-editor='%s' data-cms-editor-options='%s' data-cms-element='meta' data-cms-meta-element='%s' data-cms-content-uri='%s' ".formatted(
					editor, 
					JSONUtil.toJson(options),
					element, 
					uri);
		}
	}
}
