package com.condation.cms.content.shortcodes;

/*-
 * #%L
 * CMS Content
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.condation.cms.api.model.Parameter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 *
 * @author t.marx
 */
public class ShortCodeMap {

	private final Map<String, Function<Parameter, String>> shortCodes = new HashMap<>();

	public Set<String> names () {
		return Collections.unmodifiableSet(shortCodes.keySet());
	}
	
	public void put(String codeName, Function<Parameter, String> function) {
		shortCodes.put(codeName, function);
	}

	public void putAll(Map<String, Function<Parameter, String>> shortCodes) {
		this.shortCodes.putAll(shortCodes);
	}
	
	public boolean has(String codeName) {
		return shortCodes.containsKey(codeName);
	}

	public Function<Parameter, String> get(String codeName) {
		return shortCodes.getOrDefault(codeName, (params) -> "");
	}
}
