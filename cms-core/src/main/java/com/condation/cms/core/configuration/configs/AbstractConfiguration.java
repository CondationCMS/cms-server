package com.condation.cms.core.configuration.configs;

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

import com.condation.cms.core.configuration.ConfigSource;
import com.condation.cms.core.configuration.GSONProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
public abstract class AbstractConfiguration {
	
	abstract protected List<ConfigSource> getSources ();
	
	public List<Object> getList (String field) {
		List<Object> result = new ArrayList<>();
		getSources().stream()
				.filter(ConfigSource::exists)
				.map(config -> config.getList(field))
				.forEach(result::addAll);
		return result;
	}
	
	public <T> List<T> getList(String field, Class<T> aClass) {
		try {
			var list = getList(field);
			
			return list.stream()
					.map(item -> GSONProvider.GSON.toJson(item))
					.map(item -> GSONProvider.GSON.fromJson(item, aClass))
					.collect(Collectors.toList());
		} catch (Exception ex) {
			log.error("", ex);
			throw new RuntimeException(ex);
		}
	}
}
