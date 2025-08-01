package com.condation.cms.modules.ui.utils;

/*-
 * #%L
 * ui-module
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

import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.mailer.MailerBuilder;
import org.simplejavamail.api.mailer.config.TransportStrategy;

public class MailerProvider {

	public static Mailer provide(String siteId) {
		var key = normalizeHostId(siteId);
		String host = getRequiredEnv("CMS_UI_SMTP_HOST_%s".formatted(key));
		int port = Integer.parseInt(getRequiredEnv("CMS_UI_SMTP_PORT_%s".formatted(key)));
		String username = getRequiredEnv("CMS_UI_SMTP_USER_%s".formatted(key));
		String password = getRequiredEnv("CMS_UI_SMTP_PASS_%s".formatted(key));

		String transport = System.getenv().getOrDefault("CMS_UI_SMTP_TRANSPORT_%s".formatted(key), "SMTP_TLS");

		TransportStrategy strategy = switch (transport.toUpperCase()) {
			case "SMTP_TLS" ->
				TransportStrategy.SMTP_TLS;
			case "SMTPS" ->
				TransportStrategy.SMTPS;
			case "SMTP_PLAIN" ->
				TransportStrategy.SMTP;
			default ->
				throw new IllegalArgumentException("Unsupported SMTP_TRANSPORT: " + transport);
		};

		return MailerBuilder
				.withSMTPServer(host, port, username, password)
				.withTransportStrategy(strategy)
				.buildMailer();
	}

	private static String getRequiredEnv(String name) {
		String value = System.getenv(name);
		if (value == null || value.isEmpty()) {
			throw new IllegalStateException("Missing required environment variable: " + name);
		}
		return value;
	}

	private static String normalizeHostId(String hostId) {
		return hostId.toUpperCase().replaceAll("[^A-Z0-9]", "_");
	}
}
