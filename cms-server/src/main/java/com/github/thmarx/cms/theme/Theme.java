package com.github.thmarx.cms.theme;

import com.github.thmarx.cms.api.ThemeProperties;
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
public class Theme {
	private final Path themePath;
	private final ThemeProperties properties;
	
	
	public static Theme load (Path themePath) throws IOException {
		Yaml yaml = new Yaml();
		Path themeYaml = themePath.resolve("theme.yaml");
		
		var  content = Files.readString(themeYaml, StandardCharsets.UTF_8);
		Map<String, Object> config = (Map<String, Object>)yaml.load(content);
		
		return new Theme(themePath, new ThemeProperties(config));
	}
}
