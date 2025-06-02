package com.condation.cms;

/*-
 * #%L
 * cms-server
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


import com.condation.cms.api.template.TemplateEngine;
import com.condation.cms.api.theme.Theme;
import com.condation.cms.filesystem.FileDB;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.StringSubstitutor;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class TestTemplateEngine implements TemplateEngine {

	private StringSubstitutor stringSubstitutor = new StringSubstitutor();

	private final FileDB db;
	
	@Override
	public void invalidateCache() {

	}

	@Override
	public String render(final String template, Model model) throws IOException {
		
		Map<String, Object> values = new HashMap<>();
		values.putAll(model.values);
		values.put("meta.title", ((Map<String, Object>)model.values.getOrDefault("meta", Map.of())).getOrDefault("title", "<no title>"));
		
		String templateContent = db.getFileSystem().loadContent(db.getFileSystem().resolve("templates").resolve(template), StandardCharsets.UTF_8);
		
		StringSubstitutor sub = new StringSubstitutor(values);
		return sub.replace(templateContent);
	}

	@Override
	public void updateTheme(Theme theme) {
	}

}
