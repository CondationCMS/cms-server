package com.github.thmarx.cms.modules.pebble;

/*-
 * #%L
 * example-module
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

import com.github.thmarx.cms.api.extensions.TemplateEngineProviderExtentionPoint;
import com.github.thmarx.cms.api.template.TemplateEngine;
import com.github.thmarx.modules.api.annotation.Extension;

/**
 *
 * @author t.marx
 */
@Extension(TemplateEngineProviderExtentionPoint.class)
public class PebbleTemplateEngineProviderExtentionPoint extends TemplateEngineProviderExtentionPoint {

	PebbleTemplateEngine templateEngine;
	
	@Override
	public void init() {
		templateEngine = new PebbleTemplateEngine(getContext().getFileSystem(), getContext().getServerProperties());
	}

	@Override
	public String getName() {
		return "pebble";
	}

	@Override
	public TemplateEngine getTemplateEngine() {
		return templateEngine;
	}

}
