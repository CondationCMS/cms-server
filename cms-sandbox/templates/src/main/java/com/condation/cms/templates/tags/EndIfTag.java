package com.condation.cms.templates.tags;

import com.condation.cms.templates.Tag;

/**
 *
 * @author t.marx
 */
public class EndIfTag implements Tag {

	@Override
	public String getTagName() {
		return "endif";
	}

	@Override
	public boolean isEndTag() {
		return true;
	}
}
