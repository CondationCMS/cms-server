package com.github.thmarx.cms.extensions;

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
import com.github.thmarx.cms.request.RequestExtensions;
import com.github.thmarx.cms.api.theme.Theme;
import com.github.thmarx.cms.filesystem.FileSystem;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.io.IOAccess;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
@Slf4j
public class ExtensionManager implements AutoCloseable {
	private final FileSystem fileSystem;
	private final Theme parentTheme;

	@Getter
	private Engine engine;

	List<Source> sources = new ArrayList<>();
	List<Source> theme_sources = new ArrayList<>();

	private ClassLoader getClassLoader() throws IOException {
		Path libs = fileSystem.resolve("libs/");
		List<URL> urls = new ArrayList<>();
		if (Files.exists(libs)) {
			Files.list(libs)
					.filter(path -> path.getFileName().toString().endsWith(".jar"))
					.forEach(path -> {
						try {
							urls.add(path.toUri().toURL());
						} catch (MalformedURLException ex) {
							log.error("", ex);
						}
					});
		}
		return new URLClassLoader(urls.toArray(URL[]::new), ClassLoader.getSystemClassLoader());
	}

	public void init() throws IOException {
		if (engine == null) {
			log.debug("init extensions");
			engine = Engine.newBuilder("js")
					.option("engine.WarnInterpreterOnly", "false")
					.build();

			if (!parentTheme.empty()) {
				var themeExtPath = parentTheme.extensionsPath();
				if (Files.exists(themeExtPath)) {
					log.debug("load extensions from theme");
					loadExtensions(themeExtPath, theme_sources);
				}
			}
			var extPath = fileSystem.resolve("extensions/");
			if (Files.exists(extPath)) {
				log.debug("load extensions from site");
				loadExtensions(extPath, sources);
			}
		}
	}

	protected void loadExtensions(Path extPath, List<Source> sources) throws IOException {
		Files.list(extPath)
				.filter(path -> !Files.isDirectory(path) && path.getFileName().toString().endsWith(".js"))
				.forEach(extFile -> {
					try {
						log.debug("load extension {}", extFile.getFileName().toString());
						Source source = Source.newBuilder(
								"js",
								Files.readString(extFile, StandardCharsets.UTF_8),
								extFile.getFileName().toString() + ".mjs")
								.encoding(StandardCharsets.UTF_8)
								.build();

						sources.add(source);
					} catch (IOException ex) {
						log.error("", ex);
					}
				});
	}
	

	public RequestExtensions newContext(Theme theme) throws IOException {
		var context = Context.newBuilder()
				.allowAllAccess(true)
				.allowHostClassLookup(className -> true)
				.allowHostAccess(HostAccess.ALL)
				.allowValueSharing(true)
				.hostClassLoader(getClassLoader())
				.allowIO(IOAccess.newBuilder()
						.fileSystem(new ExtensionFileSystem(fileSystem.resolve("extensions/")))
						.build())
				.engine(engine).build();

		Context themeContext = null;
		if (!theme.empty()) {
			themeContext = Context.newBuilder()
				.allowAllAccess(true)
				.allowHostClassLookup(className -> true)
				.allowHostAccess(HostAccess.ALL)
				.allowValueSharing(true)
				.hostClassLoader(getClassLoader())
				.allowIO(IOAccess.newBuilder()
						.fileSystem(new ExtensionFileSystem(theme.extensionsPath()))
						.build())
				.engine(engine).build();
		}

		RequestExtensions holder = new RequestExtensions(context, themeContext);

		final Value bindings = context.getBindings("js");
		bindings.putMember("extensions", holder);
		bindings.putMember("fileSystem", fileSystem);
		bindings.putMember("theme", theme);

		sources.forEach(context::eval);

		if (!theme.empty()) {
			final Value themeBindings = themeContext.getBindings("js");
			themeBindings.putMember("extensions", holder);
			themeBindings.putMember("fileSystem", fileSystem);
			themeBindings.putMember("theme", theme);

			theme_sources.forEach(themeContext::eval);
		}
		

		return holder;
	}

	@Override
	public void close() throws Exception {
		if (engine != null) {
			engine.close(true);
		}
	}

}
