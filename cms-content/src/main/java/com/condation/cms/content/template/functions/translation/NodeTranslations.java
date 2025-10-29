package com.condation.cms.content.template.functions.translation;

/*-
 * #%L
 * cms-content
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

import com.condation.cms.api.Constants;
import com.condation.cms.api.SiteProperties;
import com.condation.cms.api.db.ContentNode;
import com.condation.cms.core.serivce.ServiceRegistry;
import com.condation.cms.core.serivce.impl.SiteLinkService;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author thorstenmarx
 */
public class NodeTranslations {
	
	private final ContentNode node;
	private final SiteProperties siteProperties;

	public NodeTranslations(ContentNode node, SiteProperties siteProperties) {
		this.node = node;
		this.siteProperties = siteProperties;
	}
	
	public List<TranslationDto> translations () {
		siteProperties.translation().getMapping().stream().map(mapping -> {
			var translations = (Map<String, Object>)node.data().getOrDefault(Constants.MetaFields.TRANSLATIONS, Collections.emptyMap());
			
			var url = "";
			if (translations.containsKey(mapping.language())) {
				var linkService = ServiceRegistry.getInstance().get(mapping.site(), SiteLinkService.class).get();
				url = linkService.link((String)translations.get(mapping.language()));
			}
			
			return new TranslationDto(mapping.language(), mapping.language().equals(siteProperties.language()), url);
		});
		return List.of();
	}
	
	public static record TranslationDto (String lang, boolean current, String url) {
	}
}
