package com.condation.cms.configuration;

import com.condation.cms.api.eventbus.EventBus;
import com.condation.cms.configuration.source.TomlConfigSource;
import com.condation.cms.configuration.source.YamlConfigSource;
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
public class TaxonomyConfigurationTest {
	
	TaxonomyConfiguration configuration;
	
	@Mock
	EventBus eventBus;
	
	@BeforeEach
	public void setup() throws IOException, SchedulerException {
		configuration = TaxonomyConfiguration.builder(eventBus)
				.id("taxonomy-config")
				.addSource(YamlConfigSource.build(Path.of("configs/taxonomy.yaml")))
				.addSource(TomlConfigSource.build(Path.of("configs/taxonomy.toml")))
				.build();
	}

	@Test
	public void testSomeMethod() {
		var taxonomies = configuration.getTaxonomies();
		
		Assertions.assertThat(taxonomies)
				.hasSize(2)
				.containsKey("tags");
		Assertions.assertThat(taxonomies.get("tags").getValues()).hasSize(3);
	}
	
}
