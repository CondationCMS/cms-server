package com.condation.cms.templates;

import java.util.Map;

/**
 *
 * @author thmar
 */
public interface Template {
	
	default String execute () {
		return execute(Map.of());
	}
	
	String execute(Map<String, Object> context);
}
