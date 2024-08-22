package com.github.thmarx.cms.server.configs;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 - 2024 Marx-Software
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
import com.github.thmarx.cms.api.PropertiesLoader;
import com.github.thmarx.cms.api.ServerProperties;
import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import java.io.IOException;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Engine;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

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
			SchedulerFactory schedulerFactory = new StdSchedulerFactory();
			var scheduler = schedulerFactory.getScheduler();
			scheduler.start();

			return scheduler;
		} catch (SchedulerException ex) {
			log.error(null, ex);
			throw new RuntimeException(ex);
		}
	}

	@Provides
	public ServerProperties serverProperties() throws IOException {
		return PropertiesLoader.serverProperties(Path.of("server.yaml"));
	}
	
	@Provides
	public Engine engine() throws IOException {
		return Engine.newBuilder("js")
					.option("engine.WarnInterpreterOnly", "false")
					.build();
	}
}
