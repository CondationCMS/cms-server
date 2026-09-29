package com.condation.cms.server.configs;

/*-
 * #%L
 * CMS Server
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
import com.condation.cms.api.Constants;
import com.condation.cms.api.ServerProperties;
import com.condation.cms.api.eventbus.EventBus;
import com.condation.cms.api.feature.features.InjectorFeature;
import com.condation.cms.api.feature.features.ModuleManagerFeature;
import com.condation.cms.api.feature.features.ServerHookSystemFeature;
import com.condation.cms.api.hooks.HookSystem;
import com.condation.cms.api.injector.Injector;
import com.condation.cms.api.messaging.Messaging;
import com.condation.cms.api.module.ServerModuleContext;
import com.condation.cms.api.scheduler.CronJobScheduler;
import com.condation.cms.api.site.SiteService;
import com.condation.cms.api.utils.ServerUtil;
import com.condation.cms.auth.services.UserService;
import com.condation.cms.auth.services.RoleService;
import com.condation.cms.core.configuration.ConfigurationFactory;
import com.condation.cms.core.configuration.properties.ExtendedServerProperties;
import com.condation.cms.core.eventbus.MessagingEventBus;
import com.condation.cms.core.messaging.DefaultMessaging;
import com.condation.cms.core.scheduler.ServerCronJobScheduler;
import com.condation.cms.core.site.DefaultSiteService;
import com.condation.cms.hooksystem.CMSHookSystem;
import com.condation.modules.api.ModuleManager;
import com.condation.modules.manager.ModuleAPIClassLoader;
import com.condation.modules.manager.ModuleManagerImpl;
import static com.condation.cms.server.configs.ProviderSupport.provide;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.jmx.JmxConfig;
import io.micrometer.jmx.JmxMeterRegistry;
import java.io.IOException;
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
public class ServerGlobalModule implements com.condation.cms.api.injector.Module {

    @Override
    public void register(Injector injector) {
        injector.register(MeterRegistry.class, _ -> metricRegisry()).singleton();
        injector.register(Scheduler.class, _ -> scheduler()).singleton();
        injector.register(Constants.DiScopes.SERVER, Messaging.class, _ -> serverMessaging()).singleton();
        injector.register(Constants.DiScopes.SERVER, EventBus.class, this::serverEventBus).singleton();
        injector.register(Constants.DiScopes.SERVER, CronJobScheduler.class,
                i -> serverCronJobScheudler(i.getInstance(Scheduler.class))).singleton();
        injector.register(ServerProperties.class, _ -> provide(this::serverProperties));
        injector.register(Engine.class, _ -> provide(this::engine)).singleton();
        injector.register(UserService.class, _ -> userService()).singleton();
        injector.register(RoleService.class, _ -> roleService()).singleton();
        injector.register(SiteService.class, _ -> siteService()).singleton();
        injector.register(Constants.DiScopes.SERVER, HookSystem.class, _ -> hookSystem()).singleton();
        injector.register(ServerModuleContext.class,
                i -> serverModuleContext(i, i.getInstance(Constants.DiScopes.SERVER, HookSystem.class))).singleton();
        injector.register(Constants.DiScopes.SERVER, ModuleManager.class,
                i -> serverModuleManager( i.getInstance(ServerModuleContext.class))).singleton();
    }


    public MeterRegistry metricRegisry() {
        MeterRegistry registry = new JmxMeterRegistry(
                JmxConfig.DEFAULT,
                Clock.SYSTEM
        );

        new ClassLoaderMetrics().bindTo(registry);
        new JvmMemoryMetrics().bindTo(registry);
        new JvmGcMetrics().bindTo(registry);
        new JvmThreadMetrics().bindTo(registry);
        new ProcessorMetrics().bindTo(registry);

        return registry;
    }

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

    public Messaging serverMessaging() {
        return new DefaultMessaging("server");
    }

    public EventBus serverEventBus(Injector injector) {
        return new MessagingEventBus(injector.getInstance(Constants.DiScopes.SERVER, Messaging.class));
    }

    public CronJobScheduler serverCronJobScheudler(Scheduler scheduler) {
        return new ServerCronJobScheduler(scheduler);
    }

    public ServerProperties serverProperties() throws IOException {
        return new ExtendedServerProperties(ConfigurationFactory.serverConfiguration());
    }

    public Engine engine() throws IOException {
        return Engine.newBuilder("js")
                .option("engine.WarnInterpreterOnly", "false")
                .build();
    }

    public UserService userService() {
        return new UserService(ServerUtil.getHome());
    }

	public RoleService roleService() {
		return new RoleService(ServerUtil.getHome());
	}

    public SiteService siteService() {
        return new DefaultSiteService();
    }

    public HookSystem hookSystem() {
        return new CMSHookSystem();
    }

    public ServerModuleContext serverModuleContext(Injector injector, HookSystem hookSystem) {
        var context = new ServerModuleContext();

        context.add(InjectorFeature.class, new InjectorFeature(injector));
        context.add(ServerHookSystemFeature.class, new ServerHookSystemFeature(hookSystem));

        return context;
    }

    public ModuleManager serverModuleManager(ServerModuleContext context) {
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
                .setModulesDataPath(homePath.resolve("modules_data").toFile())
                .setModulesPath(homePath.resolve("modules").toFile())
                .setContext(context)
                .build();

        context.add(ModuleManagerFeature.class, new ModuleManagerFeature(moduleManager));

        return moduleManager;
    }


}
