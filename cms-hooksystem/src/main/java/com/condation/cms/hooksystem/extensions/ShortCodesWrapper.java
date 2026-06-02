package com.condation.cms.hooksystem.extensions;

/*-
 * #%L
 * CMS Extensions
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


import com.condation.cms.api.Constants;
import com.condation.cms.api.model.Parameter;
import com.google.common.base.Strings;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class ShortCodesWrapper {

	@Getter
	private final Map<String, Function<Parameter, String>> shortCodes;

	public void put(final String namespace, final String shortCode, final Function<Parameter, String> function) {
		var ns = !Strings.isNullOrEmpty(namespace) ? namespace : Constants.TemplateNamespaces.DEFAULT_MODULE_NAMESPACE;
		shortCodes.put("%s:%s".formatted(ns, shortCode), function);
	}

	public void put(final String shortCode, final Function<Parameter, String> function) {
		put(Constants.TemplateNamespaces.DEFAULT_MODULE_NAMESPACE, shortCode, function);
	}

	// called from JS: shortCodes.put("shortCodeName", ({name}) => ...)
	public void put(final String shortCode, final Value jsFunction) {
		put(Constants.TemplateNamespaces.DEFAULT_MODULE_NAMESPACE, shortCode, jsFunction);
	}

	// called from JS: shortCodes.put("namespace", "shortCodeName", ({name}) => ...)
	public void put(final String namespace, final String shortCode, final Value jsFunction) {
		put(namespace, shortCode, (Parameter param) -> {
			Map<String, Object> jsArgs = new HashMap<>(param);
			Value result = jsFunction.execute(ProxyObject.fromMap(jsArgs));
			return result.isNull() ? "" : result.asString();
		});
	}
}
