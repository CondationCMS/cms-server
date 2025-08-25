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

import com.condation.cms.api.UIProperties;
import java.util.Optional;

/**
 *
 * @author t.marx
 */
public class ExtendedUIProperties implements UIProperties {

	
	private boolean force2fa = false;
	
	private boolean managerEnabled = false;

	@Override
	public boolean force2fa() {
		return force2fa;
	}

	@Override
	public boolean managerEnabled() {
		return managerEnabled;
	}
}
