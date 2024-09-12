package com.condation.cms.git;

/*-
 * #%L
 * cms-git
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


import com.condation.cms.git.tasks.FetchTask;
import com.condation.cms.git.tasks.MergeTask;
import com.condation.cms.git.tasks.ResetTask;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 *
 * @author t.marx
 */
@Slf4j
public class UpdateRepoJob implements Job {
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		Repo repo = (Repo) context.getJobDetail().getJobDataMap().get("repo");
		TaskRunner taskRunner = (TaskRunner) context.getJobDetail().getJobDataMap().get("taskRunner");
		
		try {
			var fetch = taskRunner.execute(new FetchTask(repo));
			
			if (fetch.get()) {
				log.debug("fetch {} done", repo.getName());
				var merge = taskRunner.execute(new MergeTask(repo));
				if (merge.get()) {
					log.debug("{} merged", repo.getName());
				} else {
					log.error("merge {} error", repo.getName());
					var reset = taskRunner.execute(new ResetTask(repo));
					log.debug("reset {}", repo.getName());
				}
			} else {
				log.error("fetch {} error", repo.getName());
			}
		} catch (Exception e) {
			log.error(null, e);
		}
	}
}
