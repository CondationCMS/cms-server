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



import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author thmarx
 */
public class ManagerConfiguration {
	
	private ConcurrentMap<String, ModuleConfig> modules = new ConcurrentHashMap<>();

	public ManagerConfiguration () {
		
	}

	public ConcurrentMap<String, ModuleConfig> getModules() {
		return modules;
	}

	public void setModules(ConcurrentMap<String, ModuleConfig> modules) {
		this.modules = modules;
	}
	
	public ModuleConfig get (final String moduleId) {
		return modules.get(moduleId);
	}
	public void add (final ModuleConfig config) {
		modules.put(config.getId(), config);
	}
	public void remove (final String moduleId) {
		modules.remove(moduleId);
	}

	public static class ModuleConfig {
		
		private boolean active = false;
		
		private String id;
		
		private String moduleDir;
		
		private String moduleDataDir;
		
		public ModuleConfig () {
			
		}
		public ModuleConfig (final String id) {
			this.id = id;
		}

		public void setId (final String id) {
			this.id = id;
		}
		
		public String getId () {
			return id;
		}
		
		public boolean isActive() {
			return active;
		}

		public void setActive(boolean active) {
			this.active = active;
		}

		public String getModuleDir() {
			return moduleDir;
		}

		public ModuleConfig setModuleDir(String moduleDir) {
			this.moduleDir = moduleDir;
			return this;
		}

		public String getModuleDataDir() {
			return moduleDataDir;
		}

		public ModuleConfig setModuleDataDir(String moduleDataDir) {
			this.moduleDataDir = moduleDataDir;
			
			return this;
		}
		
		

		@Override
		public int hashCode() {
			int hash = 3;
			hash = 37 * hash + Objects.hashCode(this.id);
			return hash;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final ModuleConfig other = (ModuleConfig) obj;
			if (!Objects.equals(this.id, other.id)) {
				return false;
			}
			return true;
		}
		
		
	}
}
