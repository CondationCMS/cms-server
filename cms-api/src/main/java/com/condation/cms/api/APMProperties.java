package com.github.thmarx.cms.api;

/*-
 * #%L
 * cms-api
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


import java.time.Duration;
import java.util.Map;
import lombok.RequiredArgsConstructor;

/**
 * Application Performance Management Properties
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class APMProperties {
	private final Map<String, Object> properties;
	
	public boolean enabled () {
		return (boolean) properties.getOrDefault("enabled", Boolean.FALSE);
	}
	
	public int max_requests () {
		return (int) properties.getOrDefault("max_requests", 100);
	}
	
	public int thread_limit () {
		return (int) properties.getOrDefault("thread_limit", 10);
	}
	
	public Duration max_suspend () {
		if (!properties.containsKey("max_suspend")) {
			return Duration.ZERO;
		}
		return Duration.parse((String) properties.get("max_suspend"));
	}
}
