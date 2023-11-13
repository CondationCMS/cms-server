package com.github.thmarx.cms.theme;

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

import com.github.thmarx.cms.api.theme.Assets;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;

/**
 *
 * @author t.marx
 */
public class DefaultAssets implements Assets {

	private enum Type {
		CSS,
		JS;
	}
	
	Multimap<Type, String> assets;
	
	public DefaultAssets() {
		assets = LinkedHashMultimap.create();
	}

	@Override
	public void addCss(String css) {
		assets.put(Type.CSS, css);
	}
	
	public Collection<String> css() {
		return assets.get(Type.CSS);
	}

	@Override
	public void addJs(String js) {
		assets.put(Type.JS, js);
	}
	
	public Collection<String> js() {
		return assets.get(Type.JS);
	}
	
}
