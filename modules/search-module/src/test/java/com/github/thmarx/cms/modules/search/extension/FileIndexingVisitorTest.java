package com.github.thmarx.cms.modules.search.extension;

/*-
 * #%L
 * search-module
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
import com.github.thmarx.cms.api.utils.PathUtil;
import com.github.thmarx.cms.modules.search.IndexDocument;
import com.github.thmarx.cms.modules.search.SearchEngine;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class FileIndexingVisitorTest {

	public FileIndexingVisitorTest() {
	}

	@Test
	public void testSomeMethod() {
		var contentPath = Path.of("src/test/resources").resolve("content");
		try {
			Files.walkFileTree(contentPath, new TestFileVisitor(contentPath));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@RequiredArgsConstructor
	public class TestFileVisitor extends SimpleFileVisitor<Path> {

		private final Path contentBase;

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			if (Files.isDirectory(file)) {
				return FileVisitResult.CONTINUE;
			}
			try {
				var uri = PathUtil.toRelativeFile(file, contentBase);
				System.out.println(uri);

			} catch (Exception e) {
				e.printStackTrace();
			}

			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
			if (dir.getFileName().toString().startsWith(".")) {
				return FileVisitResult.SKIP_SUBTREE;
			}
			return FileVisitResult.CONTINUE;
		}
		
		

	}

}
