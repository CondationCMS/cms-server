package com.github.thmarx.cms.filesystem;

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
import com.github.thmarx.cms.api.ModuleFileSystem;
import com.github.thmarx.cms.api.Constants;
import com.github.thmarx.cms.content.ContentParser;
import com.github.thmarx.cms.api.eventbus.EventBus;
import com.github.thmarx.cms.api.eventbus.events.ContentChangedEvent;
import com.github.thmarx.cms.api.eventbus.events.TemplateChangedEvent;
import com.github.thmarx.cms.api.utils.PathUtil;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
public class FileSystem implements ModuleFileSystem {

	private final Path hostBaseDirectory;
	private final EventBus eventBus;

	private MultiRootRecursiveWatcher fileWatcher;
	
	final ContentParser contentParser = new ContentParser(this);
	private Path contentBase;

	@Getter
	private final MetaData metaData = new MetaData();

	public boolean isVisible(final String uri) {
		var node = metaData.byUri(uri);
		if (node.isEmpty()) {
			return false;
		}
		var n = node.get();
		return n.isPublished() && !n.isHidden() && !n.isSection();
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

	public String loadContent(final Path file) throws IOException {
		return loadContent(file, StandardCharsets.UTF_8);
	}

	public List<String> loadLines(final Path file) throws IOException {
		return loadLines(file, StandardCharsets.UTF_8);
	}

	public String loadContent(final Path file, final Charset charset) throws IOException {
		return Files.readString(file, charset);
	}

	public List<String> loadLines(final Path file, final Charset charset) throws IOException {
		return Files.readAllLines(file, charset);
	}

	public List<MetaData.MetaNode> listDirectories(final Path base, final String start) {
		var startPath = base.resolve(start);
		String folder = PathUtil.toRelativePath(startPath, contentBase).toString();

		List<MetaData.MetaNode> nodes = new ArrayList<>();

		if ("".equals(folder)) {
			metaData.tree().values()
					.stream()
					.filter(node -> node.isDirectory())
					.forEach((node) -> {
						nodes.add(node);
					});
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

	public List<MetaData.MetaNode> listContent(final Path base, final String start) {
		var startPath = base.resolve(start);

		String folder = PathUtil.toRelativePath(startPath, contentBase).toString();

		List<MetaData.MetaNode> nodes = new ArrayList<>();

		if ("".equals(folder)) {
			return metaData.listChildren("");
		} else {
			return metaData.listChildren(folder);
		}

	}

	public List<MetaData.MetaNode> listSections(final Path contentFile) {
		String folder = PathUtil.toRelativePath(contentFile, contentBase).toString();
		String filename = contentFile.getFileName().toString();
		filename = filename.substring(0, filename.length() - 3);

		List<MetaData.MetaNode> nodes = new ArrayList<>();

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
			Optional<MetaData.MetaNode> findFolder = metaData.findFolder(folder);
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
		log.debug("update meta data for {}", file.toString());
		Map<String, Object> fileMeta = contentParser.parseMeta(file);

		var uri = PathUtil.toRelativeFile(file, contentBase);

		metaData.addFile(uri, fileMeta);
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
