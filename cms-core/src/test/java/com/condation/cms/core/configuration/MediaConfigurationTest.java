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
import com.condation.cms.core.configuration.source.TomlConfigSource;
import com.condation.cms.core.configuration.source.YamlConfigSource;
import com.condation.cms.api.eventbus.EventBus;
import com.condation.cms.core.configuration.configs.MediaConfiguration;
import java.io.IOException;
import java.nio.file.Path;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.SchedulerException;

/**
 *
 * @author t.marx
 */
@ExtendWith(MockitoExtension.class)
public class MediaConfigurationTest {

	MediaConfiguration configuration;

	@Mock
	EventBus eventBus;

	@BeforeEach
	public void setup() throws IOException, SchedulerException {
		configuration = MediaConfiguration.builder(eventBus)
				.id("media-config")
				.addSource(YamlConfigSource.build(Path.of("config/media.yaml")))
				.addSource(TomlConfigSource.build(Path.of("config/media.toml")))
				.build();
	}

	@Test
	public void test_object() {
		var medias = configuration.getFormats();

		Assertions.assertThat(medias)
				.isNotNull()
				.hasSize(4);

		MediaConfiguration.Format f1 = medias.getFirst();
		Assertions.assertThat(f1.getName()).isEqualTo("big");
		Assertions.assertThat(f1.getFormat()).isEqualTo("webp");
		Assertions.assertThat(f1.isCompression()).isTrue();
		Assertions.assertThat(f1.getHeight()).isEqualTo(512);
		Assertions.assertThat(f1.getWidth()).isEqualTo(512);
	}
}
