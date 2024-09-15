package com.condation.modules.manager;

/*-
 * #%L
 * modules-manager
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
public final class ModuleServiceLoader {

	static final String PREFIX = "META-INF/services/";

	private final ClassLoader loader;

	private final ConcurrentMap<Class<?>, List<Provider<?>>> providers = new ConcurrentHashMap<>();

	private ModuleServiceLoader(ClassLoader loader) {
		this.loader = loader;
	}

	public static ModuleServiceLoader create(ClassLoader loader) {
		return new ModuleServiceLoader(loader);
	}

	public <S> List<S> get(Class<S> service) {
		try {
			return providers.computeIfAbsent(service, clazz -> {
				return initService(clazz);
			}).stream()
					.map(this::newInstance)
					.filter(Objects::nonNull)
					.map(service::cast)
					.toList();
		} catch (Exception ex) {
			log.error("", ex);
		}
		return Collections.emptyList();
	}

	private <S> S newInstance(Provider<S> provider) {
		try {
			return provider.get();
		} catch (Exception ex) {
			log.error("error createing instance", ex);
		}
		return null;
	}

	private <S> List<Provider<?>> initService(Class<S> service) {

		List<Provider<?>> providerImpls = new ArrayList<>();
		try {
			String fullName = PREFIX + service.getName();
			Enumeration<URL> resources = loader.getResources(fullName);
			while (resources.hasMoreElements()) {
				var url = resources.nextElement();
				try (var ins = url.openStream(); var reader = new BufferedReader(new InputStreamReader(ins))) {

					String line;
					while ((line = reader.readLine()) != null) {
						var serviceImplClass = (Class<S>) Class.forName(line, false, loader);
						providerImpls.add(new Provider<>(serviceImplClass));
					}
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}

		return providerImpls;
	}

	private record Provider<S>(Class<S> type) {

		public S get() throws Exception {
			return (S) type.getConstructors()[0].newInstance();
		}
	}
}
