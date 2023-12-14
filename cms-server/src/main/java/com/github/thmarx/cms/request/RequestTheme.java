package com.github.thmarx.cms.request;

/*-
 * #%L
 * cms-server
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

import com.github.thmarx.cms.api.ThemeProperties;
import com.github.thmarx.cms.api.theme.Assets;
import com.github.thmarx.cms.api.theme.Theme;
import com.github.thmarx.cms.theme.DefaultAssets;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class RequestTheme implements Theme {
	private final Theme wrapped;

	private final Assets assets = new DefaultAssets();
	
	@Override
	public Assets getAssets() {
		return assets;
	}

	@Override
	public String getName() {
		return wrapped.getName();
	}

	@Override
	public Path templatesPath() {
		return wrapped.templatesPath();
	}

	@Override
	public Path extensionsPath() {
		return wrapped.extensionsPath();
	}

	@Override
	public Path assetsPath() {
		return wrapped.assetsPath();
	}

	@Override
	public ThemeProperties properties() {
		return wrapped.properties();
	}

	@Override
	public boolean empty() {
		return wrapped.empty();
	}
}
