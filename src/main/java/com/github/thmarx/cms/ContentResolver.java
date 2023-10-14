/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms;

import com.github.thmarx.cms.filesystem.FileSystem;
import com.github.thmarx.cms.filesystem.MetaData;
import com.github.thmarx.cms.utils.PathUtil;
import com.google.common.base.Strings;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
@Slf4j
public class ContentResolver {

	private final Path contentBase;

	private final ContentRenderer contentRenderer;
	
	private final FileSystem fileSystem;
	
	public Optional<String> getContent(final RenderContext context) {
		String path;
		if (Strings.isNullOrEmpty(context.uri())) {
			path = "";
		} else {
			path = context.uri().substring(1);
		}
		

		var contentPath = contentBase.resolve(path);
		Path contentFile = null;
		if (Files.exists(contentPath) && Files.isDirectory(contentPath)) {
			// use index.md
			var tempFile = contentPath.resolve("index.md");
			if (Files.exists(tempFile)) {
				contentFile = tempFile;
			}
		} else {
			var temp = contentBase.resolve(path + ".md");
			if (Files.exists(temp)) {
				contentFile = temp;
			} else {
				return Optional.empty();
			}
		}
		
		var uri = PathUtil.toFile(contentFile, contentBase);
		if (!fileSystem.isVisible(uri)) {
			return Optional.empty();
		}
		
		try {
			
			List<MetaData.MetaNode> sections = fileSystem.listSections(contentFile);
			
			Map<String, ContentRenderer.Section> renderedSections = contentRenderer.renderSections(sections, context);
			
			var content = contentRenderer.render(contentFile, context, renderedSections);
			return Optional.of(content);
		} catch (IOException ex) {
			log.error(null, ex);
			return Optional.empty();
		}
	}
}
