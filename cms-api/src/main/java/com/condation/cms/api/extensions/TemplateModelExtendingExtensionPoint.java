package com.condation.cms.api.extensions;

import java.util.Map;

import com.condation.cms.api.Constants;

/*-
 * #%L
 * cms-api
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

import com.condation.cms.api.template.TemplateEngine;

public abstract class TemplateModelExtendingExtensionPoint extends AbstractExtensionPoint{

	/**
	 * deprecated: use @TemplateModelExtendingExtensionPoint.getModel instead
	 * @param model
	 */
	@Deprecated(since = "7.3.0", forRemoval = true)
	public abstract void extendModel (TemplateEngine.Model model);

	public Map<String, Object> getModel () {
		TemplateEngine.Model model = new TemplateEngine.Model(null, null, null);
		extendModel(model);
		return model.values;
	}

	public String getNamespace () {
		return Constants.TemplateNamespaces.DEFAULT_MODULE_NAMESPACE;
	}

}
