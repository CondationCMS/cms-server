package com.condation.cms.api.mail;

/**
 *
 * @author thmar
 */
public interface MailService {
	void sendText (Message message);
	
	void sendHtml (Message message);
}
