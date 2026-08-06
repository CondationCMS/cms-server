package com.condation.cms.core.theme;

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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.condation.cms.core.messages.EmptyMessageSource;
import java.nio.file.Path;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DefaultThemeTest {

	@TempDir
	private Path tempDir;

	@Test
	void resolveFallsBackToParentWhenOverrideIsMissing() {
		var theme = new DefaultTheme(null, new EmptyThemeProperties(Map.of()), new EmptyMessageSource());

		Assertions.assertThat(theme.resolve("example.js", null, tempDir))
				.isEqualTo(tempDir.resolve("example.js"));
	}

	@Test
	void resolveReturnsNullWhenOverrideAndParentAreMissing() {
		var theme = new DefaultTheme(null, new EmptyThemeProperties(Map.of()), new EmptyMessageSource());

		Assertions.assertThat(theme.resolve("example.js", null, null)).isNull();
	}
}
