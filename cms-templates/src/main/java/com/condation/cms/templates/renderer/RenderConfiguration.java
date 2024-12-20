package com.condation.cms.templates.renderer;

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
import com.condation.cms.templates.DynamicConfiguration;
import com.condation.cms.templates.Tag;
import com.condation.cms.templates.TemplateConfiguration;
import java.util.Optional;

/**
 *
 * @author t.marx
 */
public record RenderConfiguration(TemplateConfiguration templateEngineConfiguration, DynamicConfiguration dynamicConfiguration) {

	public RenderConfiguration(TemplateConfiguration templateConfiguration) {
		this(templateConfiguration, null);
	}

	public boolean hasTag(String tagName) {
		return templateEngineConfiguration.hasTag(tagName);
	}

	public Optional<Tag> getTag(String tagName) {
		return templateEngineConfiguration.getTag(tagName);
	}

	public boolean hasComponent(String name) {
		return (dynamicConfiguration != null && dynamicConfiguration.hasComponent(name));
	}

	public Optional<Component> getComponent(String name) {
		if (dynamicConfiguration != null && dynamicConfiguration.hasComponent(name)) {
			return dynamicConfiguration.getComponent(name);
		}

		return Optional.empty();
	}
}
