package com.condation.cms.tests.template.xml;

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

import com.condation.cms.tests.template.xml.ast.AstNode;
import com.condation.cms.tests.template.xml.ast.HtmlElementNode;
import java.util.HashMap;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author thorstenmarx
 */
public class DefaultAstNodeFactory implements AstNodeFactory {

	@Override
	public boolean supports(String namespace, String localName) {
		return !"cms".equals(namespace)
				&& !"view".equalsIgnoreCase(namespace); // fallback for all standard HTML
	}

	@Override
	public AstNode create(XMLStreamReader reader, StAXTemplateRenderer parser, HtmlElementNode current) throws XMLStreamException {
		Map<String, String> attrs = new HashMap<>();
		for (int i = 0; i < reader.getAttributeCount(); i++) {
			attrs.put(reader.getAttributeLocalName(i), reader.getAttributeValue(i));
		}
		return new HtmlElementNode(reader.getLocalName(), attrs);
	}
}
