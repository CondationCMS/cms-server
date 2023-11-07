package com.github.thmarx.cms.api;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 Marx-Software
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.function.Function;
import java.util.regex.Pattern;

/**
 *
 * @author t.marx
 */
public abstract class Constants {
	
	public static class MetaFields {
		public static final String DRAFT = "draft";
		public static final String PUBLISHED = "published";
		public static final String TITLE = "title";
		public static final String MENU = "menu";
		public static final String MENU_VISIBLE = "visible";
		public static final String MENU_POSITION = "position";
		public static final String MENU_TITLE = "title";
	}
	
	public static final String SPLIT_PATH_PATTERN = Pattern.quote("/");
	
	public static final Pattern SECTION_PATTERN = Pattern.compile("\\w+[a-zA-Z0-9-]*\\.(?<section>[a-zA-Z0-9]+[a-zA-Z0-9-]*)\\.md");
	
	public static final Function<String, Pattern> SECTION_OF_PATTERN = (fileName) -> {
		return Pattern.compile("%s\\.([a-zA-Z0-9]+[a-zA-Z0-9-]*)\\.md".formatted(fileName));
	};
	
	public static final Pattern SECTION_ORDERED_PATTERN = Pattern.compile("\\w+[a-zA-Z0-9-]*\\.(?<section>[a-zA-Z0-9]+[a-zA-Z0-9-]*)\\.(?<index>[0-9]+[0-9]*)\\.md");
	
	public static final Function<String, Pattern> SECTION_ORDERED_OF_PATTERN = (fileName) -> {
		return Pattern.compile("%s\\.([a-zA-Z0-9]+[a-zA-Z0-9-]*)\\.[0-9]+[0-9]*\\.md".formatted(fileName));
	};
	
	public static final int DEFAULT_SECTION_ORDERED_INDEX = 0;
	public static final double DEFAULT_MENU_POSITION = 1000f;
	public static final boolean DEFAULT_MENU_VISIBILITY = true;
}
