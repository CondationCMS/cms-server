package com.condation.cms.templates;

/*-
 * #%L
 * templates
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

import com.condation.cms.templates.parser.ASTNode;
import com.condation.cms.templates.renderer.Renderer;
import com.condation.cms.templates.renderer.ScopeStack;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.jexl3.JexlEngine;

/**
 *
 * @author thmar
 */
@RequiredArgsConstructor
public class DefaultTemplate implements Template {

	@Getter
	private final ASTNode rootNode;
	
	private final Renderer renderer;
	
	@Override
	public String evaluate(Map<String, Object> context) {
		
		ScopeStack scopes = new ScopeStack(context);
		
		return evaluate(scopes);
	}
	
	public String evaluate (ScopeStack scopes) {
		return renderer.render(rootNode, scopes);
	}
	
}
