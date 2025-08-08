package com.condation.cms.templates.tags.component;

/*-
 * #%L
 * cms-templates
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

import com.condation.cms.templates.Component;
import com.condation.cms.templates.components.TemplateComponents;
import com.condation.cms.templates.exceptions.RenderException;
import com.condation.cms.templates.parser.ComponentNode;
import com.condation.cms.templates.renderer.Renderer;
import com.condation.cms.templates.utils.ParameterUtil;
import com.google.common.base.Strings;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class ComponentTag implements Component {

	private final String componentName;

	private final TemplateComponents components;

	@Override
	public String getName() {
		return componentName;
	}
	

	@Override
	public Optional<String> getCloseingName() {
		return Optional.of("end%s".formatted(componentName));
	}

	@Override
	public void render(ComponentNode node, Renderer.Context context, Writer writer) {
		try {
			
			var params = ParameterUtil.parseAndEvaluate(node.getParameters(), context.createEngineContext(), context.engine());
			var content = renderChildren(node, context);

			params.put("_content", content);
			
			var componentResult = components.execute(componentName, params, context.dynamicConfiguration().requestContext());
			if (!Strings.isNullOrEmpty(componentResult)) {
				writer.write(componentResult);
			}
		} catch (Exception e) {	
			throw new RenderException(e.getMessage(), node.getLine(), node.getColumn());
		}
	}

	private String renderChildren(ComponentNode node, Renderer.Context context) {
		try {
			StringWriter writer = new StringWriter();
			for (var child : node.getChildren()) {
				context.renderer().render(child, context, writer);
			}
			return writer.toString();
		} catch (IOException ioe) {
			throw new RenderException(ioe.getMessage(), node.getLine(), node.getColumn());
		}
	}

}
