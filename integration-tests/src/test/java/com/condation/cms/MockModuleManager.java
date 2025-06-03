package com.condation.cms;

/*-
 * #%L
 * cms-server
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


import com.condation.modules.api.ExtensionPoint;
import com.condation.modules.api.ManagerConfiguration;
import com.condation.modules.api.Module;
import com.condation.modules.api.ModuleDescription;
import com.condation.modules.api.ModuleManager;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author thmar
 */
public class MockModuleManager implements ModuleManager {

	public MockModuleManager() {
	}

	@Override
	public boolean activateModule(String string) throws IOException {
		return true;
	}

	@Override
	public boolean deactivateModule(String string) throws IOException {
		return true;
	}

	@Override
	public ModuleDescription description(String string) throws IOException {
		return new ModuleDescription();
	}

	@Override
	public <T extends ExtensionPoint> List<T> extensions(Class<T> type) {
		return Collections.emptyList();
	}

	@Override
	public Module module(String string) {
		return null;
	}

	@Override
	public ManagerConfiguration configuration() {
		return null;
	}

	@Override
	public List<String> getModuleIds() {
		return Collections.emptyList();
	}

	@Override
	public void close() throws Exception {
	}

	@Override
	public void initModules() {
		
	}
	
}
