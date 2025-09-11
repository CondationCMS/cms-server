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

import com.condation.cms.api.ui.extensions.UIRemoteMethodExtensionPoint;
import com.condation.cms.api.utils.AnnotationsUtil;
import com.condation.modules.api.ModuleManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.condation.cms.api.ui.annotations.RemoteMethod;
import com.condation.cms.auth.services.AuthorizationService;
import com.condation.cms.auth.services.User;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
public class RemoteMethodService {
	
	public Map<String, RMethod> handlers = new HashMap<>();
	
	protected static AuthorizationService authorizationService = new AuthorizationService();
	
	public void init (final ModuleManager moduleManager) {
		moduleManager.extensions(UIRemoteMethodExtensionPoint.class).forEach(this::register);
	}
	
	public void register (UIRemoteMethodExtensionPoint extension) {
		AnnotationsUtil.process(extension, RemoteMethod.class, List.of(Map.class), Object.class)
				.forEach(ann -> {
					handlers.put(ann.annotation().name(), 
							new RMethod(
									ann.annotation(), 
									(parameters) -> ann.invoke(parameters)
							));
				});
	}
	
	public Optional<?> execute (final String endpoint, final Map<String, Object> parameters, User user) {
		if (!handlers.containsKey(endpoint)) {
			return Optional.empty();
		} 
		return Optional.ofNullable(handlers.get(endpoint).execute(parameters, user));
	}
	
	@RequiredArgsConstructor
	public static class RMethod {
		private final RemoteMethod remoteMethodAnnotation;
		private final Function<Map<String, Object>, Object> function;
		
		public Object execute (final Map<String, Object> parameters, User user) {
			if (!RemoteMethodService.authorizationService.hasAnyPermission(user, remoteMethodAnnotation.permissions())) {
				throw new RemoteMethodException("access not allowed");
			}
			return function.apply(parameters);
		};
	}
	
	public static class RemoteMethodException extends RuntimeException {
		public RemoteMethodException (String message) {
			super(message);
		}
	}
}
