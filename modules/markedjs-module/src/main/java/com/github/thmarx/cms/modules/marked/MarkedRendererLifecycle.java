package com.github.thmarx.cms.modules.marked;

/*-
 * #%L
 * example-module
 * %%
 * Copyright (C) 2023 Marx-Software
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
import com.github.thmarx.modules.api.ModuleLifeCycleExtension;
import com.github.thmarx.modules.api.annotation.Extension;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Engine;

/**
 *
 * @author t.marx
 */
@Extension(ModuleLifeCycleExtension.class)
@Slf4j
public class MarkedRendererLifecycle extends ModuleLifeCycleExtension {

	protected static Engine engine;

	

	@Override
	public void deactivate() {
		log.debug("destroy engine");
		engine.close(true);
	}

	@Override
	public void activate() {
		log.debug("create engine");
		engine = Engine.create();
	}

	@Override
	public void init() {
	}

	
}
