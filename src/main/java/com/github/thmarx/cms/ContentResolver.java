/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms;

import com.google.common.base.Strings;
import com.sun.net.httpserver.HttpExchange;
import com.vladsch.flexmark.util.data.MutableDataSet;
import static com.github.thmarx.cms.Server.contentRenderer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class ContentResolver {

	private final Path contentBase;

	private final ContentRenderer contentRenderer;
	
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
			contentFile = contentPath.resolve("index.md");
		} else {
			var temp = contentBase.resolve(path + ".md");
			if (Files.exists(temp)) {
				contentFile = temp;
			} else {
				return Optional.empty();
			}
		}

		try {
			var content = contentRenderer.render(contentFile, context);
			return Optional.of(content);
		} catch (IOException ex) {
			ex.printStackTrace();
			return Optional.empty();
		}
	}
}
