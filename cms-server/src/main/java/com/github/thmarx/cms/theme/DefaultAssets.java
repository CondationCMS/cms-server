package com.github.thmarx.cms.theme;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 Marx-Software
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
