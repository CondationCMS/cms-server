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

import java.lang.reflect.Type;

/**
 *
 * @author thorstenmarx
 */


public class FileSerializer implements JsonSerializer<RemoteFileEnpoints.File> {
    @Override
    public JsonElement serialize(RemoteFileEnpoints.File src, Type typeOfSrc, JsonSerializationContext context) {
        // Standard-Felder serialisieren
        JsonObject jsonObject = context.serialize(src, src.getClass()).getAsJsonObject();

        // Interface-Methoden ergänzen
        jsonObject.addProperty("directory", src.directory());
        jsonObject.addProperty("media", src.media());
        jsonObject.addProperty("content", src.content());

        return jsonObject;
    }
}

