package com.condation.cms.api;

/*-
 * #%L
 * cms-api
 * %%
 * Copyright (C) 2023 - 2024 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */


import java.util.List;
import java.util.Locale;

/**
 *
 * @author t.marx
 */
public interface SiteProperties {
	
	public List<String> hostnames ();
	
	public String markdownEngine ();
	
	public String contextPath ();
	
	public String baseUrl ();
	
	public String id ();
	
	public Object get (String field);
	
	public <T> T getOrDefault (String field, T defaultValue);
	
	public String theme ();

	public String queryIndexMode ();
	
	public Locale locale ();
	
	public String language();
	
	public String defaultContentType ();
	
	public List<String> contentPipeline ();
	
	public String cacheEngine();
	
	public boolean cacheContent();
	
	public String templateEngine();

	public List<String> activeModules();
	
	public default boolean spaEnabled () {
		return false;
	}
	
	public UIProperties ui();
	
	public TranslationProperties translation ();
}
