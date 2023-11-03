package com.github.thmarx.cms.api.extensions;

/*-
 * #%L
 * cms-api
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

import com.github.thmarx.cms.api.CMSModuleContext;
import com.github.thmarx.modules.api.ExtensionPoint;
import com.github.thmarx.modules.api.ModuleConfiguration;
import lombok.Getter;

/**
 *
 * @author t.marx
 */
public abstract class AbstractExtensionEndpoint implements ExtensionPoint<CMSModuleContext> {
	@Getter
	protected ModuleConfiguration moduleConfiguration;
	@Getter
	protected CMSModuleContext context;

	@Override
	public void setConfiguration(ModuleConfiguration configuration) {
		this.moduleConfiguration = configuration;
	}

	@Override
	public void setContext(CMSModuleContext context) {
		this.context = context;
	}
	
	@Override
	public void init() {
	}
}
