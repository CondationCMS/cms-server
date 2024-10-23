package com.condation.cms.core.configuration.properties;

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

import com.condation.cms.api.PerformanceProperties;

/**
 *
 * @author t.marx
 */
public class ExtendedPerformanceProperties implements PerformanceProperties {

	private boolean pool_enabled = false;
	private int pool_size = 10;
	private int pool_expire = 10;
	private int request_workers = 200;
	
	@Override
	public boolean pool_enabled() {
		return pool_enabled;
	}

	@Override
	public int pool_size() {
		return pool_size;
	}

	@Override
	public int pool_expire() {
		return pool_expire;
	}

	@Override
	public int request_workers() {
		return request_workers;
	}

	
}
