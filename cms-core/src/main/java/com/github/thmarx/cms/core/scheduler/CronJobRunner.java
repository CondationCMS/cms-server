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
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 *
 * @author t.marx
 */
public class CronJobRunner implements Job {

	public static final String DATA_CRONJOB = "cronjob";
	public static final String DATA_CONTEXT = "context";
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		if (context.get(DATA_CRONJOB) != null) {
			CronJobContext jobContext = (CronJobContext) context.get(DATA_CONTEXT);
			((CronJob)context.get(DATA_CRONJOB)).accept(null);
		}
	}
	
}
