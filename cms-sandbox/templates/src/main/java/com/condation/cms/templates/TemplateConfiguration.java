package com.condation.cms.templates;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author t.marx
 */
public class TemplateConfiguration {

	private final Map<String, Tag> registeredTags = new HashMap<>();
	
	@Getter
	@Setter
	private TemplateLoader templateLoader;
	
	public TemplateConfiguration registerTag (Tag tag) {
		registeredTags.put(tag.getTagName(), tag);
		
		return this;
	}
	
	public boolean hasTag (String tagName) {
		return registeredTags.containsKey(tagName);
	}
	
	public Optional<Tag> getTag (String tagName) {
		return Optional.ofNullable(registeredTags.get(tagName));
	}
}
