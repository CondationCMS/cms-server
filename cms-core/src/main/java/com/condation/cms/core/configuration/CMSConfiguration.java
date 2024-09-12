package com.condation.cms.core.configuration;

/*-
 * #%L
 * cms-core
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


import com.condation.cms.api.PropertiesLoader;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
@RequiredArgsConstructor
public class CMSConfiguration implements IConfiguration {
	
	private final Path configFolder;
	
	@Override
	public <T extends ConfigProperties> Optional<T> load(String filename, Class<T> dataClass) {
		try {
			var constructor = dataClass.getConstructor(Map.class);
			var data = PropertiesLoader.rawProperties(configFolder.resolve(filename));
			return Optional.of(constructor.newInstance(data));
		} catch (Exception ex) {
			Logger.getLogger(CMSConfiguration.class.getName()).log(Level.SEVERE, null, ex);
		}
		return Optional.empty();
	}
	
	
}
