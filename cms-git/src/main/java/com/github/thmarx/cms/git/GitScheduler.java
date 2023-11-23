package com.github.thmarx.cms.git;

/*-
 * #%L
 * cms-git
 * %%
 * Copyright (C) 2023 Marx-Software
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

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

/**
 *
 * @author t.marx
 */
public class GitScheduler {

	Scheduler scheduler;
	TaskRunner taskRunner = new TaskRunner();

	public GitScheduler() {

	}

	public void open() throws SchedulerException {
		SchedulerFactory schedulerFactory = new StdSchedulerFactory();
		scheduler = schedulerFactory.getScheduler();
		scheduler.start();
	}

	public void close() throws SchedulerException {
		scheduler.shutdown();
		taskRunner.executor.shutdown();
	}

	public void schedule(final Repo repo) throws SchedulerException {
		JobDataMap data = new JobDataMap();
		data.put("repo", repo);
		data.put("taskRunner", taskRunner);
		JobDetail jobDetail = JobBuilder
				.newJob(UpdateRepoJob.class)
				.withIdentity(repo.getName(), "update-repo")
				.usingJobData(data)
				.build();
		
		CronTrigger trigger = TriggerBuilder.newTrigger()
				.withIdentity(repo.getName(), "update-repo")
				.withSchedule(CronScheduleBuilder.cronSchedule(repo.getCron()))
				.startNow()
				.forJob(jobDetail)
				.build();
		
		scheduler.scheduleJob(jobDetail, trigger);
	}
}
