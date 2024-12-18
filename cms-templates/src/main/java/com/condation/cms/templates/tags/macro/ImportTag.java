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
import com.condation.cms.templates.DefaultTemplate;
import com.condation.cms.templates.Tag;
import com.condation.cms.templates.exceptions.TagException;
import com.condation.cms.templates.parser.TagNode;
import com.condation.cms.templates.renderer.Renderer;
import com.condation.cms.templates.renderer.ScopeStack;
import com.condation.cms.templates.tags.AbstractTag;
import com.condation.cms.templates.utils.NullWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jexl3.JexlExpression;

/**
 *
 * @author t.marx
 */
@Slf4j
public class ImportTag extends AbstractTag implements Tag {

	// Regular expression to match the template and optional namespace
	private static final String REGEX = "^((?:'([^']+)'|\"([^\"]+)\"|[a-zA-Z0-9._-]+))(?:\\s+as\\s+(\\w+))?$";
	private static final Pattern PATTERN = Pattern.compile(REGEX);

	@Override
	public String getTagName() {
		return "import";
	}

	@Override
	public void render(TagNode node, Renderer.Context context, Writer writer) {
		try {

			var importDefinition = parseImport(node.getCondition(), node);

			var scope = context.createEngineContext();
			final JexlExpression expression = context.engine().createExpression(importDefinition.template);
			var templateString = (String) evaluateExpression(node, expression, context, scope);

			var template = (DefaultTemplate) context.templateEngine().getTemplate(templateString);
			if (template != null) {
				CustomScopeStack scopeStack = new CustomScopeStack();
				template.evaluate(scopeStack, new NullWriter());

				var namespace = new HashMap<String, MacroTag.MacroFunction>();
				scopeStack.macros().forEach(macro -> {
					if (importDefinition.namespace.isPresent()) {
						namespace.put(macro.getMacro().getName(), macro);
					} else {
						context.scopes().setVariable(macro.getMacro().getName(), macro);
					}
				});
				if (importDefinition.namespace.isPresent()) {
					context.scopes().setVariable(importDefinition.namespace.get(), namespace);
				}

			}
		} catch (Exception e) {
			throw new TagException("error importing template", node.getLine(), node.getColumn());
		}
	}

	private class CustomScopeStack extends ScopeStack {

		private List<String> macros = new ArrayList<>();

		public Stream<MacroTag.MacroFunction> macros() {
			return macros.stream()
					.map(macro -> getVariable(macro))
					.filter(Optional::isPresent)
					.map(Optional::get)
					.map(MacroTag.MacroFunction.class::cast);
		}

		@Override
		public void setVariable(String name, Object value) {
			super.setVariable(name, value);

			if (value instanceof MacroTag.MacroFunction) {
				macros.add(name);
			}
		}
	}

	// Method to parse the import statement
	private ImportDefinition parseImport(String importStatement, TagNode node) {

		Matcher matcher = PATTERN.matcher(importStatement.trim());

		if (matcher.matches()) {
			String template = matcher.group(1);
			String namespace = matcher.group(4);
			return new ImportDefinition(template, Optional.ofNullable(namespace));
		} else {
			throw new TagException("Invalid import definition: " + importStatement, node.getLine(), node.getColumn());
		}
	}

	private record ImportDefinition(String template, Optional<String> namespace) {

	}
;
}
