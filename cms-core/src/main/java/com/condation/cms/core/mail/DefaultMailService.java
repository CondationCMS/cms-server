package com.condation.cms.core.mail;

import com.condation.cms.api.db.DB;
import com.condation.cms.api.mail.MailService;
import com.condation.cms.api.mail.Message;

/**
 *
 * @author thmar
 */
public class DefaultMailService implements MailService {

	private final DB db;

	public DefaultMailService(DB db) {
		this.db = db;
		
		init();
	}
	
	private void init () {
		db.getFileSystem().resolve("config/mail.yaml");
	}
	
	@Override
	public void sendText(Message message) {
	}

	@Override
	public void sendHtml(Message message) {
	}
	
}
