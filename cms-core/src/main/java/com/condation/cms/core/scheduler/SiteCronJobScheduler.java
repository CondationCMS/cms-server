package com.condation.cms.core.scheduler;

/*-
 * #%L
 * cms-core
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


import com.condation.cms.api.SiteProperties;
import com.condation.cms.api.scheduler.CronJob;
import com.condation.cms.api.scheduler.CronJobContext;
import com.condation.cms.api.scheduler.CronJobScheduler;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;

/**
 *
 * @author t.marx
 */
@Slf4j
public class SiteCronJobScheduler extends AbstractCronJobScheduler implements CronJobScheduler {

	public SiteCronJobScheduler(Scheduler scheduler, CronJobContext context, SiteProperties siteProperties) {
		super(scheduler, context, siteProperties);
	}
	
	@Override
	public void schedule(String cronExpression, String name, CronJob job) {
		super.schedule(cronExpression, name, job, DefaultCronJobRunner.class);
	}

	@Override
	public void remove(String name) {
		super.remove(name);
	}
	
	@Override
	public boolean exists(String name) {
		return super.exists(name);
	}
	
}
