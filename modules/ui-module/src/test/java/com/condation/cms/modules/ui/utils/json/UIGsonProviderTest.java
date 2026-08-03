package com.condation.cms.modules.ui.utils.json;

/*-
 * #%L
 * UI Module
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

import com.condation.cms.modules.ui.extensionpoints.remotemethods.RemoteFileEnpoints;
import com.condation.cms.api.ui.elements.FormDefinition;
import com.condation.cms.api.ui.elements.FormTab;
import com.condation.cms.api.ui.elements.PageTemplate;
import com.condation.cms.api.ui.elements.fields.StringField;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.List;
import java.util.Map;
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
		var content = new RemoteFileEnpoints.Content(
				"readme.md",
				"/docs/readme.md",
				"/docs/readme",
				"Read me"
		);
        String json = UIGsonProvider.INSTANCE.toJson(content);
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();

		Assertions.assertThat(obj.get("name").getAsString()).isEqualTo("readme.md");
		Assertions.assertThat(obj.get("uri").getAsString()).isEqualTo("/docs/readme.md");
		Assertions.assertThat(obj.get("url").getAsString()).isEqualTo("/docs/readme");
		Assertions.assertThat(obj.get("title").getAsString()).isEqualTo("Read me");
        Assertions.assertThat(obj.get("directory").getAsBoolean()).isFalse();
        Assertions.assertThat(obj.get("media").getAsBoolean()).isFalse();
        Assertions.assertThat(obj.get("content").getAsBoolean()).isTrue();
    }

	@Test
	void serializesTypedPageTemplateAsFrontendContract() {
		var pageTemplate = PageTemplate.builder()
				.name("StartPage")
				.template("start.html")
				.contentFolder("content")
				.forms(Map.of("settings", new FormDefinition(
						List.of(new StringField("title", "Title")),
						List.of(new FormTab("SEO", List.of(
								new StringField("description", "Description")))))))
				.build();

		JsonObject json = JsonParser.parseString(UIGsonProvider.INSTANCE.toJson(pageTemplate))
				.getAsJsonObject();

		Assertions.assertThat(json.has("data")).isFalse();
		Assertions.assertThat(json.get("name").getAsString()).isEqualTo("StartPage");
		Assertions.assertThat(json.get("template").getAsString()).isEqualTo("start.html");
		Assertions.assertThat(json.get("contentFolder").getAsString()).isEqualTo("content");
		Assertions.assertThat(json.get("createButton").getAsBoolean()).isTrue();
		var fields = json.getAsJsonObject("forms")
				.getAsJsonObject("settings")
				.getAsJsonArray("fields");
		Assertions.assertThat(fields).hasSize(1);
		Assertions.assertThat(fields.get(0).getAsJsonObject().get("type").getAsString()).isEqualTo("text");
		Assertions.assertThat(fields.get(0).getAsJsonObject().get("name").getAsString()).isEqualTo("title");
		Assertions.assertThat(fields.get(0).getAsJsonObject().get("title").getAsString()).isEqualTo("Title");
		var tabs = json.getAsJsonObject("forms")
				.getAsJsonObject("settings")
				.getAsJsonArray("tabs");
		Assertions.assertThat(tabs).hasSize(1);
		Assertions.assertThat(tabs.get(0).getAsJsonObject().get("title").getAsString()).isEqualTo("SEO");
		Assertions.assertThat(tabs.get(0).getAsJsonObject().getAsJsonArray("fields")
				.get(0).getAsJsonObject().get("name").getAsString()).isEqualTo("description");
	}
	
}
