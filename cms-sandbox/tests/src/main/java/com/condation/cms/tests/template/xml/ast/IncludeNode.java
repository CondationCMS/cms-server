package com.condation.cms.tests.template.xml.ast;

/*-
 * #%L
 * tests
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

import com.condation.cms.tests.template.xml.TemplateLoader;
import java.util.Map;

/**
 *
 * @author thorstenmarx
 */
public class IncludeNode extends AstNode {

	private final String templateName;
	private final TemplateLoader loader;

	public IncludeNode(String templateName, TemplateLoader loader) {
		this.templateName = templateName;
		this.loader = loader;
	}

	@Override
	public String render(Map<String, Object> context) {
		try {
			AstNode node = loader.load(templateName);
			return node.render(context);
		} catch (Exception e) {
			return "<!-- include error: " + e.getMessage() + " -->";
		}
	}
}
