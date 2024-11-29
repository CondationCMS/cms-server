package com.condation.cms.templates.loaders;

import com.condation.cms.templates.TemplateLoader;
import com.condation.cms.templates.exceptions.TemplateNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class FileTemplateLoader implements TemplateLoader {

	private final Path basePath;
	
	@Override
	public String load(String template)  {
		try {
			var path = basePath.resolve(template);
			return Files.readString(path);
		} catch (Exception e) {
			throw new TemplateNotFoundException(e.getMessage());
		}
	}
	
}
