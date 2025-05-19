package com.condation.cms.modules.ui.services;

/*-
 * #%L
 * ui-module
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

import com.condation.cms.api.ui.annotations.RemoteEndpoint;
import com.condation.cms.api.ui.extensions.UIRemoteEndpointExtensionPoint;
import com.condation.cms.api.utils.AnnotationsUtil;
import com.condation.modules.api.ModuleManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author t.marx
 */
public class RemoteCallService {
	
	public Map<String, Enpoint> handlers = new HashMap<>();
	
	public void init (final ModuleManager moduleManager) {
		moduleManager.extensions(UIRemoteEndpointExtensionPoint.class).forEach(this::register);
	}
	
	public void register (UIRemoteEndpointExtensionPoint extension) {
		AnnotationsUtil.process(extension, RemoteEndpoint.class, List.of(Map.class), Object.class)
				.forEach(ann -> {
					handlers.put(ann.annotation().endpoint(), (parameters) -> {
						return ann.invoke(parameters);
					});
				});
	}
	
	public Optional<?> execute (final String endpoint, final Map<String, Object> parameters) {
		if (!handlers.containsKey(endpoint)) {
			return Optional.empty();
		} 
		return Optional.ofNullable(handlers.get(endpoint).execute(parameters));
	}
	
	public static interface Enpoint {
		public Object execute (final Map<String, Object> parameters);
	}
}
