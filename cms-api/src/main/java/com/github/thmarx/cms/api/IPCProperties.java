package com.github.thmarx.cms.api;

/*-
 * #%L
 * cms-api
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

import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

/**
 * Application Performance Management Properties
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class IPCProperties {
	private final Map<String, Object> properties;
	
	public int port () {
		return (int) properties.getOrDefault("port", 6868);
	}
	
	public Optional<String> password () {
		return Optional.ofNullable((String)properties.get("password"));
	}
	
}
