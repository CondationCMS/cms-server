package com.condation.cms.templates.loaders;

import com.condation.cms.templates.TemplateLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author thmar
 */
public class StringTemplateLoader implements TemplateLoader {
	
	private ConcurrentMap<String, String> templates = new ConcurrentHashMap<>();

	public StringTemplateLoader add (String template, String content) {
		templates.put(template, content);
		
		return this;
	}
	
	@Override
	public String load(String template) {
		return templates.get(template);
	}
	
	
}
