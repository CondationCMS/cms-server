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
import com.condation.cms.api.template.TemplateEngine;
import com.condation.modules.api.annotation.Extension;
import java.util.Map;

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
		return Map.of("ui", new UIHelper());	
	}
	
	public static class UIHelper {
		
		public String editContent (String editor) {
			return " data-cms-edit='true' data-cms-editor='%s' data-cms-element='content' ".formatted(editor);
		}
		
		public String editMeta (String editor, String element) {
			return " data-cms-edit='true' data-cms-editor='%s' data-cms-element='meta' data-cms-meta-element='%s' ".formatted(editor, element);
		}
		
		public String editForm () {
			return " data-cms-edit='true' data-cms-editor='form' data-cms-element='meta' ";
		}
		
		public String editFormElement (String editor, String element) {
			return " data-cms-editor='%s' data-cms-element='meta' data-cms-meta-element='%s' ".formatted(editor, element);
		}
		
		public String editContent (String editor, String uri) {
			return " data-cms-edit='true' data-cms-editor='%s' data-cms-element='content' data-cms-content-uri='%s' ".formatted(editor, uri);
		}
		
		public String editMeta (String editor, String element, String uri) {
			return " data-cms-edit='true' data-cms-editor='%s' data-cms-element='meta' data-cms-meta-element='%s' data-cms-content-uri='%s' ".formatted(editor, element, uri);
		}
		
		public String editForm (String uri) {
			return " data-cms-edit='true' data-cms-editor='form' data-cms-element='meta' data-cms-content-uri='%s' ".formatted(uri);
		}
		
		public String editFormElement (String editor, String element, String uri) {
			return " data-cms-editor='%s' data-cms-element='meta' data-cms-meta-element='%s' data-cms-content-uri='%s' ".formatted(editor, element, uri);
		}
	}
}
