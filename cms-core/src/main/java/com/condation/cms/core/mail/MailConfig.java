package com.condation.cms.core.mail;

import lombok.Data;

/**
 *
 * @author thmar
 */
@Data
public class MailConfig {
	
	private String host;
	
	private int port;
	
	private String username;
	
	private String password;
}
