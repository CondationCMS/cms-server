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
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.theme.Theme;
import com.condation.cms.extensions.request.RequestExtensions;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
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
public class ExtensionManager {

	private final DB db;
	private final ServerProperties serverProperties;

	@Getter
	private final Engine engine;

	private ClassLoader getClassLoader() throws IOException {
		Path libs = db.getFileSystem().resolve("libs/");
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

	protected void loadExtensions(final Path extPath, final Consumer<Source> loader) throws IOException {
		if (!Files.exists(extPath)) {
			return;
		}
		Files.list(extPath)
				.filter(path -> !Files.isDirectory(path) && path.getFileName().toString().endsWith(".js"))
				.map(extFile -> {
					try {
						log.trace("load extension {}", extFile.getFileName().toString());
						return Source.newBuilder(
								"js",
								Files.readString(extFile, StandardCharsets.UTF_8),
								extFile.getFileName().toString() + ".mjs")
								.encoding(StandardCharsets.UTF_8)
								.cached(true)
								.build();
					} catch (IOException ex) {
						log.error("", ex);
					}
					return null;
				}).filter(source -> source != null)
				.forEach(loader);
	}

	public RequestExtensions newContext(Theme theme, RequestContext requestContext) throws IOException {
		var context = Context.newBuilder()
				.allowAllAccess(true)
				.allowHostClassLookup(className -> true)
				.allowHostAccess(HostAccess.ALL)
				.allowValueSharing(true)
				.hostClassLoader(getClassLoader())
				.allowIO(IOAccess.newBuilder()
						.fileSystem(new ExtensionFileSystem(db.getFileSystem().resolve("extensions/"), theme))
						.build())
				.engine(engine).build();

		RequestExtensions requestExtensions = new RequestExtensions(context);

		final Value bindings = context.getBindings("js");
		setUpBinding(bindings, requestExtensions, theme, requestContext);

		
		List<Path> extPaths = getExtensionPaths(theme);
		for (var extPath : extPaths) {
			log.debug("load extensions from " + extPath);
			loadExtensions(extPath, context::eval);
		}

		return requestExtensions;
	}

	private void setUpBinding(Value bindings,
			RequestExtensions requestExtensions, Theme theme, RequestContext requestContext) {
		bindings.putMember("extensions", requestExtensions);
		bindings.putMember("fileSystem", db.getFileSystem());
		bindings.putMember("db", db);
		bindings.putMember("theme", theme);
		// for backword compability
//		bindings.putMember("hooks", requestContext.get(HookSystemFeature.class).hookSystem());
		bindings.putMember("requestContext", requestContext);
		bindings.putMember("ENV", serverProperties.env());
	}

	private List<Path> getExtensionPaths(Theme theme) {
		var extPaths = new ArrayList<Path>();
		extPaths.add(db.getFileSystem().resolve("extensions/"));
	
		if (theme.getParentTheme() != null) {
			extPaths.add(theme.getParentTheme().extensionsPath());
		}
		if (!theme.empty() && theme.extensionsPath() != null) {
			extPaths.add(theme.extensionsPath());
		}
		return extPaths;
	}
}
