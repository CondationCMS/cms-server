package com.condation.cms.test;

/*-
 * #%L
 * cms-test
 * %%
 * Copyright (C) 2023 - 2025 CondationCMS
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

import com.condation.cms.api.UIProperties;

/**
 *
 * @author thorstenmarx
 */
public class TestUiProperties implements UIProperties {

	private boolean force2Fa;
	private boolean managerEnabled;

	public TestUiProperties() {
	}

	public TestUiProperties( boolean force2Fa, boolean managerEnabled) {
		this.force2Fa = force2Fa;
		this.managerEnabled = managerEnabled;
	}
	@Override
	public boolean force2fa() {
		return force2Fa;
	}

	@Override
	public boolean managerEnabled() {
		return managerEnabled;
	}
}
