package com.condation.cms.templates;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author t.marx
 */
public class TemplateConfiguration {

	private final Map<String, Tag> registeredTags = new HashMap<>();
	
	public void registerTag (Tag tag) {
		registeredTags.put(tag.getTagName(), tag);
	}
	
	public boolean hasTag (String tagName) {
		return registeredTags.containsKey(tagName);
	}
	
	public Optional<Tag> getTag (String tagName) {
		return Optional.ofNullable(registeredTags.get(tagName));
	}
}
