package com.condation.cms.api.extensions.server;

/*-
 * #%L
 * cms-api
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

import com.condation.cms.api.module.ServerModuleContext;
import com.condation.cms.api.module.ServerRequestContext;
import com.condation.modules.api.ExtensionPoint;
import com.condation.modules.api.ModuleConfiguration;
import lombok.Getter;

/**
 *
 * @author thmar
 */
public abstract class ServerExtensionPoint implements ExtensionPoint<ServerModuleContext, ServerRequestContext> {
	@Getter
	protected ModuleConfiguration moduleConfiguration;
	@Getter
	protected ServerModuleContext context;
	@Getter
	protected ServerRequestContext requestContext;

	@Override
	public void setConfiguration(ModuleConfiguration configuration) {
		this.moduleConfiguration = configuration;
	}

	@Override
	public void setContext(ServerModuleContext context) {
		this.context = context;
	}

	@Override
	public void setRequestContext(ServerRequestContext requestContext) {
		this.requestContext = requestContext;
	}

	@Override
	public void init() {
	}
}
