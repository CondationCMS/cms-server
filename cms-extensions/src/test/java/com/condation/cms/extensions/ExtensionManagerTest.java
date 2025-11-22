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

import com.condation.cms.api.ServerProperties;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.feature.features.AuthFeature;
import com.condation.cms.api.feature.features.HookSystemFeature;
import com.condation.cms.api.hooks.HookSystem;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.theme.Theme;
import com.condation.cms.filesystem.FileSystem;
import java.io.IOException;
import java.nio.file.Path;
import org.assertj.core.api.Assertions;
import org.graalvm.polyglot.Engine;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 *
 * @author t.marx
 */
@ExtendWith(MockitoExtension.class)
public class ExtensionManagerTest {

	@Mock
	DB db;
	@Mock
	Theme theme;
	@Mock
	ServerProperties properties;
	@Mock
	FileSystem fileSystem;

	ExtensionManager extensionManager;

	static Engine engine;

	@BeforeAll
	public static void initEngine() {
		engine = Engine.newBuilder("js")
				.option("engine.WarnInterpreterOnly", "false")
				.build();
	}
	@AfterAll
	public static void shutdown() throws Exception {
		engine.close(true);
	}
	
	@BeforeEach
	public void setup() throws Exception {

		Mockito.when(fileSystem.resolve("libs/"))
				.thenReturn(Path.of("src/test/resources/site/libs"));
		Mockito.when(fileSystem.resolve("extensions/"))
				.thenReturn(Path.of("src/test/resources/site/extensions"));
		Mockito.when(db.getFileSystem()).thenReturn(fileSystem);
		Mockito.when(theme.extensionsPath())
				.thenReturn(Path.of("src/test/resources/theme/extensions"));

		extensionManager = new ExtensionManager(db, properties, engine);
	}

	@Test
	public void test_with_auth() throws IOException {

		var requestContext = new RequestContext();
		final HookSystem hookSystem = new HookSystem();
		requestContext.add(HookSystemFeature.class, new HookSystemFeature(hookSystem));
		requestContext.add(AuthFeature.class, new AuthFeature("thorsten"));
		extensionManager.newContext(theme, requestContext);

		Assertions.assertThat(hookSystem.execute("test").results())
				.hasSize(1)
				.containsExactly("Hallo thorsten");
	}

	@Test
	public void test_without_auth() throws IOException {

		var requestContext = new RequestContext();
		final HookSystem hookSystem = new HookSystem();
		requestContext.add(HookSystemFeature.class, new HookSystemFeature(hookSystem));
		extensionManager.newContext(theme, requestContext);

		Assertions.assertThat(hookSystem.execute("test").results())
				.hasSize(1)
				.containsExactly("Guten Tag");
	}
}
