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

import com.condation.cms.templates.tags.ElseIfTag;
import com.condation.cms.templates.tags.ElseTag;
import com.condation.cms.templates.tags.EndForTag;
import com.condation.cms.templates.tags.EndIfTag;
import com.condation.cms.templates.tags.EndMacroTag;
import com.condation.cms.templates.tags.ForTag;
import com.condation.cms.templates.tags.IfTag;
import com.condation.cms.templates.tags.IncludeTag;
import com.condation.cms.templates.tags.MacroTag;
import com.condation.cms.templates.tags.SetTag;

/**
 *
 * @author t.marx
 */
public class TemplateEngineBuilder {
	
	public static TemplateEngine buildDefault (TemplateLoader templateLoader) {
		TemplateConfiguration config = new TemplateConfiguration();
		
		config.registerTag(new IfTag())
				.registerTag(new ElseIfTag())
				.registerTag(new ElseTag())
				.registerTag(new EndIfTag())
				.registerTag(new ForTag())
				.registerTag(new EndForTag())
				.registerTag(new SetTag())
				.registerTag(new MacroTag())
				.registerTag(new EndMacroTag())
				.registerTag(new IncludeTag())
				;
		
		config.setTemplateLoader(templateLoader);
		
		return new TemplateEngine(config);
	}
}
