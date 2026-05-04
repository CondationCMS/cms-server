package com.condation.cms.templates.components;

/*-
 * #%L
 * CMS Templates
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
public class FunctionMap {

    public record ExtFunction (String namespace, String name, Function<Parameter, ?> function){
        public ExtFunction(String name, Function<Parameter, ?> fn) {
            this(null, name, fn);
        }
    };
    
	private final Set<ExtFunction> functions = new HashSet<>();

	public Set<ExtFunction> functions() {
		return Collections.unmodifiableSet(functions);
	}

	public void put(String namespace, String codeName, Function<Parameter, ?> function) {
		functions.add(new ExtFunction(namespace, codeName, function));
	}

	public void putAll(String namespace, Map<String, Function<Parameter, ?>> functions) {
        functions.forEach((name, fn) -> {
            this.functions.add(new ExtFunction(namespace, name, fn));
        });
		
	}

	public boolean has(String namespace, String name) {
		return functions.stream().anyMatch(fn -> fn.namespace.equals(namespace) && fn.name.equals(name));
	}

	public Optional<ExtFunction> get(String namespace, String name) {
		return functions.stream().filter(fn -> fn.namespace.equals(namespace) && fn.name.equals(name)).findFirst();
	}
}
