package com.condation.cms.templates.functions.impl;

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

import com.condation.cms.api.feature.features.InjectorFeature;
import com.condation.cms.api.messages.MessageSource;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.content.RenderContext;
import com.condation.cms.templates.functions.TemplateFunction;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author thorstenmarx
 */
@Slf4j
public class MessageFunction implements TemplateFunction {

	public static final String NAME = "message";

    private final RequestContext context;
    
	public MessageFunction(RequestContext requestContext) {
		this.context = requestContext;
	}


	@Override
	public String name() {
		return NAME;
	}

    @Override
    public Object invoke(Object... params) {
        if (params == null || params.length < 2) {
            return "";
        }
        if (!(params[0] instanceof String) && !(params[1] instanceof String)) {
            return "";
        }
        
        String bundle = (String) params[0];
        String label = (String) params[1];
        
        var theme = context.get(RenderContext.class).theme();
        MessageSource messageSource = null;
		if (theme.empty()) {
			messageSource = context.get(InjectorFeature.class).injector().getInstance(MessageSource.class);
		} else {
			messageSource = theme.getMessages();
		}
        
        return messageSource.getLabel(bundle, label);
    }
	
}
