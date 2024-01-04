package com.github.thmarx.cms.modules.forms;

/*-
 * #%L
 * forms-module
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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.thmarx.cms.api.feature.features.DBFeature;
import com.github.thmarx.cms.api.module.CMSModuleContext;
import com.github.thmarx.modules.api.ModuleLifeCycleExtension;
import com.github.thmarx.modules.api.annotation.Extension;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.mailer.MailerBuilder;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author t.marx
 */
@Slf4j
@Extension(ModuleLifeCycleExtension.class)
public class FormsLifecycleExtension extends ModuleLifeCycleExtension<CMSModuleContext> {

	public static Cache<String, String> CAPTCHAS;
	public static FormsConfig FORMSCONFIG;
	public static Mailer MAILER;

	@Override
	public void init() {

	}

	@Override
	public void activate() {
		CAPTCHAS = Caffeine.newBuilder()
				.maximumSize(10_000)
				.expireAfterWrite(Duration.ofMinutes(5))
				.build();
		
		Path formsConfig = getContext().get(DBFeature.class).db().getFileSystem().resolve("config/forms.yaml");
		try {
			FORMSCONFIG = new Yaml().loadAs(Files.readString(formsConfig, StandardCharsets.UTF_8), FormsConfig.class);
			
			MAILER = MailerBuilder.withSMTPServer(FORMSCONFIG.getMail().getSmtp().getHostname(), FORMSCONFIG.getMail().getSmtp().getPort()).buildMailer();
			
		} catch (IOException ex) {
			log.error(null, ex);
		}
	}

}
