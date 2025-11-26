package com.condation.cms.api.mail;

import java.util.List;

/**
 *
 * @author thmar
 */
public record Message(String from, List<String> to, String subject, String message) {

	public Message (String from, String to, String subject, String message) {
		this(from, List.of(to), subject, message);
	}
}
