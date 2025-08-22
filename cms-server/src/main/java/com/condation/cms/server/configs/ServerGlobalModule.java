package com.condation.cms.server.configs;

/*-
 * #%L
 * cms-server
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
import com.condation.cms.api.ServerProperties;
import com.condation.cms.api.feature.features.ModuleManagerFeature;
import com.condation.cms.api.module.ServerModuleContext;
import com.condation.cms.api.module.SiteModuleContext;
import com.condation.cms.api.site.SiteService;
import com.condation.cms.api.utils.ServerUtil;
import com.condation.cms.auth.services.UserService;
import com.condation.cms.core.configuration.ConfigurationFactory;
import com.condation.cms.core.configuration.properties.ExtendedServerProperties;
import com.condation.cms.core.site.DefaultSiteService;
import com.condation.cms.filesystem.FileDB;
import com.condation.cms.git.RepositoryManager;
import com.condation.modules.api.ModuleManager;
import com.condation.modules.api.ModuleRequestContextFactory;
import com.condation.modules.manager.ModuleAPIClassLoader;
import com.condation.modules.manager.ModuleManagerImpl;
import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Engine;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.DirectSchedulerFactory;
import org.quartz.simpl.RAMJobStore;
import org.quartz.simpl.SimpleThreadPool;

/**
 *
 * @author t.marx
 */
@Slf4j
public class ServerGlobalModule implements com.google.inject.Module {

	@Override
	public void configure(Binder binder) {

	}

	@Provides
	@Singleton
	public Scheduler scheduler() {
		try {

			DirectSchedulerFactory schedulerFactory = DirectSchedulerFactory.getInstance();
			schedulerFactory.createScheduler(
					"cms-scheduler",
					"cms-scheduler",
					new SimpleThreadPool(5, Thread.NORM_PRIORITY),
					new RAMJobStore());
			var scheduler = schedulerFactory.getScheduler("cms-scheduler");
			scheduler.start();

			return scheduler;
		} catch (SchedulerException ex) {
			log.error(null, ex);
			throw new RuntimeException(ex);
		}
	}

	@Provides
	public ServerProperties serverProperties() throws IOException {
		return new ExtendedServerProperties(ConfigurationFactory.serverConfiguration());
	}

	@Provides
	public Engine engine() throws IOException {
		return Engine.newBuilder("js")
				.option("engine.WarnInterpreterOnly", "false")
				.build();
	}

	@Provides
	@Singleton
	public UserService userService() {
		return new UserService(ServerUtil.getHome());
	}

	@Provides
	@Singleton
	public RepositoryManager repositoryManager(Scheduler scheduler) throws Exception {
		Path gitConfig = ServerUtil.getPath("git.yaml");

		log.info("repository configuration found");
		final RepositoryManager repositoryManager = new RepositoryManager(scheduler);
		repositoryManager.init(gitConfig);

		return repositoryManager;
	}

	@Provides
	@Singleton
	public SiteService siteService() {
		return new DefaultSiteService();
	}
	
	@Provides
	@Singleton
	public ServerModuleContext serverModuleContext () {
		return new ServerModuleContext();
	}
	
	@Provides
	@Singleton
	@Named("server")
	public ModuleManager serverModuleManager(Injector injector, ServerModuleContext context) {
		var classLoader = new ModuleAPIClassLoader(ClassLoader.getSystemClassLoader(),
				List.of(
						"org.slf4j",
						"com.condation.cms",
						"com.condation.modules",
						"org.apache.logging",
						"org.graalvm.polyglot",
						"org.graalvm.js",
						"org.eclipse.jetty",
						"jakarta.servlet",
						"com.google",
						"org.w3c"
				));
		
		var homePath = ServerUtil.getHome();
		var moduleManager = ModuleManagerImpl.builder()
				.setClassLoader(classLoader)
				.setInjector((instance) -> injector.injectMembers(instance))
				.setModulesDataPath(homePath.resolve("modules_data").toFile())
				.setModulesPath(homePath.toFile())
				.setContext(context)
				.build();

		context.add(ModuleManagerFeature.class, new ModuleManagerFeature(moduleManager));

		return moduleManager;
	}
}
