package com.github.thmarx.cms.api.feature.features;

import com.github.thmarx.cms.api.feature.Feature;
import com.github.thmarx.cms.api.theme.Theme;
import lombok.AllArgsConstructor;

/*-
 * #%L
 * cms-api
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

/**
 *
 * @author t.marx
 */
@AllArgsConstructor
public class ThemeFeature implements Feature {

	private Theme theme;
	
	public Theme theme () {
		return theme;
	}
	
	public void updateTheme (Theme theme) {
		this.theme = theme;
	}
}
