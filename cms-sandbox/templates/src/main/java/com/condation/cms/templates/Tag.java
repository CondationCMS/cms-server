package com.condation.cms.templates;

import java.util.Optional;

/**
 *
 * @author t.marx
 */
public interface Tag {

	String getTagName();
	
	default Optional<String> getCloseTagName () {
		return Optional.empty();
	}
	
	default boolean supportsNestedTag(String tagName) {
        return false;
    }
	
	default boolean isEndTag () {
		return false;
	}
}
