package com.condation.cms.core.menu;

/*-
 * #%L
 * CMS Core
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.condation.cms.api.menu.Menu;
import com.condation.cms.api.menu.MenuItem;
import com.condation.cms.api.menu.MenuService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * YAML-backed menu storage scoped to one site directory.
 */
public class FileMenuService implements MenuService {

	private static final Pattern VALID_ID = Pattern.compile("[a-zA-Z0-9][a-zA-Z0-9_-]*");

	private final Path menusDirectory;

	public FileMenuService(Path hostBase) {
		this.menusDirectory = hostBase.resolve("config/menus").normalize();
	}

	@Override
	public synchronized List<Menu> list() throws IOException {
		if (!Files.isDirectory(menusDirectory)) {
			return List.of();
		}

		try (Stream<Path> paths = Files.list(menusDirectory)) {
			List<Path> menuFiles = paths
					.filter(Files::isRegularFile)
					.filter(path -> path.getFileName().toString().endsWith(".yaml"))
					.sorted(Comparator.comparing(path -> path.getFileName().toString()))
					.toList();

			List<Menu> menus = new ArrayList<>(menuFiles.size());
			for (Path menuFile : menuFiles) {
				menus.add(read(menuFile));
			}
			return List.copyOf(menus);
		}
	}

	@Override
	public synchronized Optional<Menu> get(String id) throws IOException {
		Path menuFile = resolve(id);
		if (!Files.isRegularFile(menuFile)) {
			return Optional.empty();
		}
		return Optional.of(read(menuFile));
	}

	@Override
	public synchronized Menu create(Menu menu) throws IOException {
		Menu normalizedMenu = normalize(menu);
		Path target = resolve(normalizedMenu.id());
		if (Files.exists(target)) {
			throw new FileAlreadyExistsException(target.toString());
		}
		write(target, normalizedMenu);
		return normalizedMenu;
	}

	@Override
	public synchronized Menu update(Menu menu) throws IOException {
		Menu normalizedMenu = normalize(menu);
		Path target = resolve(normalizedMenu.id());
		if (!Files.isRegularFile(target)) {
			throw new NoSuchFileException(target.toString());
		}
		write(target, normalizedMenu);
		return normalizedMenu;
	}

	@Override
	public synchronized boolean delete(String id) throws IOException {
		return Files.deleteIfExists(resolve(id));
	}

	private Path resolve(String id) {
		validateId(id);
		Path resolved = menusDirectory.resolve(id + ".yaml").normalize();
		if (!resolved.startsWith(menusDirectory)) {
			throw new IllegalArgumentException("Invalid menu id");
		}
		return resolved;
	}

	private Menu normalize(Menu menu) {
		if (menu == null) {
			throw new IllegalArgumentException("Menu is required");
		}
		validateId(menu.id());
		String name = menu.name() == null || menu.name().isBlank() ? menu.id() : menu.name().trim();
		return new Menu(menu.id(), name, menu.items());
	}

	private void validateId(String id) {
		if (id == null || !VALID_ID.matcher(id).matches()) {
			throw new IllegalArgumentException(
					"Menu id must only contain letters, numbers, hyphens and underscores");
		}
	}

	private Menu read(Path menuFile) throws IOException {
		String id = fileId(menuFile);
		String content = Files.readString(menuFile, StandardCharsets.UTF_8);
		if (content.isBlank()) {
			return new Menu(id, id, List.of());
		}

		Object loaded = createYaml().load(content);
		if (!(loaded instanceof Map<?, ?> document)) {
			throw new IOException("Invalid menu document: " + menuFile.getFileName());
		}

		String documentId = stringValue(document.get("id"), id);
		if (!id.equals(documentId)) {
			throw new IOException("Menu id does not match file name: " + menuFile.getFileName());
		}
		String name = stringValue(document.get("name"), id);
		return new Menu(id, name, readItems(document.get("items"), menuFile));
	}

	private List<MenuItem> readItems(Object value, Path menuFile) throws IOException {
		if (value == null) {
			return List.of();
		}
		if (!(value instanceof List<?> values)) {
			throw new IOException("Invalid menu items in " + menuFile.getFileName());
		}

		List<MenuItem> items = new ArrayList<>(values.size());
		for (Object itemValue : values) {
			if (!(itemValue instanceof Map<?, ?> item)) {
				throw new IOException("Invalid menu item in " + menuFile.getFileName());
			}
			items.add(new MenuItem(
					stringValue(item.get("id"), ""),
					stringValue(item.get("type"), "link"),
					stringValue(item.get("label"), ""),
					stringValue(item.get("url"), ""),
					stringValue(item.get("target"), "_self"),
					booleanValue(item.get("enabled"), true),
					readItems(item.get("children"), menuFile)));
		}
		return items;
	}

	private void write(Path target, Menu menu) throws IOException {
		Files.createDirectories(menusDirectory);
		Path temporaryFile = Files.createTempFile(menusDirectory, "." + menu.id() + "-", ".yaml.tmp");
		try {
			Files.writeString(temporaryFile, createYaml().dump(toDocument(menu)), StandardCharsets.UTF_8);
			try {
				Files.move(temporaryFile, target,
						StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
			} catch (AtomicMoveNotSupportedException ex) {
				Files.move(temporaryFile, target, StandardCopyOption.REPLACE_EXISTING);
			}
		} finally {
			Files.deleteIfExists(temporaryFile);
		}
	}

	private Map<String, Object> toDocument(Menu menu) {
		Map<String, Object> document = new LinkedHashMap<>();
		document.put("id", menu.id());
		document.put("name", menu.name());
		List<Map<String, Object>> items = new ArrayList<>();
		menu.items().forEach(item -> items.add(toDocument(item)));
		document.put("items", items);
		return document;
	}

	private Map<String, Object> toDocument(MenuItem item) {
		Map<String, Object> document = new LinkedHashMap<>();
		document.put("id", item.id());
		document.put("type", item.type());
		document.put("label", item.label());
		document.put("url", item.url());
		document.put("target", item.target());
		document.put("enabled", item.enabled());
		List<Map<String, Object>> children = new ArrayList<>();
		item.children().forEach(child -> children.add(toDocument(child)));
		document.put("children", children);
		return document;
	}

	private String fileId(Path menuFile) {
		String fileName = menuFile.getFileName().toString();
		return fileName.substring(0, fileName.length() - ".yaml".length());
	}

	private String stringValue(Object value, String fallback) {
		return value == null ? fallback : String.valueOf(value);
	}

	private boolean booleanValue(Object value, boolean fallback) {
		if (value instanceof Boolean bool) {
			return bool;
		}
		return value == null ? fallback : Boolean.parseBoolean(String.valueOf(value));
	}

	private Yaml createYaml() {
		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		options.setPrettyFlow(true);
		options.setIndent(2);
		return new Yaml(options);
	}
}
