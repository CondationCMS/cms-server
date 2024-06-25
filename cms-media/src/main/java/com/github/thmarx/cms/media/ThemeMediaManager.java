package com.github.thmarx.cms.media;

/*-
 * #%L
 * cms-media
 * %%
 * Copyright (C) 2023 - 2024 Marx-Software
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

import com.github.thmarx.cms.api.configuration.Configuration;
import com.github.thmarx.cms.api.theme.Theme;
import java.nio.file.Path;

/**
 *
 * @author t.marx
 */
public class ThemeMediaManager extends MediaManager {
	public ThemeMediaManager(Path tempFolder, Theme theme, Configuration configuration) {
		this.assetBase = theme.assetsPath();
		this.tempFolder = tempFolder;
		this.theme = theme;
		this.configuration = configuration;
	}
	
	@Override
	public void reloadTheme (Theme updateTheme) {
		this.assetBase = updateTheme.assetsPath();
		this.theme = updateTheme;
		this.mediaFormats = null;
	}
}
