/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms.template.functions.navigation;

import com.github.thmarx.cms.ContentParser;
import com.github.thmarx.cms.filesystem.FileSystem;
import com.github.thmarx.cms.filesystem.MetaData;
import com.github.thmarx.cms.template.functions.AbstractCurrentNodeFunction;
import com.github.thmarx.cms.utils.NodeUtil;
import com.github.thmarx.cms.utils.PathUtil;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
public class NavigationFunction extends AbstractCurrentNodeFunction {

	private static final int DEFAULT_DEPTH = 0;

	public NavigationFunction(FileSystem fileSystem, Path currentNode, ContentParser contentParser) {
		super(fileSystem, currentNode, contentParser);
	}

	public List<NavNode> list(final String start) {
		return getNodes(start, DEFAULT_DEPTH);
	}

	public List<NavNode> list(final String start, final int depth) {
		return getNodes(start, depth);
	}

	private List<NavNode> getNodes(final String start, final int depth) {
		if (start.startsWith("/")) { // root
			return getNodesFromBase(fileSystem.resolve("content/"), start.substring(1), depth);
		} else if (start.equals(".")) { // current
			return getNodesFromBase(currentNode.getParent(), "", depth);
		} else if (start.startsWith("./")) { // subfolder of current
			return getNodesFromBase(currentNode.getParent(), start.substring(2), depth);
		}
		return Collections.emptyList();
	}

	public List<NavNode> getNodesFromBase(final Path base, final String start, final int depth) {
		try {
			final List<MetaData.MetaNode> navNodes = fileSystem.listContent(base, start);
			
			navNodes.sort((node1, node2) -> {
				var order1 = NodeUtil.getMenuOrder(node1);
				var order2 = NodeUtil.getMenuOrder(node2);
				
				int compare = Float.compare(order1, order2);
				
				if (compare == 0) {
					var name1 = NodeUtil.getName(node1);
					var name2 = NodeUtil.getName(node2);
					
					return name1.compareTo(name2);
				}
				
				return compare;	
			});
			
			
			final List<NavNode> nodes = new ArrayList<>();
			final Path contentBase = fileSystem.resolve("content/");
			navNodes.forEach((node) -> {
				var name = NodeUtil.getName(node);
				var path = contentBase.resolve(node.uri());
				final NavNode navNode = new NavNode(name, getUrl(path));
				if (isCurrentNode(path)) {
					navNode.setCurrent(true);
				}
				nodes.add(navNode);
			});
			return nodes;
		} catch (Exception ex) {
			log.error(null, ex);
		}
		return Collections.emptyList();
	}

	private boolean isCurrentNode(final Path node) {
		Path nodeIndex;
		if ("index.md".equals(node.getFileName().toString())) {
			nodeIndex = node;
		} else {
			nodeIndex = node.resolve("index.md");
		}
		return node.equals(currentNode) || currentNode.equals(nodeIndex);
	}
}
