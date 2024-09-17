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
import com.condation.modules.api.ManagerConfiguration;
import com.condation.modules.api.Module;
import com.condation.modules.api.ModuleDescription;
import com.condation.modules.api.ModuleLifeCycleExtension;
import com.condation.modules.api.ModuleManager;
import com.condation.modules.api.ModuleRequestContextFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ModuleManager loads all modules from a given directoy.
 *
 * @author thmarx
 */
public class ModuleManagerImpl implements ModuleManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(ModuleManagerImpl.class);

	public static class Builder {

		private File modulesPath = null;
		private File modulesDataPath = null;
		private Context context = null;
		private ModuleAPIClassLoader classLoader = null;
		private ModuleInjector injector = null;
		private ModuleRequestContextFactory requestContextFactory = null;

		public Builder requestContextFactory(ModuleRequestContextFactory requestContextFactory) {
			this.requestContextFactory = requestContextFactory;
			return this;
		}

		public ModuleManager build() {
			return new ModuleManagerImpl(this);
		}

		public Builder setModulesPath(File path) {
			this.modulesPath = path;
			return this;
		}

		public Builder setModulesDataPath(File path) {
			this.modulesDataPath = path;
			return this;
		}

		public Builder setContext(Context context) {
			this.context = context;
			return this;
		}

		public Builder setClassLoader(ModuleAPIClassLoader classLoader) {
			this.classLoader = classLoader;
			return this;
		}

		public Builder setInjector(ModuleInjector injector) {
			this.injector = injector;
			return this;
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Creates a new ModuleManager instance.
	 *
	 * @param modulesPath Path where the modules are stored
	 * @param modulesDataPath Path, where the modules data is stored
	 * @param context the context
	 * @return A new ModuleManager.
	 */
	public static ModuleManager create(final File modulesPath, final File modulesDataPath, final Context context) {
		return create(modulesPath, modulesDataPath, context, new ModuleAPIClassLoader(ModuleManagerImpl.class.getClassLoader(), Collections.EMPTY_LIST));
	}

	/**
	 * Creates a new ModuleManager instance.
	 *
	 * @param modulesPath the path where the installed modules are located.
	 * @param modulesDataPath the path where the modules data directory is
	 * located.
	 * @param context the context
	 * @param classLoader the classloader
	 * @return
	 */
	public static ModuleManager create(final File modulesPath, final File modulesDataPath, final Context context, final ModuleAPIClassLoader classLoader) {
		return create(modulesPath, modulesDataPath, context, classLoader, null);
	}

	/**
	 * Creates a new ModuleManager instance.
	 *
	 * @param modulesPath the path where the installed modules are located.
	 * @param modulesDataPath the path where the modules data directory is
	 * located.
	 * @param context the context
	 * @param injector Injector for dependency injection
	 * @return A new ModuleManager.
	 */
	public static ModuleManager create(final File modulesPath, final File modulesDataPath, final Context context, final ModuleInjector injector) {
		return create(modulesPath, modulesDataPath, context, new ModuleAPIClassLoader(ModuleManagerImpl.class.getClassLoader(), Collections.EMPTY_LIST), injector);
	}

	/**
	 * Creates a ne ModuleManager instance.
	 *
	 * @param modulesPath the path where the installed modules are located.
	 * @param modulesDataPath the path where the modules data directory is
	 * located.
	 * @param context the context
	 * @param classLoader the classloader
	 * @param injector Injector for dependency injection
	 * @return
	 */
	public static ModuleManager create(final File modulesPath, final File modulesDataPath, final Context context, final ModuleAPIClassLoader classLoader, final ModuleInjector injector) {
		return builder().setClassLoader(classLoader)
				.setModulesPath(modulesPath)
				.setModulesDataPath(modulesDataPath)
				.setContext(context).setInjector(injector).build();
	}

	final File modulesPath;
	final File modulesDataPath;

	final ModuleLoader moduleLoader;

	final ModuleAPIClassLoader globalClassLoader;

	private ManagerConfiguration configuration;

	private final Context context;

	final ModuleInjector injector;

	final ModuleRequestContextFactory requestContextFactory;

	final ModuleServiceLoader systemExtensionLoader;

	public ModuleManagerImpl() {
		this.modulesDataPath = null;
		this.modulesPath = null;
		this.globalClassLoader = null;
		this.moduleLoader = null;
		this.context = null;
		this.injector = null;
		this.requestContextFactory = null;
		this.systemExtensionLoader = null;
	}

	private ModuleManagerImpl(final Builder builder) {
		this.modulesPath = builder.modulesPath;
		this.modulesDataPath = builder.modulesDataPath;
		this.context = builder.context;
		this.injector = builder.injector;
		this.requestContextFactory = builder.requestContextFactory;

		this.configuration = new ManagerConfiguration();
		this.globalClassLoader = builder.classLoader;
		this.moduleLoader = new ModuleLoader(configuration, modulesPath, modulesDataPath, this.globalClassLoader,
				this.context, this.injector, this.requestContextFactory);

		File[] moduleFiles = modulesPath.listFiles((File file) -> file.isDirectory());
		File moduleData = modulesDataPath;

		Set<String> allUsedModuleIDs = new HashSet<>();

		Map<String, ModuleImpl> modules = new HashMap<>();
		if (moduleFiles != null) {
			loadModules(moduleFiles, moduleData, allUsedModuleIDs, modules);
		}
		configuration.getModules().values().stream().filter((mc) -> (!allUsedModuleIDs.contains(mc.getId()))).forEach((mc) -> {
			configuration.remove(mc.getId());
		});

		systemExtensionLoader = ModuleServiceLoader.create(globalClassLoader.getParent());

	}

	@Override
	public void initModules() {

		File[] moduleFiles = modulesPath.listFiles((File file) -> file.isDirectory());
		File moduleData = modulesDataPath;

		Set<String> allUsedModuleIDs = new HashSet<>();

		Map<String, ModuleImpl> modules = new HashMap<>();
		if (moduleFiles != null) {
			loadModules(moduleFiles, moduleData, allUsedModuleIDs, modules);
		}

		List<ModuleImpl> moduleList = new ArrayList<>(modules.values());
		moduleLoader.tryToLoadModules(moduleList);

		configuration.getModules().values().forEach((mc) -> {
			if (!moduleLoader.activeModules().containsKey(mc.getId())) {
				configuration.get(mc.getId()).setActive(false);
			}
		});
	}

	private void loadModules(File[] moduleFiles, File moduleData, Set<String> allUsedModuleIDs, Map<String, ModuleImpl> modules) {
		for (File module : moduleFiles) {
			try {
				ModuleImpl mod = new ModuleImpl(module, moduleData, this.context, this.injector, this.requestContextFactory
				);
				allUsedModuleIDs.add(mod.getId());
				modules.put(mod.getId(), mod);
				if (configuration.get(mod.getId()) == null) {
					configuration.add(new ManagerConfiguration.ModuleConfig(mod.getId()).setModuleDir(module.getName()));
				}
			} catch (IOException ex) {
				LOGGER.error("", ex);
				// deactivate module
				String modid = module.getName();
				allUsedModuleIDs.add(modid);
				if (configuration.get(modid) != null) {
					LOGGER.warn("deactivate module caused by an error");
					configuration.get(modid).setActive(false);
				}
			}
		}
	}

	@Override
	public void close() {
		extensions(ModuleLifeCycleExtension.class).stream().forEach((ModuleLifeCycleExtension mle) -> {
			mle.setContext(context);
			mle.deactivate();
		});
	}

	/**
	 * Returns a module by id. All the modules are loaded correctly so you can
	 * get extensions.
	 *
	 * @param id The id of the module.
	 * @return The module for the given id or null.
	 */
	@Override
	public Module module(final String id) {
		return moduleLoader.activeModules.get(id);
	}

	@Override
	public List<String> getModuleIds() {
		List<ManagerConfiguration.ModuleConfig> modules = new ArrayList<>(configuration.getModules().values());

		return modules.stream().map((mc) -> {
			try {
				File moduleDir = new File(modulesPath, configuration.get(mc.getId()).getModuleDir());
				File moduleData = modulesDataPath;
				ModuleImpl module = new ModuleImpl(moduleDir, moduleData, this.context, this.injector, this.requestContextFactory);
				return module;
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}).sorted(new ModuleComparator()).map(Module::getId).collect(Collectors.toList());
	}

	/**
	 * Returns the module description.
	 *
	 * @param id
	 * @return
	 * @throws IOException
	 */
	@Override
	public ModuleDescription description(final String id) throws IOException {
		ModuleImpl module;
		if (moduleLoader.activeModules.containsKey(id)) {
			module = moduleLoader.activeModules.get(id);
		} else {
			ManagerConfiguration.ModuleConfig mc = configuration.get(id);
			File moduleDir = new File(modulesPath, configuration.get(mc.getId()).getModuleDir());
			module = new ModuleImpl(moduleDir, null, this.context, this.injector, this.requestContextFactory);
		}

		ModuleDescription description = new ModuleDescription();
		description.setVersion(module.getVersion());
		description.setName(module.getName());
		description.setDescription(module.getDescription());
		return description;
	}

	/**
	 * activates a module.
	 *
	 * @param moduleId
	 * @return returns true if the module is correctly or allready installed,
	 * otherwise false
	 * @throws java.io.IOException
	 */
	@Override
	public boolean activateModule(final String moduleId) throws IOException {
		if (configuration.get(moduleId) == null || !configuration.get(moduleId).isActive()) {
			return moduleLoader.activateModule(moduleId);
		} else if (configuration.get(moduleId) != null && configuration.get(moduleId).isActive()) {
			return true;
		}
		return false;

	}

	/**
	 *
	 * @param moduleId
	 * @return
	 */
	@Override
	public boolean deactivateModule(final String moduleId) throws IOException {
		if (configuration.get(moduleId) == null) {
			return true;
		} else if (configuration.get(moduleId) != null && !configuration.get(moduleId).isActive()) {
			return true;
		}

		moduleLoader.deactivateModule(moduleId);

		configuration.get(moduleId).setActive(false);
		return true;

	}

	/**
	 * Returns all Extensions of the given type.
	 *
	 * @param <T>
	 * @param extensionClass
	 * @return
	 */
	@Override
	public <T extends ExtensionPoint> List<T> extensions(Class<T> extensionClass) {
		List<T> extensions = new ArrayList<>();
		moduleLoader.activeModules().values().forEach((ModuleImpl m) -> {
			var moduleExt = m.extensions(extensionClass);
			if (!moduleExt.isEmpty()) {
				extensions.addAll(moduleExt);
			}
		});
		// system modules
		systemExtensionLoader.get(extensionClass)
				.stream()
				.map(ext -> {
					ext.setContext(context);
					
					if (requestContextFactory != null) {
						ext.setRequestContext(requestContextFactory.createContext());
					}

					if (injector != null) {
						injector.inject(ext);
					}

					ext.init();

					return ext;
				})
				.forEach(extensions::add);

		return extensions;
	}

	/**
	 * Returns the configuration of the module manager.
	 *
	 * @return
	 */
	@Override
	public ManagerConfiguration configuration() {
		return configuration;
	}
}
