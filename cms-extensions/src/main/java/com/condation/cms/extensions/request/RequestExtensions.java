package com.condation.cms.extensions.request;

/*-
 * #%L
 * cms-extensions
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


import com.condation.cms.api.annotations.FeatureScope;
import com.condation.cms.api.feature.Feature;
import java.net.URLClassLoader;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.graalvm.polyglot.Context;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
@FeatureScope(FeatureScope.Scope.REQUEST)
public class RequestExtensions implements AutoCloseable, Feature {

	@Getter
	private final Context context;

	@Getter
	private final ClassLoader libsClassLoader;
	
	@Override
	public void close() throws Exception {
		if (context != null) {
			context.close();
		}
		if (libsClassLoader != null) {
			((URLClassLoader)libsClassLoader).close();
		}
	}
}
