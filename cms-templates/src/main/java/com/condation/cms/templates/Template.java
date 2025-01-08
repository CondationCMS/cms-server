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

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

/**
 *
 * @author thmar
 */
public interface Template {
	
	default String evaluate () throws IOException {
		return evaluate(Map.of());
	}
	
	default String evaluate(Map<String, Object> context) throws IOException {
		var writer = new StringWriter();
		evaluate(context, writer, DynamicConfiguration.EMPTY);
		return writer.toString();
	}
	
	String evaluate(Map<String, Object> context, DynamicConfiguration dynamicConfiguration) throws IOException;
	
	void evaluate (Map<String, Object> context, Writer writer, DynamicConfiguration dynamicConfiguration) throws IOException;
}
