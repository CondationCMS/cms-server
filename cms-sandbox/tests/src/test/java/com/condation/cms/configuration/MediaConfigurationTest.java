package com.condation.cms.configuration;

import com.condation.cms.configuration.source.TomlConfigSource;
import com.condation.cms.configuration.source.YamlConfigSource;
import com.condation.cms.api.eventbus.EventBus;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
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

	Configuration configuration;
	
	@Mock
	EventBus eventBus;
	
	@BeforeEach
	public void setup() throws IOException, SchedulerException {
		configuration = Configuration.builder(eventBus)
				.id("media-config")
				.addSource(YamlConfigSource.build(Path.of("configs/media.yaml")))
				.addSource(TomlConfigSource.build(Path.of("configs/media.toml")))
				.build();
	}
	
	@Test
	public void test_object () {
		var medias = configuration.getList("formats", Format.class);
		
		Assertions.assertThat(medias)
				.isNotNull()
				.hasSize(4);
		
		Format f1 = medias.getFirst();
		Assertions.assertThat(f1.name).isEqualTo("yaml");
		Assertions.assertThat(f1.format).isEqualTo("webp");
		Assertions.assertThat(f1.compression).isTrue();
		Assertions.assertThat(f1.height).isEqualTo(256);
		Assertions.assertThat(f1.width).isEqualTo(256);
		
	}
	
	@Data
	@NoArgsConstructor
	public static class MediaFormats {
		private List<Format> formats;
	}
	
	@Data
	@NoArgsConstructor
	public static class Format {
		private String name;
		private String format;
		private boolean compression;
		private int width;
		private int height;
	}
}
