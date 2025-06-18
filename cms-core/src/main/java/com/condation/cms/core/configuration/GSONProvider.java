package com.condation.cms.core.configuration;

/*-
 * #%L
 * tests
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.time.Duration;

/**
 *
 * @author t.marx
 */
 public class GSONProvider {

	public static final Gson GSON = new GsonBuilder()
			.enableComplexMapKeySerialization()
			.registerTypeAdapter(Duration.class, new DurationTypeAdapter())
			.create();

	public static class DurationTypeAdapter extends TypeAdapter<Duration> {

		@Override
		public void write(JsonWriter out, Duration value) throws IOException {
			if (value == null) {
				out.nullValue();
			} else {
				// Schreibe die Duration als ISO-8601 String (z.B. "PT1H30M" für 1 Stunde 30 Minuten)
				out.value(value.toString());
			}
		}

		@Override
		public Duration read(JsonReader in) throws IOException {
			if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
				in.nextNull();
				return null;
			} else {
				// Lese den ISO-8601 String und konvertiere ihn zurück zu einer Duration
				return Duration.parse(in.nextString());
			}
		}
	}
}
