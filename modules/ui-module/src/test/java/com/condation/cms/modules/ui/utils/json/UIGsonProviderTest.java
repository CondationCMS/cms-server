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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author thorstenmarx
 */
public class UIGsonProviderTest {
	
	public UIGsonProviderTest() {
	}

    @Test
    void testContentSerializationIncludesInterfaceProperties() {
		var content = new RemoteFileEnpoints.Content("readme.md", "/docs/readme.md");
        String json = UIGsonProvider.INSTANCE.toJson(content);
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();

        Assertions.assertThat(obj.get("name").getAsString()).isEqualTo("readme.md");
        Assertions.assertThat(obj.get("uri").getAsString()).isEqualTo("/docs/readme.md");
        Assertions.assertThat(obj.get("directory").getAsBoolean()).isFalse();
        Assertions.assertThat(obj.get("media").getAsBoolean()).isFalse();
        Assertions.assertThat(obj.get("content").getAsBoolean()).isTrue();
    }
	
}
