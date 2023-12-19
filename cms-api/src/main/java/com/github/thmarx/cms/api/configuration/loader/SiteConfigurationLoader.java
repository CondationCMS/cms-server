package com.github.thmarx.cms.api.configuration.loader;

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

import com.github.thmarx.cms.api.PropertiesLoader;
import com.github.thmarx.cms.api.configuration.Loader;
import com.github.thmarx.cms.api.configuration.configs.SiteConfiguration;
import java.io.IOException;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class SiteConfigurationLoader implements Loader<SiteConfiguration> {

	private final Path hostBase;
	
	@Override
	public SiteConfiguration load() throws IOException {
		var props = hostBase.resolve("site.yaml");
		return new SiteConfiguration(PropertiesLoader.hostProperties(props));
	}

	@Override
	public void reload(final SiteConfiguration config) throws IOException {
		var props = hostBase.resolve("site.yaml");
		config.siteProperties().update(PropertiesLoader.rawProperties(props));
	}
	
}
