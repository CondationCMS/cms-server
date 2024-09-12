package com.condation.cms.api;

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


import com.condation.cms.api.annotations.Experimental;
import java.util.Map;
import lombok.RequiredArgsConstructor;

/**
 * Application Performance Management Properties
 *
 * @author t.marx
 */
@Experimental(since = "5.3.0")
@RequiredArgsConstructor
public class PerformanceProperties {
	private final Map<String, Object> properties;
	
	public boolean pool_enabled () {
		return (boolean) properties.getOrDefault("pool_enabled", Boolean.FALSE);
	}
	/**
	 * pool size per site
	 * 
	 * @return 
	 */
	public int pool_size () {
		return (int) properties.getOrDefault("pool_size", 10);
	}
	
	
	public int pool_expire () {
		return (int) properties.getOrDefault("pool_expire", 10);
	}
	
	public int request_workers () {
		return (int) properties.getOrDefault("request_workers", 200);
	}
}
