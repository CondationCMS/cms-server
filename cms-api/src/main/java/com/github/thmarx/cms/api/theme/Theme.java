package com.github.thmarx.cms.api.theme;

/*-
 * #%L
 * cms-api
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

import com.github.thmarx.cms.api.ThemeProperties;
import java.nio.file.Path;

/**
 *
 * @author thmar
 */

public interface Theme {
	
	Assets getAssets();
	
	String getName();
	
	Path templatesPath ();
	
	Path extensionsPath ();
	
	Path assetsPath ();
	
	ThemeProperties properties();
	
	/**
	 * empty theme is used for sites without configured theme
	 * @return 
	 */
	boolean empty();
}
