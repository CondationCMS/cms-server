package com.condation.cms.api.ui.action;

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
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 * @author thorstenmarx
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UIScriptAction extends UIAction {

	public static final String TYPE = "script";

	private String function;
	private String module;

	private Map<String, Object> parameters;

	public UIScriptAction() {
		super(TYPE);
	}
	
	public UIScriptAction (String module, String function, Map<String, Object> parameters) {
		this();
		this.function = function;
		this.module = module;
		this.parameters = parameters;
	}
	
	public UIScriptAction (String module, Map<String, Object> parameters) {
		this(module, "runAction", parameters);
	}
	
}
