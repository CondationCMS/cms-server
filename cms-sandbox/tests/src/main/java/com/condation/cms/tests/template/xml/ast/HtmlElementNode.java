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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author thorstenmarx
 */
public class HtmlElementNode extends AstNode {

	private final String tagName;
	private final Map<String, String> attributes;
	private final List<AstNode> children = new ArrayList<>();

	public HtmlElementNode(String tagName, Map<String, String> attributes) {
		this.tagName = tagName;
		this.attributes = attributes;
	}

	public void addChild(AstNode node) {
		children.add(node);
	}

	@Override
	public String render(Map<String, Object> context) {
		StringBuilder sb = new StringBuilder();
		sb.append("<").append(tagName);
		attributes.forEach((k, v) -> sb.append(" ").append(k).append("=\"").append(v).append("\""));
		sb.append(">");
		for (AstNode child : children) {
			sb.append(child.render(context));
		}
		sb.append("</").append(tagName).append(">");
		return sb.toString();
	}
}
