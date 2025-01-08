package com.condation.cms.templates.tags.macro;

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

import com.condation.cms.templates.Tag;
import com.condation.cms.templates.parser.ASTNode;
import com.condation.cms.templates.parser.TagNode;
import com.condation.cms.templates.renderer.Renderer;
import com.condation.cms.templates.renderer.ScopeStack;
import com.condation.cms.templates.utils.MacroUtils;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlException;
import org.apache.commons.jexl3.introspection.JexlMethod;

/**
 *
 * @author t.marx
 */
public class MacroTag implements Tag {

	@Override
	public String getTagName() {
		return "macro";
	}

	@Override
	public Optional<String> getCloseTagName() {
		return Optional.of("endmacro");
	}

	@Override
	public void render(TagNode node, Renderer.Context context, Writer writer) {
		Optional<Macro> macroOpt = MacroUtils.parseMacro(node.getCondition());
		if (macroOpt.isEmpty()) {
			return;
		}
		
		var macro = macroOpt.get();
		macro.setChildren(node.getChildren());
		
		context.scopes().setVariable(macro.name, new MacroFunction(macro, context));
	}

	@RequiredArgsConstructor
	public static class Macro {
		
		@Getter
		private final String name;
		private final List<String> parameters;
		
		@Getter
		@Setter
		private List<ASTNode> children;
	}

	@Slf4j
	@RequiredArgsConstructor
	public static class MacroFunction implements JexlMethod {

		@Getter
		private final Macro macro;
		private final Renderer.Context context;

		@Override
		public Class<?> getReturnType() {
			return String.class;
		}

		@Override
		public Object invoke(Object obj, Object... params) throws Exception {
			
			ScopeStack scope = new ScopeStack(context.scopes());
			
			for (int i = 0; i < macro.parameters.size(); i++) {
				Object value = "";
				if ( i < params.length ) {
					value = params[i];
				}
				
				scope.setVariable(macro.parameters.get(i), value);
			}
			StringWriter writer = new StringWriter();
			
			var newContext = new Renderer.Context(
					context.engine(), 
					scope, 
					context.renderer(), 
					context.templateEngine(), 
					context.dynamicConfiguration()
			);
			for (var child : macro.children) {
				context.renderer().render(child, newContext, writer);
			}
			
			return writer.toString();
		}

		@Override
		public boolean isCacheable() {
			return false;
		}

		@Override
		public boolean tryFailed(Object rval) {
			return JexlEngine.TRY_FAILED.equals(rval);
		}

		@Override
		public Object tryInvoke(String name, Object obj, Object... params) throws JexlException.TryFailed {
			if (macro.name.equals(name)) {
				try {
					return invoke(obj, params);
				} catch (Exception ex) {
					log.error("error calling macro", ex);
				}
			}
			return JexlEngine.TRY_FAILED;
		}

	}
}
