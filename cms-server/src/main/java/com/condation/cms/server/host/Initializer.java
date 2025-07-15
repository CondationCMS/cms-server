package com.condation.cms.server.host;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 - 2025 CondationCMS
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
import com.condation.cms.api.db.DB;
import com.condation.cms.core.backup.BackupUtil;
import com.condation.cms.core.scheduler.SiteCronJobScheduler;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author thorstenmarx
 */
@Slf4j
@RequiredArgsConstructor
public class Initializer {

	private final VHost host;
	
	
	public void initBackup () throws IOException {
		var siteProperties = host.getInjector().getInstance(SiteProperties.class);
		
		if (!siteProperties.backupEnabled()) {
			log.info("backup disabled");
			return;
		}
		
		log.info("init backup");
		var backup = siteProperties.getOrDefault("backup", Collections.emptyMap());
		if (!backup.containsKey("cron")) {
			log.error("backup skipped: cron expression required");
			return;
		}
		if (!backup.containsKey("target")) {
			log.error("backup skipped: target folder required");
			return;
		}
		
		String cron = (String)backup.get("cron");
		String target = (String)backup.get("target");
		
		var scheduler = host.getInjector().getInstance(SiteCronJobScheduler.class);
		
		var targetPath = Path.of(target);
		Files.createDirectories(targetPath);
		
		scheduler.schedule(
				cron, 
				"backup-job-%s".formatted(siteProperties.id()), 
				(context) -> {
					try {
						var timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
						log.debug("start backup at {}", timestamp);
						var sitePath = host.getInjector().getInstance(DB.class).getFileSystem().hostBase();					
						var backupFilename = "%s-%s.tar.gz".formatted(siteProperties.id(), timestamp);
						
						BackupUtil.createTarGzBackup(sitePath, targetPath.resolve(backupFilename));
					} catch (Exception e) {
						log.error("error creating backup", e);
					}
				}
		);
	}
}
