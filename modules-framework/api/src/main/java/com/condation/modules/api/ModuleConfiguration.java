package com.condation.modules.api;

/*-
 * #%L
 * modules-api
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




import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author thmarx
 */
public class ModuleConfiguration {

	private final Properties properties = new Properties();
	
	final File configFile;
	
	final File dataDirectory;
	
	public ModuleConfiguration (final File path) throws IOException{
		configFile = new File(path, "configuration.properties");
		if (!configFile.exists()) {
			configFile.createNewFile();
		}
		properties.load(new FileReader(configFile));
		
		this.dataDirectory = new File(path.toURI());
	}
	
	public File getDataDir () {
		return dataDirectory;
	}
	
	
	/**
	 * Stores the module configuration in the data directory for this module.
	 * 
	 * @throws IOException 
	 */
	public void store () throws IOException {
		properties.store(new FileWriter(configFile), "module configuraion saved");
	}
	/**
	 * Returns a configuration property value.
	 * 
	 * @param key The key of the property.
	 * @param defaultValue The default value to return.
	 * @return The value for the key or the default value.
	 */
	public Object get (final String key, final Object defaultValue) {
		return properties.getOrDefault(key, defaultValue);
	}
	/**
	 * Returns a configuration property value as int.
	 * 
	 * @param key The key of the property.
	 * @param defaultValue The default value to return.
	 * @return The value for the key or the default value.
	 */
	public int getInt (final String key, final int defaultValue) {
		Object value = properties.get(key);
		if (value != null) {
			if (value instanceof Integer) {
				return (int) value;
			} else {
				return Integer.parseInt(((String)value).trim());
			}
		}
		return defaultValue;
	}
	/**
	 * Returns a configuration property value as String.
	 * 
	 * @param key The key of the property.
	 * @param defaultValue The default value to return.
	 * @return The value for the key or the default value.
	 */
	public String getString (final String key, final String defaultValue) {
		Object value = properties.get(key);
		if (value != null) {
			if (value instanceof String) {
				return (String) value;
			} else {
				return String.valueOf(value);
			}
		}
		return defaultValue;
	}
	
	/**
	 * Sets a configuration property.
	 * 
	 * @param key The property key.
	 * @param value The property value. 
	 */
	public void set (final String key, final Object value) {
		properties.put(key, value);
	}
}
