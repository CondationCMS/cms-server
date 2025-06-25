package com.condation.cms.modules.system.api.services;

/*-
 * #%L
 * cms-system-modules
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author thorstenmarx
 */
public record ApiNavNode (String path, String name, Map<String, String> _links, List<ApiNavNode> children) {
	public ApiNavNode (String path, String name, Map<String, String> _links) {
		this(path, name, _links, Collections.emptyList());
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 29 * hash + Objects.hashCode(this.path);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ApiNavNode other = (ApiNavNode) obj;
		return Objects.equals(this.path, other.path);
	}
	
	
}
