package com.condation.cms.filesystem;

/*-
 * #%L
 * cms-filesystem
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
import com.condation.cms.api.Constants;
import com.condation.cms.api.ModuleFileSystem;
import com.condation.cms.api.db.ContentNode;
import com.condation.cms.api.db.ContentQuery;
import com.condation.cms.api.db.DBFileSystem;
import com.condation.cms.api.eventbus.EventBus;
import com.condation.cms.api.eventbus.events.ContentChangedEvent;
import com.condation.cms.api.eventbus.events.InvalidateContentCacheEvent;
import com.condation.cms.api.eventbus.events.InvalidateTemplateCacheEvent;
import com.condation.cms.api.eventbus.events.ReIndexContentMetaDataEvent;
import com.condation.cms.api.eventbus.events.TemplateChangedEvent;
import com.condation.cms.api.exceptions.AccessNotAllowedException;
import com.condation.cms.api.utils.PathUtil;
import com.condation.cms.filesystem.metadata.AbstractMetaData;
import com.condation.cms.filesystem.metadata.memory.MemoryMetaData;
import com.condation.cms.filesystem.metadata.persistent.PersistentMetaData;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
@Slf4j
public class FileSystem implements ModuleFileSystem, DBFileSystem {

	private final Path hostBaseDirectory;
	private final EventBus eventBus;
	final Function<Path, Map<String, Object>> contentParser;

	private MultiRootRecursiveWatcher fileWatcher;
	private Path contentBase;

	@Getter
	private MetaData metaData;

	@Override
	public Path hostBase() {
		return hostBaseDirectory;
	}

	public <T> ContentQuery<T> query(final BiFunction<ContentNode, Integer, T> nodeMapper) {
		return metaData.query(nodeMapper);
	}

	public <T> ContentQuery<T> query(final String startURI, final BiFunction<ContentNode, Integer, T> nodeMapper) {
		return metaData.query(startURI, nodeMapper);
	}

	public boolean isVisible(final String uri) {
		var node = metaData.byUri(uri);
		if (node.isEmpty()) {
			return false;
		}
		var n = node.get();
		return AbstractMetaData.isVisible(n);
	}

	@Override
	public Optional<Map<String, Object>> getMeta(final String uri) {
		var node = metaData.byUri(uri);
		if (node.isEmpty()) {
			return Optional.empty();
		}
		var n = node.get();
		return Optional.of(n.data());
	}

	public void shutdown() {
		if (fileWatcher != null) {
			fileWatcher.stop();
		}
		if (metaData != null) {
			try {
				metaData.close();
			} catch (IOException ex) {
				log.error("", ex);
				throw new RuntimeException(ex);
			}
		}
	}

	@Override
	public Path resolve(String path) {
		final Path resolved = hostBaseDirectory.resolve(path);
		if (!PathUtil.isChild(hostBaseDirectory, resolved)) {
			throw new AccessNotAllowedException("access outside host package not allowed");
		}
		return resolved;
	}

	@Override
	public String loadContent(final Path file) throws IOException {
		return loadContent(file, StandardCharsets.UTF_8);
	}

	@Override
	public List<String> loadLines(final Path file) throws IOException {
		return loadLines(file, StandardCharsets.UTF_8);
	}

	@Override
	public String loadContent(final Path file, final Charset charset) throws IOException {
		return Files.readString(file, charset);
	}

	@Override
	public List<String> loadLines(final Path file, final Charset charset) throws IOException {
		return Files.readAllLines(file, charset);
	}

	public List<ContentNode> listDirectories(final Path base, final String start) {
		var startPath = base.resolve(start);
		String folder = PathUtil.toRelativePath(startPath, contentBase);

		return listDirectories(folder);
	}

	public List<ContentNode> listDirectories(final String folder) {
		List<ContentNode> nodes = new ArrayList<>();

		if ("".equals(folder)) {
			metaData.getTree().values()
					.stream()
					.filter(node -> node.isDirectory())
					.forEach((node) -> {
						nodes.add(node);
					});
		} else if (folder.contains("/")) {
			ContentNode node = null;
			var parts = folder.split("\\/");
			for (var part : parts) {
				if (node == null) {
					node = metaData.getTree().get(part);
				} else {
					node = node.children().get(part);
				}
			}
			if (node != null) {
				node.children().values()
						.stream()
						.filter(n -> n.isDirectory())
						.forEach((n) -> {
							nodes.add(n);
						});
			}
		} else {
			metaData.getTree().get(folder).children().values()
					.stream()
					.filter(node -> node.isDirectory())
					.forEach((node) -> {
						nodes.add(node);
					});
		}

		return nodes;
	}

	public List<ContentNode> listContent(final Path base, final String start) {
		var startPath = base.resolve(start);
		String folder = PathUtil.toRelativePath(startPath, contentBase);

		return listContent(folder);
	}

	public List<ContentNode> listContent(final String folder) {
		if ("".equals(folder)) {
			return metaData.listChildren("");
		} else {
			return metaData.listChildren(folder);
		}

	}

	public List<ContentNode> listSections(final Path contentFile) {
		String folder = PathUtil.toRelativePath(contentFile, contentBase);
		String filename = contentFile.getFileName().toString();
		filename = filename.substring(0, filename.length() - 3);

		return listSections(filename, folder);
	}

	public List<ContentNode> listSections(final String filename, String folder) {
		List<ContentNode> nodes = new ArrayList<>();

		final Pattern isSectionOf = Constants.SECTION_OF_PATTERN.apply(filename);
		final Pattern isNamedSectionOf = Constants.SECTION_NAMED_OF_PATTERN.apply(filename);

		if ("".equals(folder)) {
			metaData.getTree().values()
					.stream()
					.filter(node -> !node.isHidden())
					.filter(node -> node.isVisible())
					.filter(node -> node.isSection())
					.filter(node -> {
						return isSectionOf.matcher(node.name()).matches() || isNamedSectionOf.matcher(node.name()).matches();
					})
					.forEach((node) -> {
						nodes.add(node);
					});
		} else {
			Optional<ContentNode> findFolder = metaData.findFolder(folder);
			if (findFolder.isPresent()) {
				findFolder.get().children().values()
						.stream()
						.filter(node -> !node.isHidden())
						.filter(node -> node.isVisible())
						.filter(node -> node.isSection())
						.filter(node
								-> isSectionOf.matcher(node.name()).matches() || isNamedSectionOf.matcher(node.name()).matches()
						)
						.forEach((node) -> {
							nodes.add(node);
						});
			}
		}

		return nodes;
	}

	private void addOrUpdateMetaData(Path file) {
		addOrUpdateMetaData(file, false);
	}

	private void addOrUpdateMetaData(Path file, boolean batch) {
		try {
			if (!Files.exists(file)) {
				return;
			}
			if (!PathUtil.isContentFile(file)) {
				return;
			}
			log.debug("update meta data for {}", file.toString());
			Map<String, Object> fileMeta = contentParser.apply(file);

			var uri = PathUtil.toRelativeFile(file, contentBase);

			var lastModified = LocalDate.ofInstant(Files.getLastModifiedTime(file).toInstant(), ZoneId.systemDefault());

			metaData.addFile(uri, fileMeta, lastModified);
		} catch (Exception e) {
			log.error(null, e);
		}
	}

	public void init() throws IOException {
		init(MetaData.Type.MEMORY);
	}

	public void init(MetaData.Type metaDataType) throws IOException {
		log.debug("init filesystem");

		if (MetaData.Type.MEMORY.equals(metaDataType)) {
			this.metaData = new MemoryMetaData();
		} else {
			this.metaData = new PersistentMetaData(this.hostBaseDirectory);
		}
		this.metaData.open();

		this.contentBase = resolve("content/");
		var templateBase = resolve("templates/");
		log.debug("init filewatcher");
		this.fileWatcher = new MultiRootRecursiveWatcher(List.of(contentBase, templateBase));
		fileWatcher.getPublisher(contentBase).subscribe(new MultiRootRecursiveWatcher.AbstractFileEventSubscriber() {
			@Override
			public void onNext(FileEvent item) {
				try {

					if (item.file().isDirectory() || FileEvent.Type.DELETED.equals(item.type())) {
						swapMetaData();
					} else {
						addOrUpdateMetaData(item.file().toPath());
					}
				} catch (IOException ex) {
					log.error("", ex);
				}

				this.subscription.request(1);
			}
		});
		fileWatcher.getPublisher(templateBase).subscribe(new MultiRootRecursiveWatcher.AbstractFileEventSubscriber() {
			@Override
			public void onNext(FileEvent item) {
				try {
					eventBus.publish(new TemplateChangedEvent(item.file().toPath()));
					eventBus.publish(new InvalidateTemplateCacheEvent());
				} catch (Exception e) {
					log.error("", e);
				}
				this.subscription.request(1);
			}
		});

		reInitFolder(contentBase);

		fileWatcher.start();

		eventBus.register(ReIndexContentMetaDataEvent.class, (event) -> {
			try {
				if (event.uri() == null) {
					swapMetaData();
				} else {
					var contentFile = contentBase.resolve(event.uri());
					addOrUpdateMetaData(contentFile);
				}
			} catch (IOException ex) {
				log.error("error while reindex meta data", ex);
			}
		});
	}

	private void swapMetaData() throws IOException {
		log.debug("rebuild metadata");
		metaData.clear();
		reInitFolder(contentBase);
		eventBus.publish(new ContentChangedEvent(contentBase));
		eventBus.publish(new InvalidateContentCacheEvent());
	}

	private void reInitFolder(final Path folder) throws IOException {

		if (metaData instanceof PersistentMetaData pMetaData) {
			pMetaData.startBatch();
		}
		try {
			long before = System.currentTimeMillis();
			Files.walkFileTree(folder, new FileVisitor<Path>() {
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					var uri = PathUtil.toRelativePath(dir, contentBase);

					metaData.createDirectory(uri);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

					addOrUpdateMetaData(file);

					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					return FileVisitResult.CONTINUE;
				}
			});

			long after = System.currentTimeMillis();

			log.debug("loading metadata took " + (after - before) + "ms");
		} finally {
			if (metaData instanceof PersistentMetaData pMetaData) {
				pMetaData.stopBatch();
			}
		}
	}
}
