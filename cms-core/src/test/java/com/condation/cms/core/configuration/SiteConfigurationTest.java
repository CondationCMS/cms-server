package com.condation.cms.core.configuration;

/*-
 * #%L
 * tests
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

import com.condation.cms.core.configuration.configs.SimpleConfiguration;
import com.condation.cms.core.configuration.source.TomlConfigSource;
import com.condation.cms.core.configuration.source.YamlConfigSource;
import com.condation.cms.api.SiteProperties;
import com.condation.cms.api.eventbus.EventBus;
import com.condation.cms.api.scheduler.CronJobContext;
import com.condation.cms.api.scheduler.CronJobScheduler;
import com.condation.cms.core.configuration.reload.CronReload;
import com.condation.cms.core.configuration.properties.ExtendedSiteProperties;
import com.condation.cms.core.scheduler.SingleCronJobScheduler;
import java.io.IOException;
import java.nio.file.Path;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

/**
 *
 * @author t.marx
 */
@ExtendWith(MockitoExtension.class)
public class SiteConfigurationTest {

	private SimpleConfiguration siteConfig;
	private ExtendedSiteProperties siteProperties;
	
	@BeforeEach
	public void setup() throws IOException, SchedulerException {
		siteConfig = ConfigurationFactory.siteConfiguration("dev", Path.of("configs"));
		
		siteProperties = new ExtendedSiteProperties(siteConfig);
	}
	
	@AfterEach
	public void shutdown () throws SchedulerException {
	}

	@Test
	public void test_hostname() {
		var hostnames = siteProperties.hostnames();
		
		Assertions.assertThat(hostnames).contains("localhost", "condation.com");
	}

	
}
