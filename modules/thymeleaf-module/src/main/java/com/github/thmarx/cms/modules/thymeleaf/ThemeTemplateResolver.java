package com.github.thmarx.cms.modules.thymeleaf;

/*-
 * #%L
 * thymeleaf-module
 * %%
 * Copyright (C) 2023 Marx-Software
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

import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolution;

/**
 *
 * @author thmar
 */
@RequiredArgsConstructor
public class ThemeTemplateResolver implements ITemplateResolver {

	private final ITemplateResolver siteTemplateResolver;
	private final Optional<ITemplateResolver> themeTemplateResolver;
	
	@Setter
	private int order = 0;
	
	@Override
	public String getName() {
		return "ThemeTemlateResolver";
	}

	@Override
	public Integer getOrder() {
		return order;
	}

	@Override
	public TemplateResolution resolveTemplate(IEngineConfiguration configuration, String ownerTemplate, String template, Map<String, Object> templateResolutionAttributes) {
		TemplateResolution resolveTemplate = siteTemplateResolver.resolveTemplate(configuration, ownerTemplate, template, templateResolutionAttributes);
		
		if ((resolveTemplate == null || !resolveTemplate.getTemplateResource().exists()) && themeTemplateResolver.isPresent()) {
			return themeTemplateResolver.get().resolveTemplate(configuration, ownerTemplate, template, templateResolutionAttributes);
		}
		return resolveTemplate;
	}
	
}
