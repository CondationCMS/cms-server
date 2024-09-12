package com.condation.cms.core.configuration;

/*-
 * #%L
 * cms-core
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


import com.condation.cms.core.configuration.CMSConfiguration;
import com.condation.cms.core.configuration.ConfigProperties;
import java.nio.file.Path;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;


/**
 *
 * @author t.marx
 */
public class CMSConfigurationTest {
	

	@Test
	public void testSomeMethod() {
		CMSConfiguration configuration = new CMSConfiguration(Path.of("src/test/resources/config"));
		
		var myconfig = configuration.load("test.yaml", MYConfiguration.class);
		
		Assertions.assertThat(myconfig).isPresent();
		Assertions.assertThat(myconfig.get().getName()).isEqualTo("the name");
	}
	
	public static class MYConfiguration extends ConfigProperties {
		
		
		
		public MYConfiguration(Map<String, Object> properties) {
			super(properties);
		}
		
		public String getName () {
			return (String) getProperties().getOrDefault("name", "<name>");
		}
		
	}
}
