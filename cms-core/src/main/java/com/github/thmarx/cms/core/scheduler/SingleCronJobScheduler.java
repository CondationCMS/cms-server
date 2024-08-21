package com.github.thmarx.cms.core.scheduler;

/*-
 * #%L
 * cms-core
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

import com.github.thmarx.cms.api.scheduler.CronJob;
import com.github.thmarx.cms.api.scheduler.CronJobContext;
import com.github.thmarx.cms.api.scheduler.CronJobScheduler;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;

/**
 *
 * @author t.marx
 */
@Slf4j
public class SingleCronJobScheduler extends AbstractCronJobScheduler implements CronJobScheduler {

	public SingleCronJobScheduler(Scheduler scheduler, CronJobContext context) {
		super(scheduler, context);
	}
	
	@Override
	public void schedule(String cronExpression, String name, CronJob job) {
		super.schedule(cronExpression, name, job, SingleCronJobRunner.class);
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
