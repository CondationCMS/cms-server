package com.github.thmarx.cms.theme;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 Marx-Software
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
import com.github.thmarx.cms.api.Constants;
import com.github.thmarx.cms.api.ThemeProperties;
import com.github.thmarx.cms.api.theme.Assets;
import com.github.thmarx.cms.api.theme.Theme;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author thmar
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultTheme implements Theme {

	public static final Theme EMPTY = new DefaultTheme(null, null, true);

	private final Path themePath;
	private final ThemeProperties properties;
	private boolean empty = false;
	private Assets assets = new DefaultAssets();

	private DefaultTheme(final Path templatePath, final ThemeProperties themeProperties, final boolean empty) {
		this(templatePath, themeProperties);
		this.empty = empty;
	}

	public static Theme load(Path themePath) throws IOException {
		Yaml yaml = new Yaml();
		Path themeYaml = themePath.resolve("theme.yaml");

		var content = Files.readString(themeYaml, StandardCharsets.UTF_8);
		Map<String, Object> config = (Map<String, Object>) yaml.load(content);

		return new DefaultTheme(themePath, new ThemeProperties(config));
	}

	@Override
	public boolean empty() {
		return empty;
	}

	@Override
	public ThemeProperties properties() {
		return properties;
	}

	@Override
	public String getName() {
		return (String) properties.get("name");
	}

	@Override
	public Path assetsPath() {
		return themePath.resolve(Constants.Folders.ASSETS);
	}

	@Override
	public Path templatesPath() {
		return themePath.resolve(Constants.Folders.TEMPLATES);
	}

	@Override
	public Assets getAssets() {
		return assets;
	}

	@Override
	public Path extensionsPath() {
		return themePath.resolve(Constants.Folders.EXTENSIONS);
	}
}
