package com.github.thmarx.cms.extensions;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 Marx-Software
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.github.thmarx.cms.api.extensions.http.ExtensionHttpHandler;
import com.github.thmarx.cms.content.ContentTags;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
public class ExtensionHolder implements AutoCloseable {

	@Getter
	private final List<HttpHandlerExtension> httpHandlerExtensions = new ArrayList<>();
	@Getter
	private final List<TemplateSupplierExtension> registerTemplateSupplier = new ArrayList<>();
	@Getter
	private final List<TemplateFunctionExtension> registerTemplateFunctions = new ArrayList<>();
	@Getter
	private final Map<String, Function<ContentTags.Parameter, String>> tags = new HashMap<>();

	@Getter
	private final Context context;
	
	public void registerHttpExtension(final String method, final String path, final ExtensionHttpHandler handler) {
		httpHandlerExtensions.add(new HttpHandlerExtension(method, path, handler));
	}

	public Optional<HttpHandlerExtension> findHttpHandler (final String method, final String path) {
		return httpHandlerExtensions.stream().filter(handler -> handler.method().equalsIgnoreCase(method) && handler.path().equalsIgnoreCase(path)).findFirst();
	}
	
	public void registerTemplateSupplier(final String path, final Supplier<?> supplier) {
		registerTemplateSupplier.add(new TemplateSupplierExtension(path, supplier));
	}

	public void registerTemplateFunction(final String path, final Function<?, ?> function) {
		registerTemplateFunctions.add(new TemplateFunctionExtension(path, function));
	}
	
	public void addTag(final String tag, final Function<ContentTags.Parameter, String> function) {
		tags.put(tag, function);
	}

	@Override
	public void close() throws Exception {
		context.close();
	}
}
