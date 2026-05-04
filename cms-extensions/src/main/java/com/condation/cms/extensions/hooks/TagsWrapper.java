package com.condation.cms.extensions.hooks;

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
import java.util.Map;
import java.util.function.Function;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class TagsWrapper {

	@Getter
	private final Map<String, Function<Parameter, String>> tags;

	public void put(final String namespace, final String tag, final Function<Parameter, String> function) {
        var ns = !Strings.isNullOrEmpty(namespace) ? namespace : "ext";
        var key = "%s:%s".formatted(ns, tag);
		tags.put(key, function);
	}
    
    public void put (final String tag, final Function<Parameter, String> function) {
        put(Constants.TemplateNamespaces.DEFAULT_MODULE_NAMESPACE, tag, function);
    }
}
