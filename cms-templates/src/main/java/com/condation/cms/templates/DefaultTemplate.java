package com.condation.cms.templates;

/*-
 * #%L
 * CMS Templates
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
import com.condation.cms.api.Constants;
import com.condation.cms.templates.exceptions.TagException;
import com.condation.cms.templates.functions.JexlTemplateFunction;
import com.condation.cms.templates.functions.impl.DateFunction;
import com.condation.cms.templates.functions.impl.MessageFunction;
import com.condation.cms.templates.functions.impl.NodeFunction;
import com.condation.cms.templates.functions.impl.NodeMetaFunction;
import com.condation.cms.templates.functions.impl.UriParamFunction;
import com.condation.cms.templates.parser.ASTNode;
import com.condation.cms.templates.renderer.Renderer;
import com.condation.cms.templates.renderer.ScopeStack;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author thmar
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultTemplate implements Template {

	private final String templateName;

	@Getter
	private final ASTNode rootNode;

	private final Renderer renderer;

	@Override
	public void evaluate(Map<String, Object> context, Writer writer, DynamicConfiguration dynamicConfiguration) throws IOException {
		ScopeStack scopes = createScope(context, dynamicConfiguration);

		evaluate(scopes, writer, dynamicConfiguration);

		writer.flush();
	}

	@Override
	public String evaluate(Map<String, Object> context, DynamicConfiguration dynamicConfiguration) throws IOException {
		ScopeStack scopes = createScope(context, dynamicConfiguration);

		try (var writer = new StringWriter()) {
			renderer.render(rootNode, scopes, writer, dynamicConfiguration);
			writer.flush();
			return writer.toString();
		} catch (TagException te) {
			log.debug("error rendering template: {}", templateName);
			throw te;
		}
	}

	public void evaluate(ScopeStack scopes, Writer writer, DynamicConfiguration dynamicConfiguration) throws IOException {
		try {
			renderer.render(rootNode, scopes, writer, dynamicConfiguration);
		} catch (TagException te) {
			log.debug("error rendering template: {}", templateName);
			throw te;
		}
	}

	private ScopeStack createScope(Map<String, Object> context, DynamicConfiguration dynamicConfiguration) {
		var scope = new ScopeStack(context);
		scope.setVariable(DateFunction.NAME, new JexlTemplateFunction(new DateFunction()));
		scope.setVariable(NodeFunction.NAME, new JexlTemplateFunction(new NodeFunction(dynamicConfiguration.requestContext())));
		scope.setVariable(NodeMetaFunction.NAME, new JexlTemplateFunction(new NodeMetaFunction(dynamicConfiguration.requestContext())));
		scope.setVariable(UriParamFunction.NAME, new JexlTemplateFunction(new UriParamFunction()));
		//scope.setVariable(MessageFunction.NAME, new JexlTemplateFunction(new MessageFunction(dynamicConfiguration.requestContext())));

		getOrCreateNamespace(scope, Constants.TemplateNamespaces.CMS)
				.put(MessageFunction.NAME, new JexlTemplateFunction(new MessageFunction(dynamicConfiguration.requestContext())));
		
		dynamicConfiguration.templateFunctions().forEach(tf -> {
			getOrCreateNamespace(scope, tf.namespace()).put(tf.name(), new JexlTemplateFunction(tf));
		});

		return scope;
	}

	private Map<String, Object> getOrCreateNamespace(ScopeStack scope, String namespace) {
		var namespaceOPT = scope.getVariable(namespace);
		if (namespaceOPT.isEmpty()) {
			scope.setVariable(namespace, new HashMap<>());
		}
		return ((Map<String, Object>) scope.getVariable(namespace).get());
	}

}
