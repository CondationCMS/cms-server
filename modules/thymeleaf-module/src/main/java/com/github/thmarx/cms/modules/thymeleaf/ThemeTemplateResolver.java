package com.github.thmarx.cms.modules.thymeleaf;

/*-
 * #%L
 * thymeleaf-module
 * %%
 * Copyright (C) 2023 Marx-Software
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
