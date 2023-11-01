package com.github.thmarx.cms.modules.search;

/**
 *
 * @author thmar
 */
public enum SearchField {
	TAGS("tags"),;

	private final String fieldName;

	private SearchField(final String name) {
		this.fieldName = name;
	}

	public String getFieldName() {
		return this.fieldName;
	}
}
