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

import com.condation.cms.modules.ui.extensionpoints.remotemethods.RemoteFileEnpoints;
import com.google.gson.*;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 *
 * @author thorstenmarx
 */
public class FileTypeAdapterFactory implements TypeAdapterFactory {

    @Override
    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        Class<?> rawType = typeToken.getRawType();

        // Nur anpassen, wenn es File implementiert
        if (!RemoteFileEnpoints.File.class.isAssignableFrom(rawType)) {
            return null;
        }

        // Hole den Standardadapter für die konkrete Klasse (z. B. Content.class)
        TypeAdapter<T> delegate = gson.getDelegateAdapter(this, typeToken);

        return new TypeAdapter<>() {
            @Override
            public void write(JsonWriter out, T value) throws IOException {
                // Serialisiere Objekt normal
                JsonObject jsonObject = delegate.toJsonTree(value).getAsJsonObject();

                // Ergänze Interface-Methoden
                RemoteFileEnpoints.File file = (RemoteFileEnpoints.File) value;
                jsonObject.addProperty("directory", file.directory());
                jsonObject.addProperty("media", file.media());
                jsonObject.addProperty("content", file.content());

                // Schreibe das zusammengesetzte JSON
                Streams.write(jsonObject, out);
            }

            @Override
            public T read(JsonReader in) throws IOException {
                // Standard-Deserialisierung
                return delegate.read(in);
            }
        };
    }
}
