package com.condation.cms.test;

/*-
 * #%L
 * cms-test
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

import com.condation.cms.api.TranslationProperties;
import com.condation.cms.api.UIProperties;
import java.util.List;

/**
 *
 * @author thorstenmarx
 */
public class TestTranslationProperties implements TranslationProperties {

	private boolean enabled;
	private List<String> language;
	private List<Mapping> mapping;

	public TestTranslationProperties(boolean enabled, List<String> language, List<Mapping> mapping) {
		this.enabled = enabled;
		this.language = language;
		this.mapping = mapping;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public List<String> getLanguages() {
		return language;
	}

	@Override
	public List<Mapping> getMapping() {
		return mapping;
	}
}
