package com.condation.cms.configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author t.marx
 */
public class ConfigManagement {
	
	private final Map<Class<IConfiguration>, IConfiguration> configurations = new HashMap<>();

	public boolean has (Class<IConfiguration> configurationClass) {
		return configurations.containsKey(configurationClass);
	}
	
	public <T extends IConfiguration> Optional<T> get (Class<T> configurationClass) {
		return Optional.ofNullable((T)configurations.get(configurationClass));
	}
}
