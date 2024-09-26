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
import java.util.function.Function;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 *
 * @author t.marx
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class Constants {
	
	public static final String PID_FILE = "cms.pid";
	
	public static class Environments {
		public static final String DEV = "dev";
		public static final String PROD = "prod";
	}
	
	public static class MetaFields {
		public static final String PUBLISHED = "published";
		public static final String PUBLISH_DATE = "publish_date";
		public static final String UNPUBLISH_DATE = "unpublish_date";
		
		public static final String TITLE = "title";
		public static final String EXCERPT = "excerpt";
		
		public static final String MENU = "menu";
		public static final String MENU_VISIBLE = "visible";
		public static final String MENU_POSITION = "position";
		public static final String MENU_TITLE = "title";
		
		public static final String REDIRECT_STATUS = "redirect.status";
		public static final String REDIRECT_LOCATION = "redirect.location";
		
		public static final String TEMPLATE = "template";
		
		public static final String TYPE = "type";
	}
	
	public static class Folders {
		public static final String CONTENT = "content/";
		public static final String TEMPLATES = "templates/";
		public static final String ASSETS = "assets/";
		public static final String EXTENSIONS = "extensions/";
		public static final String MODULES = "modules/";
	}
	
	public static class NodeType {
		public static final String VIEW = "view";
		public static final String PAGE = "page";
	}
	
	public static class ContentTypes {
		public static final String HTML = "text/html";
		public static final String JSON = "application/json";
	}
	
	public static final String SPLIT_PATH_PATTERN = Pattern.quote("/");
	
	public static final Pattern TAXONOMY_VALUE = Pattern.compile("taxonomy\\.([a-zA-Z0-9-]+)\\.yaml");
	
	public static final Pattern SECTION_PATTERN = Pattern.compile("\\w+\\.(?<section>[a-zA-Z0-9-]+)\\.md");
	
	public static final Function<String, Pattern> SECTION_OF_PATTERN = (fileName) -> {
		return Pattern.compile("%s\\.(?<section>[a-zA-Z0-9-]+)\\.md".formatted(Pattern.quote(fileName)));
	};
	
	public static final Pattern SECTION_ORDERED_PATTERN = Pattern.compile("[\\w-]+\\.(?<section>[a-zA-Z0-9-]+)\\.(?<index>\\d+)\\.md");
	
	public static final Function<String, Pattern> SECTION_ORDERED_OF_PATTERN = (fileName) -> {
		return Pattern.compile("%s\\.[a-zA-Z0-9-]+\\.[0-9]+\\.md".formatted(Pattern.quote(fileName)));
	};
	
	public static final int DEFAULT_SECTION_ORDERED_INDEX = 0;
	public static final double DEFAULT_MENU_POSITION = 1000f;
	public static final boolean DEFAULT_MENU_VISIBILITY = true;
	public static final int DEFAULT_EXCERPT_LENGTH = 200;
	public static final int DEFAULT_PAGE = 1;
	public static final int DEFAULT_PAGE_SIZE = 5;
	
	public static final String DEFAULT_CONTENT_TYPE = ContentTypes.HTML;
	public static final List<String> DEFAULT_CONTENT_PIPELINE = List.of("markdown", "shortcode");
	
	public static final int DEFAULT_REDIRECT_STATUS = 301;
	
	public static final String DEFAULT_CACHE_ENGINE = "local";
	public static final boolean DEFAULT_CONTENT_CACHE_ENABLED = false;
	
	public static class Taxonomy {
		public static final String DEFAULT_TEMPLATE = "taxonomy.html";
		public static final String DEFAULT_SINGLE_TEMPLATE = "taxonomy.single.html";
	}
	
}
