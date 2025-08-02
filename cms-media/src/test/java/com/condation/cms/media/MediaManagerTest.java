package com.condation.cms.media;

/*-
 * #%L
 * cms-media
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


import com.condation.cms.api.configuration.Configuration;
import com.condation.cms.api.configuration.configs.MediaConfiguration;
import com.condation.cms.api.configuration.configs.ServerConfiguration;
import com.condation.cms.api.media.MediaFormat;
import com.condation.cms.api.media.MediaUtils;
import com.condation.cms.api.utils.FileUtils;
import com.condation.cms.core.configuration.ConfigurationFactory;
import com.condation.cms.core.configuration.properties.ExtendedServerProperties;
import com.condation.cms.test.PropertiesLoader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class MediaManagerTest {
	
	static MediaManager mediaManager;
	
	@BeforeAll
	public static void setup () throws IOException {
		
		FileUtils.deleteFolder(Path.of("target/media"));
		
		Configuration config = new Configuration();
		config.add(MediaConfiguration.class, new MediaConfiguration(List.of(
				new MediaFormat("cropped", 40, 40, MediaUtils.Format.PNG, true, true),
				new MediaFormat("focal", 400, 100, MediaUtils.Format.PNG, true, true)
		)));
		var serverConfig = new ServerConfiguration(new ExtendedServerProperties(ConfigurationFactory.serverConfiguration()));
		config.add(ServerConfiguration.class, serverConfig);
		
		mediaManager = new SiteMediaManager(
				Path.of("src/test/resources/assets"), 
				Path.of("target/"), 
				new TestTheme(PropertiesLoader.themeProperties(Path.of("src/test/resources/theme.yaml"))), 
				config
		);
	}

	@Test
	public void test_cropped() throws IOException {
		var content = mediaManager.getScaledContent("test.jpg", mediaManager.getMediaFormat("cropped"));
		
		Files.write(Path.of("target/test_cropped.jpg"), content.get());
	}
	
	@Test
	public void test_focal() throws IOException {
		var content = mediaManager.getScaledContent("demo.jpg", mediaManager.getMediaFormat("focal"));
		
		Files.write(Path.of("target/demo_focal.jpg"), content.get());
	}
	
}
