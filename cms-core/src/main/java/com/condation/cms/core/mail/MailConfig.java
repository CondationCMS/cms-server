package com.condation.cms.core.mail;

/*-
 * #%L
 * cms-core
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
import com.condation.cms.core.configuration.EnvironmentVariables;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Data;

/**
 *
 * @author thmar
 */
@Data
public class MailConfig {

	private List<Account> accounts;

	public Optional<Account> getAccount(String name) {
		if (accounts == null || accounts.isEmpty()) {
			return Optional.empty();
		}
		return accounts.stream().filter(acc -> name.equals(acc.getName())).findFirst();
	}

	@Data
	public static class Account {

		private String name;
		private String fromMail;
		private String host;
		private int port;
		private String username;
		private String password;

		public static Account fromMap(Map.Entry<?, ?> entry, EnvironmentVariables env) {
			Account acc = new Account();
			acc.setName(entry.getKey().toString());
			Map<?, ?> values = (Map<?, ?>) entry.getValue();
			acc.setFromMail(resolve((String) values.get("fromMail"), env));
			acc.setHost(resolve((String) values.get("host"), env));
			acc.setUsername(resolve((String) values.get("username"), env));
			acc.setPassword(resolve((String) values.get("password"), env));
			var port = values.get("port");
			if (port instanceof int intValue) {
				acc.setPort(intValue);
			} else if (port instanceof String stringValue) {
				acc.setPort(Integer.parseInt(resolve(stringValue, env)));
			}
			return acc;
		}
	}

	public static MailConfig fromMap(Map<?, ?> rawConfig, EnvironmentVariables env) {
		Map<?, ?> accountsRaw = (Map<?, ?>) rawConfig.get("accounts");

		List<Account> accounts = accountsRaw.entrySet().stream()
				.map(entry -> Account.fromMap(entry, env))
				.toList();

		MailConfig config = new MailConfig();
		config.setAccounts(accounts);
		return config;
	}

	private static String resolve(String value, EnvironmentVariables env) {
		return env.resolveEnvVars(value);
	}

}
