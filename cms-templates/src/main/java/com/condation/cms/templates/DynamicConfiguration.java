package com.condation.cms.templates;

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
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.content.shortcodes.ShortCodes;
import com.condation.cms.templates.components.TemplateComponents;
import com.condation.cms.templates.tags.component.EndComponentTag;
import com.condation.cms.templates.tags.component.ComponentTag;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author t.marx
 */
public record DynamicConfiguration(TemplateComponents templateComponents, Map<String, Component> components, RequestContext requestContext) {

	public static final DynamicConfiguration EMPTY = new DynamicConfiguration(
			new TemplateComponents(Collections.emptyMap()),
			Collections.emptyMap(),
			null
	);

	public DynamicConfiguration   {
		for (var tag : templateComponents.getComponentNames()) {
			var openTag = new ComponentTag(tag, templateComponents);
			var closeTag = new EndComponentTag(tag);

			components.put(openTag.getName(), openTag);
			components.put(closeTag.getName(), closeTag);
		}
	}

	public DynamicConfiguration(TemplateComponents templateComponents, RequestContext requestContext) {
		this(templateComponents, new HashMap<>(), requestContext);
	}

	public boolean hasComponent(String name) {
		return components.containsKey(name);
	}

	public Optional<Component> getComponent(String name) {
		return Optional.ofNullable(components.get(name));
	}
}
