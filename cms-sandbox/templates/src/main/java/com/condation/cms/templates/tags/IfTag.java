package com.condation.cms.templates.tags;

import com.condation.cms.templates.Tag;
import java.util.Optional;

/**
 *
 * @author t.marx
 */
public class IfTag implements Tag {

	@Override
	public String getTagName() {
		return "if";
	}

	@Override
	public Optional<String> getCloseTagName() {
		return Optional.of("endif");
	}
	
	@Override
    public boolean supportsNestedTag(String tagName) {
        return tagName.equals("elseif") || tagName.equals("else");
    }
}
