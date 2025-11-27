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

import com.condation.cms.api.db.DB;
import com.condation.cms.api.mail.MailService;
import com.condation.cms.api.mail.Message;
import java.nio.file.Files;
import org.simplejavamail.api.email.Recipient;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;

/**
 *
 * @author thmar
 */
public class DefaultMailService implements MailService {

	private final DB db;

	private MailConfig config;
	
	public DefaultMailService(DB db) {
		this.db = db;
		
		init();
	}
	
	private void init () {
		var mailConfig = db.getFileSystem().resolve("config/mail.yaml");
		if (Files.exists(mailConfig)) {
			config = MailConfigLoader.load(mailConfig);
		}
	}
	
	@Override
	public void sendText(String account, Message message) {
		var acc = getAccount(account);
		
		var mail = EmailBuilder.startingBlank()
				.appendText(message.message())
				.withSubject(message.subject())
				.from(message.from(), acc.getFromMail())
				.to(message.to().stream().map(rec -> new Recipient(rec.name(), rec.mailAddress(), null)).toList());
		
		buildMailer(acc).sendMail(mail.buildEmail());
	}

	@Override
	public void sendHtml(String account, Message message) {
		var acc = getAccount(account);
		
		var mail = EmailBuilder.startingBlank()
				.appendTextHTML(message.message())
				.withSubject(message.subject())
				.from(message.from(), acc.getFromMail())
				.to(message.to().stream().map(rec -> new Recipient(rec.name(), rec.mailAddress(), null)).toList());
		
		buildMailer(acc).sendMail(mail.buildEmail());
	}
	
	private MailConfig.Account getAccount(String account) throws RuntimeException {
		var acc = config.getAccount(account);
		if (acc.isEmpty()) {
			throw new RuntimeException("unknown account");
		}
		return acc.get();
	}
	
	
	private Mailer buildMailer (MailConfig.Account account) {
		return MailerBuilder.withSMTPServer(
				account.getHost(), 
				account.getPort(), 
				account.getUsername(), 
				account.getPassword()
		).buildMailer();
	}
}
