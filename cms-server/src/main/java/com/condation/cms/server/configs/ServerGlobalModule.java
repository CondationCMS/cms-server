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
import com.condation.cms.core.configuration.ConfigurationFactory;
import com.condation.cms.core.configuration.properties.ExtendedServerProperties;
import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import java.io.IOException;
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
}
