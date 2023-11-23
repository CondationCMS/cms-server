package com.github.thmarx.cms.git;

import com.github.thmarx.cms.git.tasks.CloneTask;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;

/**
 *
 * @author t.marx
 */
@Slf4j
public class RepositoryManager {

	Config config;
	TaskRunner taskRunner;

	GitScheduler scheduler;

	public void init(final Path configFile) throws IOException {
		config = Config.load(configFile);
		taskRunner = new TaskRunner();
		scheduler = new GitScheduler(taskRunner);

		if (config.getRepos() != null) {
			log.debug("initial clone repositories");
			for (var repo : config.getRepos()) {
				log.debug("clone {}", repo.getName());
				var result = taskRunner.execute(new CloneTask(repo));
				try {
					log.debug("result : {} ", result.get());
				} catch (InterruptedException | ExecutionException ex) {
					log.error("error cloneing repository", ex);
				}
			}
		}
	}

	public void close() throws IOException {
		try {
			scheduler.close();
			taskRunner.executor.shutdown();
		} catch (SchedulerException ex) {
			log.error(null, ex);
			throw new IOException(ex);
		}
	}

}
