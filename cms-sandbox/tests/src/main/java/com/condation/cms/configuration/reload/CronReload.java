package com.condation.cms.configuration.reload;

import com.condation.cms.api.scheduler.CronJobScheduler;
import com.condation.cms.configuration.IConfiguration;
import com.condation.cms.configuration.ReloadStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
@RequiredArgsConstructor
public class CronReload implements ReloadStrategy {

	private final String cronExpression;
	private final CronJobScheduler scheduler;
	
	@Override
	public void register(IConfiguration configuration) {
		scheduler.schedule(cronExpression, configuration.id(), (context) -> {
			log.trace("reload of config %s triggered", configuration.id());
			System.out.println("reload");
			configuration.reload();
		});
	}	
}
