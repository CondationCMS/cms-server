package com.condation.cms.modules.system.api;

/*-
 * #%L
 * cms-system-modules
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
import com.condation.cms.api.db.ContentNode;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.db.cms.ReadOnlyFile;
import com.condation.cms.api.utils.PathUtil;
import com.condation.cms.filesystem.metadata.AbstractMetaData;
import com.condation.cms.modules.system.api.handlers.v1.NavigationHandler;
import com.condation.cms.modules.system.api.helpers.NodeHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.eclipse.jetty.server.Request;

/**
 *
 * @author thorstenmarx
 */
@RequiredArgsConstructor
public class NavigationService {

	private final DB db;

	public Optional<NavNode> list(final String uri, final Request request) {
		final ReadOnlyFile contentBase = db.getReadOnlyFileSystem().contentBase();
		var file = contentBase.resolve(uri);

		if (!file.exists()) {
			return Optional.empty();
		}

		var filePath = PathUtil.toRelativeFile(file, contentBase);
		final Optional<ContentNode> contentNode = db.getContent().byUri(filePath);

		List<NavNode> children = new ArrayList<>();

		// Verzeichnisse mit (ggf. virtuellen) Knoten behandeln
		db.getContent().listDirectories(file, "").forEach(dir -> {
			ContentNode indexNode = dir.children().get("index.md");

			if (indexNode != null && AbstractMetaData.isVisible(indexNode)) {
				// Normales, sichtbares Verzeichnis mit index.md
				children.add(new NavNode(
						NodeHelper.getPath(indexNode),
						NodeHelper.getLinks(indexNode, request),
						Collections.emptyList()
				));
			} else {
				// Kein sichtbares index.md – prüfen, ob darunter etwas Sichtbares liegt
				boolean hasVisibleDescendants = dir.children().values()
						.stream()
						.anyMatch(AbstractMetaData::isVisible);

				if (hasVisibleDescendants) {
					// Virtueller Navigationsknoten für das Verzeichnis
					children.add(new NavNode(
							NodeHelper.getPath(dir),
							Collections.emptyMap(),
							Collections.emptyList()
					));
				}
			}
		});

		// Einzeldateien (nicht Verzeichnisse)
		db.getContent().listContent(file, "").stream()
				.filter(AbstractMetaData::isVisible)
				.filter(child -> !child.isDirectory())
				.filter(child -> !NodeHelper.getPath(child).equals(NodeHelper.getPath(uri)))
				.map(child -> new NavNode(
				NodeHelper.getPath(child),
				NodeHelper.getLinks(child, request),
				Collections.emptyList()
		))
				.forEach(children::add);

		children.sort((node1, node2) -> node1.path().compareTo(node2.path()));

		NavNode node;
		if (contentNode.isPresent()) {
			node = new NavNode(
					NodeHelper.getPath(contentNode.get()),
					NodeHelper.getLinks(contentNode.get(), request),
					children
			);
		} else {
			node = new NavNode(
					"/" + uri,
					Collections.emptyMap(),
					children
			);
		}

		return Optional.of(node);
	}

	public Optional<NavNode> list2(final String uri, final Request request) {
		final ReadOnlyFile contentBase = db.getReadOnlyFileSystem().contentBase();

		var file = contentBase.resolve(uri);

		if (!file.exists()) {
			return Optional.empty();
		}
		var filePath = PathUtil.toRelativeFile(file, contentBase);
		final Optional<ContentNode> contentNode = db.getContent().byUri(filePath);

		List<NavNode> children = new ArrayList<>();
		db.getContent().listDirectories(file, "").stream()
				.map(child -> {
					if (child.isDirectory()) {
						if (child.children().containsKey("index.md")) {
							return child.children().get("index.md");
						}
						return null;
					}
					return child;
				})
				.filter(child -> AbstractMetaData.isVisible(child))
				.filter(child -> child != null)
				.map(child -> new NavNode(
					NodeHelper.getPath(child),
					NodeHelper.getLinks(child, request))
				).forEach(children::add);

		final String nodeUri = NodeHelper.getPath(uri);
		db.getContent().listContent(file, "").stream()
				.filter(child -> AbstractMetaData.isVisible(child))
				.map(child -> {
					if (child.isDirectory()) {
						if (child.children().containsKey("index.md")) {
							return child.children().get("index.md");
						}
						return null;
					}
					return child;
				})
				.filter(child -> child != null)
				.filter(child
						-> !NodeHelper.getPath(child).equals(nodeUri)
				)
				.map(child -> new NavNode(
				NodeHelper.getPath(child),
				NodeHelper.getLinks(child, request))
				).forEach(children::add);

		children.sort((node1, node2) -> node1.path().compareTo(node2.path()));

		NavNode node;
		if (contentNode.isPresent()) {
			node = new NavNode(
					NodeHelper.getPath(contentNode.get()),
					NodeHelper.getLinks(contentNode.get(), request),
					children
			);
		} else {
			node = new NavNode(
					"/" + uri,
					Collections.emptyMap(),
					children
			);
		}

		return Optional.of(node);
	}
}
