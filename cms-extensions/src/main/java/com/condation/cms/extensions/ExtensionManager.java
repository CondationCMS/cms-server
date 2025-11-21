package com.condation.cms.extensions;

/*-
 * #%L
 * cms-extensions
 * %%
 * Copyright (C) 2023 - 2025 CondationCMS
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
import com.condation.cms.api.feature.features.HookSystemFeature;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.theme.Theme;
import com.condation.cms.extensions.request.RequestExtensions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.*;
import org.graalvm.polyglot.io.IOAccess;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public class ExtensionManager {

	private record CachedSource(Source source, long lastModified) {

	}

	private final ConcurrentHashMap<String, CachedSource> CACHE = new ConcurrentHashMap<>();
	private volatile ClassLoader cachedLibClassLoader;

	private final DB db;
	private final ServerProperties serverProperties;

	@Getter
	private final Engine engine;

	/**
	 * Build or reuse the classloader for extensions/libs.
	 */
	private ClassLoader getOrCreateLibClassLoader() throws IOException {
		if (cachedLibClassLoader != null) {
			return cachedLibClassLoader;
		}

		synchronized (this) {
			if (cachedLibClassLoader != null) {
				return cachedLibClassLoader;
			}

			Path libs = db.getFileSystem().resolve("libs/");
			List<URL> urls = new ArrayList<>();

			if (Files.exists(libs)) {
				try (var libsStream = Files.list(libs)) {
					libsStream.filter(f -> f.toString().endsWith(".jar"))
							.forEach(f -> {
								try {
									urls.add(f.toUri().toURL());
								} catch (Exception e) {
									log.error("Invalid JAR URL in libs/: {}", f, e);
								}
							});
				}
			}

			cachedLibClassLoader = new URLClassLoader(
					urls.toArray(new URL[0]),
					ClassLoader.getSystemClassLoader()
			);

			if (log.isDebugEnabled()) {
				log.debug("Loaded {} extension libraries.", urls.size());
			}
			return cachedLibClassLoader;
		}
	}

	/**
	 * Load extension JS files from a directory and apply loader callback.
	 */
	protected void loadExtensions(Path extPath, Consumer<Source> loader) throws IOException {
		if (!Files.exists(extPath)) {
			return;
		}

		try (var stream = Files.list(extPath)) {
			stream.filter(path -> !Files.isDirectory(path) && path.toString().endsWith(".js"))
					.forEach(jsFile -> loadSingleExtension(jsFile, loader));
		}
	}

	/**
	 * Load a single .js file with caching.
	 */
	private void loadSingleExtension(Path extFile, Consumer<Source> loader) {
		try {
			long lastModified = Files.getLastModifiedTime(extFile).toMillis();
			String key = extFile.toAbsolutePath().toString();

			CachedSource cached = CACHE.get(key);
			if (cached != null && cached.lastModified == lastModified) {
				loader.accept(cached.source());
				return;
			}

			if (log.isTraceEnabled()) {
				log.trace("Loading extension: {}", extFile.getFileName());
			}

			String code = Files.readString(extFile, StandardCharsets.UTF_8);

			Source src = Source.newBuilder("js", code, extFile.getFileName().toString())
					.encoding(StandardCharsets.UTF_8)
					.mimeType("application/javascript+module")
					.cached(true)
					.build();

			CACHE.put(key, new CachedSource(src, lastModified));
			loader.accept(src);

		} catch (Exception e) {
			log.error("Failed to load extension: {}", extFile, e);
		}
	}

	/**
	 * Create new execution context for request.
	 */
	public RequestExtensions newContext(Theme theme, RequestContext requestContext) throws IOException {

		ClassLoader libsClassLoader = getOrCreateLibClassLoader();

		Context context = Context.newBuilder("js")
				.option("js.ecmascript-version", "2025")
				.option("js.console", "false")
				.option("js.allow-eval", "false")
				.allowAllAccess(true) // TODO: reduce later
				.allowHostClassLookup(name -> true)
				.allowHostAccess(HostAccess.ALL)
				.allowValueSharing(true)
				.hostClassLoader(libsClassLoader)
				.allowIO(IOAccess.newBuilder()
						.fileSystem(new ExtensionFileSystem(
								db.getFileSystem().resolve("extensions/"), theme))
						.build())
				.engine(engine)
				.build();

		RequestExtensions requestExtensions = new RequestExtensions(context, libsClassLoader);

		setUpBindings(context.getBindings("js"), requestExtensions, theme, requestContext);

		for (Path p : resolveExtensionPaths(theme)) {
			if (log.isTraceEnabled()) {
				log.trace("Loading extensions from: {}", p);
			}
			loadExtensions(p, context::eval);
		}

		return requestExtensions;
	}

	/**
	 * Inject variables into JS bindings.
	 */
	private void setUpBindings(Value bindings, RequestExtensions reqExt,
			Theme theme, RequestContext reqCtx) {

		bindings.putMember("extensions", reqExt);
		bindings.putMember("fileSystem", db.getFileSystem());
		bindings.putMember("db", db);
		bindings.putMember("theme", theme);
		bindings.putMember("requestContext", reqCtx);
		bindings.putMember("ENV", serverProperties.env());

		// For backwards compatibility with old extensions & tests
		var hookSysFeature = reqCtx.get(HookSystemFeature.class);
		if (hookSysFeature != null) {
			bindings.putMember("hooks", hookSysFeature.hookSystem());
		}

	}

	/**
	 * Collect all extension paths for theme including parents.
	 */
	private List<Path> resolveExtensionPaths(Theme theme) {
		List<Path> paths = new ArrayList<>();

		// Global extensions
		paths.add(db.getFileSystem().resolve("extensions/"));

		// Theme hierarchy
		Theme current = theme;
		while (current != null && !current.empty()) {
			Path extPath = current.extensionsPath();
			if (extPath != null) {
				paths.add(extPath);
			}
			current = current.getParentTheme();
		}

		return paths;
	}
}
