package com.condation.modules.manager;

/*-
 * #%L
 * modules-manager
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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thmarx
 */
public class Configuration {

	public enum Field {
		DB_DIR ("db.dir");
		
		private final String value;
		
		Field (final String value) {
			this.value = value;
		}
		
		public String value () {
			return value;
		}
	}
	
	public enum Stage {
		
		Production("production"),
		Development("development"),
		Test("test");
		
		private final String value;
		private Stage (final String value) {
			this.value = value;
		}
		
		public static Stage forStage (final String stage) {
			for (final Stage s : values()) {
				if (s.value.equals(stage)) {
					return s;
				}
			}
			throw new IllegalArgumentException("unknown stage: " + stage);
		}
	}
	
	private static final EnumMap<Stage, Configuration> configurations = new EnumMap<>(Stage.class);

	private final Properties properties = new Properties();
	
	private Configuration (){}
	
	public synchronized static Configuration empty () {
		return new Configuration();
	}
	
	public synchronized static Configuration getInstance(final File baseDir) {
		Configuration instance = new Configuration();
		
		try {
			instance.properties.load(new FileReader(new File(baseDir, "conf/configuration.properties")));
		} catch (FileNotFoundException ex) {
			Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
		}
		
		return instance;
	}
	
	public synchronized static Configuration getInstance(final Stage stage) {
		if (!configurations.containsKey(stage)) {
			Configuration INSTANCE = new Configuration();
			// loading resource using getResourceAsStream() method
			InputStream in = Configuration.class.getResourceAsStream("/configuration_" + stage.value + ".properties");
			try {
				INSTANCE.properties.load(in);
			} catch (IOException ex) {
				Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
			}
			configurations.put(stage, INSTANCE);
		}
		return configurations.get(stage);
	}
	
	public Object get (final String key, final Object defaultValue) {
		return properties.getOrDefault(key, defaultValue);
	}
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
	
	public void set (final String key, final Object value) {
		properties.put(key, value);
	}
}
