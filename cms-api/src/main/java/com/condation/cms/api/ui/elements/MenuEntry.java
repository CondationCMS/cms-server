package com.condation.cms.api.ui.elements;

/*-
 * #%L
 * cms-api
 * %%
 * Copyright (C) 2024 - 2025 Condation
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

import com.condation.cms.api.ui.action.UIAction;
import com.condation.cms.api.utils.JSONUtil;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

/**
 *
 * @author thorstenmarx
 */
@Builder
@Getter
public class MenuEntry {

	private String name;
	
	private String id;
	
	@Builder.Default
	private boolean divider = false;
	
	@Builder.Default
	private List<String> permissions = new ArrayList<>();
	
	@Builder.Default
	private int position = 0;
	
	@Builder.Default
	private List<MenuEntry> children = new ArrayList<>();
	
	private UIAction action;
	
	public void addChildren (MenuEntry entry) {
		if (children == null) {
			children = new ArrayList<>();
		}
		
		children = new ArrayList<>(children);
		children.add(entry);
	}

	public String getActionDefinition () {
		return action != null ? JSONUtil.toJson(action) : "";
	}
	
	public boolean hasChildren () {
		return children != null && ! children.isEmpty();
	}
}
