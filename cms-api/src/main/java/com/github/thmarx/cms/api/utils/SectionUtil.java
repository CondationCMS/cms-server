package com.github.thmarx.cms.api.utils;

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
import com.github.thmarx.cms.api.Constants;

/**
 *
 * @author t.marx
 */
public class SectionUtil {

	public static boolean isOrderedSection(final String name) {
		return Constants.SECTION_ORDERED_PATTERN.matcher(name).matches();
	}

	public static String getSectionName(final String name) {
		if (isOrderedSection(name)) {
			var matcher = Constants.SECTION_ORDERED_PATTERN.matcher(name);
			matcher.matches();
			return matcher.group("section");
		} else {
			var matcher = Constants.SECTION_PATTERN.matcher(name);
			matcher.matches();
			return matcher.group("section");
		}
	}

	public static int getSectionIndex(final String name) {
		if (isOrderedSection(name)) {
			var matcher = Constants.SECTION_ORDERED_PATTERN.matcher(name);
			matcher.matches();
			return Integer.parseInt(matcher.group("index"));
		} else {
			return Constants.DEFAULT_SECTION_ORDERED_INDEX;
		}
	}

	public static boolean isSection(final String name) {
		return Constants.SECTION_PATTERN.matcher(name).matches()
				|| Constants.SECTION_ORDERED_PATTERN.matcher(name).matches();
	}
}
