package com.condation.cms.extensions;

/*-
 * #%L
 * cms-extensions
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


import com.condation.cms.api.hooks.HookSystem;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.graalvm.polyglot.Context;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class GlobalExtensions {
	
	private final HookSystem globalHookSystem;
	private final Context globalContext;
	
	public void init () throws IOException {
		globalContext.getBindings("js").putMember("hooks", globalHookSystem);
		try (InputStream resourceAsStream = ExtensionFileSystem.class.getResourceAsStream("global/hooks.global.mjs")) {
			String content = new String(resourceAsStream.readAllBytes(), StandardCharsets.UTF_8);
			globalContext.eval("js", content);
		}
	}
	
	public void evaluate (String scriptContent) {
		globalContext.eval("js", scriptContent);
	}
}
