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

import com.condation.modules.api.Context;
import com.condation.modules.api.ExtensionPoint;
import com.condation.modules.api.Module;
import com.condation.modules.api.Module.Priority;
import com.condation.modules.api.ModuleConfiguration;
import com.condation.modules.api.ModuleRequestContextFactory;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author thmarx
 */
public class ModuleImpl implements Module {

	private String id;
	private String version;
	private String name;
	private String description;
	private String author;
	private Priority priority = Priority.NORMAL;
	private final List<Dependency> dependencyList = new ArrayList<>();
	private final ModuleRequestContextFactory requestContextFactory;

	File moduleDir;

	File modulesDataDir;

	URLClassLoader classloader;

	ModuleConfiguration configuration;

	private final Context context;
	private final ModuleInjector injector;

	Map<Class, List> extensions = new HashMap<>();
	
	private ModuleServiceLoader moduleServiceLoader;
	
	protected ModuleImpl(final File moduleDir, final File modulesDataDir, final Context context,
			final ModuleInjector injector, final ModuleRequestContextFactory requestContextFactory) throws MalformedURLException, IOException {
		this.moduleDir = moduleDir;
		this.modulesDataDir = modulesDataDir;
		this.context = context;
		this.injector = injector;
		this.requestContextFactory = requestContextFactory;

		Properties properties = new Properties();
		try (FileReader reader = new FileReader(new File(moduleDir, "module.properties"))) {
			properties.load(reader);
			this.id = properties.getProperty("id");
			this.name = properties.getProperty("name");
			this.version = properties.getProperty("version");
			this.description = properties.getProperty("description");
			this.author = properties.getProperty("author");
			String dependencies = properties.getProperty("dependencies");
			if (dependencies != null && !dependencies.equals("")) {
				for (String dep : dependencies.split(";")) {
					String[] dependency = dep.split("#");
					if (dependency != null && dependency.length == 2) {
						dependencyList.add(new Dependency(dependency[0], dependency[1]));
					}
				}
			}
			String config_prio = properties.getProperty("priority", "NORMAL");
			this.priority = Priority.valueOf(config_prio);
		}
	}

	public void init(final ClassLoader parentClassLoader) throws MalformedURLException, IOException {
		List<URL> urls = new ArrayList<>();

		File[] libs = new File(moduleDir, "libs").listFiles((File dir, String name1) -> name1.endsWith(".jar"));
		for (File lib : libs) {
			urls.add(new URL("jar:" + lib.toURI().toURL() + "!/"));
			lib = null;
		}

		classloader = new ModuledFirstURLClassLoader(urls.toArray(new URL[libs.length]), parentClassLoader);
		urls.clear();
		urls = null;
		libs = null;

		File dataDir = new File(modulesDataDir, id);
		if (!dataDir.exists()) {
			dataDir.mkdirs();
		}
		this.configuration = new ModuleConfiguration(dataDir);
		
		this.moduleServiceLoader = ModuleServiceLoader.create(classloader);
	}

	@Override
	public boolean provides(Class<? extends ExtensionPoint> extensionClass) {
		ServiceLoader<? extends ExtensionPoint> serviceLoader = ServiceLoader.load(extensionClass, classloader);
		return serviceLoader.iterator().hasNext();
	}
	
	@Override
	public <T extends ExtensionPoint> List<T> extensions(Class<T> extensionClass) {
		
		return moduleServiceLoader.get(extensionClass).stream()
				.map(ext -> {
			ext.setContext(context);
			ext.setConfiguration(configuration);

			if (requestContextFactory != null) {
				ext.setRequestContext(requestContextFactory.createContext());
			}

			if (injector != null) {
				injector.inject(ext);
			}

			ext.init();

			return ext;
		}).toList();
	}

	@Override
	public Priority getPriority() {
		return priority;
	}

	@Override
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public List<Dependency> getDependencies() {
		return this.dependencyList;
	}

	public File getModuleDir() {
		return moduleDir;
	}

	public File getModulesDataDir() {
		return modulesDataDir;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 61 * hash + Objects.hashCode(this.id);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ModuleImpl other = (ModuleImpl) obj;
		if (!Objects.equals(this.id, other.id)) {
			return false;
		}
		return true;
	}

	public void close() throws IOException {
		this.classloader.close();

		// workaround: close all libs manually: see https://bugs.openjdk.java.net/browse/JDK-7183373
		for (URL u : this.classloader.getURLs()) {
			if (u.getProtocol().equals("jar")) {
				((JarURLConnection) u.openConnection()).getJarFile().close();
			}
		}

		this.classloader = null;
		this.configuration = null;
		this.dependencyList.clear();
		this.extensions.clear();
		this.extensions = null;
		this.modulesDataDir = null;
		this.moduleDir = null;
	}

	public static class Dependency {

		private final String id;
		private final String version;

		public Dependency(String id, String version) {
			this.id = id;
			this.version = version;
		}

		public String id() {
			return id;
		}

		public String version() {
			return version;
		}

	}
}
