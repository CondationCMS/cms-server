package com.github.thmarx.cms.filesystem;

/*-
 * #%L
 * cms-server
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
import com.github.thmarx.cms.api.ModuleFileSystem;
import com.github.thmarx.cms.api.Constants;
import com.github.thmarx.cms.api.annotations.Experimental;
import com.github.thmarx.cms.api.db.ContentNode;
import com.github.thmarx.cms.api.db.DBFileSystem;
import com.github.thmarx.cms.api.eventbus.EventBus;
import com.github.thmarx.cms.api.eventbus.events.ContentChangedEvent;
import com.github.thmarx.cms.api.eventbus.events.TemplateChangedEvent;
import com.github.thmarx.cms.api.utils.PathUtil;
import com.github.thmarx.cms.filesystem.datafilter.dimension.Dimension;
import com.github.thmarx.cms.filesystem.query.Query;
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
import java.time.ZoneOffset;
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
	private final MetaData metaData = new MetaData();

	public <T> Query<T> query(final BiFunction<ContentNode, Integer, T> nodeMapper) {
		return new Query(new ArrayList<>(metaData.nodes().values()), nodeMapper);
	}

	public <T> Query<T> query(final String startURI, final BiFunction<ContentNode, Integer, T> nodeMapper) {

		final String uri;
		if (startURI.startsWith("/")) {
			uri = startURI.substring(1);
		} else {
			uri = startURI;
		}

		var nodes = metaData.nodes().values().stream().filter(node -> node.uri().startsWith(uri)).toList();

		return new Query(nodes, nodeMapper);
	}

	@Experimental
	protected <T> Dimension<T, ContentNode> createDimension(final String name, Function<ContentNode, T> dimFunc, Class<T> type) {
		return metaData.getDataFilter().dimension(name, dimFunc, type);
	}

	@Experimental
	protected Dimension<?, ContentNode> getDimension(final String name) {
		return metaData.getDataFilter().dimension(name);
	}

	public boolean isVisible(final String uri) {
		var node = metaData.byUri(uri);
		if (node.isEmpty()) {
			return false;
		}
		var n = node.get();
		return MetaData.isVisible(n);
	}
	
	@Override
	public Optional<Map<String,Object>> getMeta(final String uri) {
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
	}

	@Override
	public Path resolve(String path) {
		return hostBaseDirectory.resolve(path);
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
		String folder = PathUtil.toRelativePath(startPath, contentBase).toString();

		List<ContentNode> nodes = new ArrayList<>();

		if ("".equals(folder)) {
			metaData.tree().values()
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
					node = metaData.tree().get(part);
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
			metaData.tree().get(folder).children().values()
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

		String folder = PathUtil.toRelativePath(startPath, contentBase).toString();

		List<ContentNode> nodes = new ArrayList<>();

		if ("".equals(folder)) {
			return metaData.listChildren("");
		} else {
			return metaData.listChildren(folder);
		}

	}

	public List<ContentNode> listSections(final Path contentFile) {
		String folder = PathUtil.toRelativePath(contentFile, contentBase).toString();
		String filename = contentFile.getFileName().toString();
		filename = filename.substring(0, filename.length() - 3);

		List<ContentNode> nodes = new ArrayList<>();

		final Pattern isSectionOf = Constants.SECTION_OF_PATTERN.apply(filename);
		final Pattern isOrderedSectionOf = Constants.SECTION_ORDERED_OF_PATTERN.apply(filename);

		if ("".equals(folder)) {
			metaData.tree().values()
					.stream()
					.filter(node -> !node.isHidden())
					.filter(node -> node.isPublished())
					.filter(node -> node.isSection())
					.filter(node -> {
						return isSectionOf.matcher(node.name()).matches() || isOrderedSectionOf.matcher(node.name()).matches();
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
						.filter(node -> node.isPublished())
						.filter(node -> node.isSection())
						.filter(node
								-> isSectionOf.matcher(node.name()).matches()
						|| isOrderedSectionOf.matcher(node.name()).matches()
						)
						.forEach((node) -> {
							nodes.add(node);
						});
			}

		}

		return nodes;
	}

	private void addOrUpdateMetaData(Path file) throws IOException {
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
	}

	public void init() throws IOException {
		log.debug("init filesystem");

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
				eventBus.publish(new TemplateChangedEvent(item.file().toPath()));
			}
		});

		reInitFolder(contentBase);

		fileWatcher.start();
	}

	private void swapMetaData() throws IOException {
		log.debug("rebuild metadata");
		metaData.clear();
		reInitFolder(contentBase);
		eventBus.publish(new ContentChangedEvent(contentBase));
	}

	private void reInitFolder(final Path folder) throws IOException {

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
	}
}
