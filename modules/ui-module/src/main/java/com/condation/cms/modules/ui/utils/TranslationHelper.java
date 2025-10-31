package com.condation.cms.modules.ui.utils;

/*-
 * #%L
 * ui-module
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

import com.condation.cms.api.SiteProperties;
import com.condation.cms.api.TranslationProperties;
import java.util.List;

/**
 *
 * @author thmar
 */
public class TranslationHelper {
	private final TranslationProperties translationProperties;
	private final SiteProperties siteProperties;

	public TranslationHelper(SiteProperties siteProperties) {
		this.translationProperties = siteProperties.translation();
		this.siteProperties = siteProperties;
	}
	
	public boolean isEnabled () {
		return translationProperties.isEnabled();
	}
	
	public List<String> getLanguages () {
		return translationProperties.getLanguages();
	}
	
	public List<String> getFilteredLanguages () {
		return translationProperties.getLanguages().stream()
				.filter(lang -> !lang.equals(siteProperties.language()))
				.toList();
	}
	
	public List<TranslationProperties.Mapping> getMapping () {
		return translationProperties.getMapping();
	}
	
	public List<TranslationProperties.Mapping> getFilteredMapping () {
		return translationProperties.getMapping().stream()
				.filter(mapping -> !mapping.language().equals(siteProperties.language()) )
				.toList();
	}
}
