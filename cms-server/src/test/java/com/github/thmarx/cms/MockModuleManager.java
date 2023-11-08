/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 Marx-Software
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.github.thmarx.modules.api.ExtensionPoint;
import com.github.thmarx.modules.api.ManagerConfiguration;
import com.github.thmarx.modules.api.Module;
import com.github.thmarx.modules.api.ModuleDescription;
import com.github.thmarx.modules.api.ModuleManager;
import com.github.thmarx.modules.manager.ModuleImpl;
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
	public String installModule(URI uri) throws IOException {
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}

	@Override
	public boolean uninstallModule(String string, boolean bln) throws IOException {
		return true;
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
