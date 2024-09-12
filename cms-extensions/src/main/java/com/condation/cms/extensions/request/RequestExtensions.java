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
import com.condation.cms.api.model.Parameter;
import com.condation.cms.extensions.HttpHandlerExtension;
import com.condation.cms.extensions.TemplateFunctionExtension;
import com.condation.cms.extensions.TemplateSupplierExtension;
import com.condation.cms.extensions.http.ExtensionHttpHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;
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
	private final List<HttpHandlerExtension> httpHandlerExtensions = new ArrayList<>();
	@Getter
	private final List<HttpHandlerExtension> httpRouteHandlerExtensions = new ArrayList<>();
	@Getter
	private final List<TemplateSupplierExtension> registerTemplateSupplier = new ArrayList<>();
	@Getter
	private final List<TemplateFunctionExtension> registerTemplateFunctions = new ArrayList<>();
	@Getter
	private final Map<String, Function<Parameter, String>> shortCodes = new HashMap<>();
	@Getter
	private final Map<String, BiPredicate<Object, Object>> queryOperations = new HashMap<>();

	@Getter
	private final Context context;
	
	@Deprecated
	public void registerQueryOperation (final String operation, final BiPredicate<Object, Object> predicate) {
		queryOperations.put(operation, predicate);
	}
	
	@Deprecated
	public void registerHttpExtension(final String method, final String path, final ExtensionHttpHandler handler) {
		httpHandlerExtensions.add(new HttpHandlerExtension(method, path, handler));
	}
	@Deprecated
	public Optional<HttpHandlerExtension> findHttpHandler (final String method, final String path) {
		return httpHandlerExtensions.stream().filter(handler -> handler.method().equalsIgnoreCase(method) && handler.path().equalsIgnoreCase(path)).findFirst();
	}
	@Deprecated
	public void registerHttpRouteExtension(final String method, final String path, final ExtensionHttpHandler handler) {
		httpRouteHandlerExtensions.add(new HttpHandlerExtension(method, path, handler));
	}
	@Deprecated
	public Optional<HttpHandlerExtension> findHttpRouteHandler (final String method, final String path) {
		return httpRouteHandlerExtensions.stream().filter(handler -> handler.method().equalsIgnoreCase(method) && handler.path().equalsIgnoreCase(path)).findFirst();
	}
	@Deprecated
	public void registerTemplateSupplier(final String path, final Supplier<?> supplier) {
		registerTemplateSupplier.add(new TemplateSupplierExtension(path, supplier));
	}
	@Deprecated
	public void registerTemplateFunction(final String path, final Function<?, ?> function) {
		registerTemplateFunctions.add(new TemplateFunctionExtension(path, function));
	}
	@Deprecated
	public void registerShortCode(final String shortCode, final Function<Parameter, String> function) {
		shortCodes.put(shortCode, function);
	}

	@Override
	public void close() throws Exception {
		context.close();
	}
}
