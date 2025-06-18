package com.condation.cms.modules.ui.utils.json;

/*-
 * #%L
 * ui-module
 * %%
 * Copyright (C) 2023 - 2025 CondationCMS
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.ToNumberPolicy;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.lang.reflect.Type;

/**
 *
 * @author thorstenmarx
 */
public class UIGsonProvider {

	public static final Gson INSTANCE = new GsonBuilder()
			.setPrettyPrinting()
			.setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
			.registerTypeAdapter(Date.class, new UtcDateSerializer())
			.registerTypeAdapterFactory(new FileTypeAdapterFactory())
			.setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
			.create();

	public static class UtcDateSerializer implements JsonSerializer<Date> {

		private final SimpleDateFormat sdf;

		public UtcDateSerializer() {
			sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		}

		@Override
		public JsonElement serialize(Date date, Type typeOfSrc, JsonSerializationContext context) {
			return new JsonPrimitive(sdf.format(date));
		}
	}
}
