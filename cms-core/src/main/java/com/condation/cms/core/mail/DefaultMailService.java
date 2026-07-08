package com.condation.cms.core.mail;

/*-
 * #%L
 * CMS Core
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.condation.cms.api.db.DB;
import com.condation.cms.api.mail.MailService;
import com.condation.cms.api.mail.Message;
import com.condation.cms.core.configuration.EnvironmentVariables;
import jakarta.mail.Message.RecipientType;
import java.nio.file.Files;
import java.util.List;
import org.simplejavamail.api.email.Recipient;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;
import org.simplejavamail.recipient.RecipientBuilder;

/**
 *
 * @author thmar
 */
public class DefaultMailService implements MailService {

	private final DB db;

	private MailConfig config;

	private final EnvironmentVariables ENV = new EnvironmentVariables();

	public DefaultMailService(DB db) {
		this.db = db;

		init();
	}

	private void init() {
		var mailConfig = db.getFileSystem().resolve("config/mail.yaml");
		if (Files.exists(mailConfig)) {
			config = MailConfigLoader.load(mailConfig, ENV);
		}
	}

	@Override
	public void sendText(String account, Message message) {
		var acc = getAccount(account);

		var mail = EmailBuilder.startingBlank()
				.withPlainText(message.message())
				.withSubject(message.subject())
				.from(message.from(), acc.getFromMail())
				.withRecipients(toRecipients(message));

		buildMailer(acc).sendMail(mail.buildEmail());
	}

	@Override
	public void sendHtml(String account, Message message) {
		var acc = getAccount(account);

		var mail = EmailBuilder.startingBlank()
				.withHTMLText(message.message())
				.withSubject(message.subject())
				.from(message.from(), acc.getFromMail())
				.withRecipients(toRecipients(message));

		buildMailer(acc).sendMail(mail.buildEmail());
	}

	private List<Recipient> toRecipients(Message message) {
		return message.to().stream()
				.map(rec -> new RecipientBuilder()
						.withName(rec.name())
						.withAddress(rec.mailAddress())
						.withType(RecipientType.TO)
						.build())
				.toList();
	}

	private MailConfig.Account getAccount(String account) {
		if (config == null) {
			throw new RuntimeException("mail config not found");
		}

		var acc = config.getAccount(account);
		if (acc.isEmpty()) {
			throw new RuntimeException("unknown mail account: " + account);
		}
		return acc.get();
	}

	private Mailer buildMailer(MailConfig.Account account) {
		return MailerBuilder.withSMTPServer(
				account.getHost(),
				account.getPort(),
				account.getUsername(),
				account.getPassword()
		).buildMailer();
	}
}