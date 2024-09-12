package com.condation.cms.markdown.home;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 - 2024 CondationCMS
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



import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class RegexTest {

	@Test
	public void bold() {
		Pattern bold = Pattern.compile("\\*{2}(.*?)\\*{2}");

		var matcher = bold.matcher("Hallo **Leute**!");
		String replaceAll = matcher.replaceAll((result) -> "<strong>%s</strong>".formatted(result.group(1)));

		Assertions.assertThat(replaceAll).isEqualTo("Hallo <strong>Leute</strong>!");
	}

	@Test
	public void italic() {
		Pattern bold = Pattern.compile("\\_{2}(.*?)\\_{2}");

		var matcher = bold.matcher("Hallo __Leute__!");
		String replaceAll = matcher.replaceAll((result) -> "<i>%s</i>".formatted(result.group(1)));

		Assertions.assertThat(replaceAll).isEqualTo("Hallo <i>Leute</i>!");
	}
	
	@Test
	public void image() {
		Pattern image = Pattern.compile("!\\[(.*?)\\]\\((.*?)\\)");

		var matcher = image.matcher("![TestBild!](/assets/images/test.jpg)");
		String replaceAll = matcher.replaceAll((result) -> "<img src=\"%s\" alt=\"%s\" />".formatted(result.group(2), result.group(1)));
		Assertions.assertThat(replaceAll).isEqualTo("<img src=\"/assets/images/test.jpg\" alt=\"TestBild!\" />");
		
		image = Pattern.compile("!\\[(.*?)\\]\\((.*?) \"(.*?)\"\\)");
		matcher = image.matcher("![TestBild!](/assets/images/test.jpg \"Test bild\")");
		replaceAll = matcher.replaceAll((result) -> "<img src=\"%s\" alt=\"%s\" title=\"%s\" />".formatted(result.group(2), result.group(1), result.group(3)));
		Assertions.assertThat(replaceAll).isEqualTo("<img src=\"/assets/images/test.jpg\" alt=\"TestBild!\" title=\"Test bild\" />");
	}
	
	@Test
	public void home_renderer () throws IOException {
		HomeRenderer renderer = new HomeRenderer();
		renderer.add(new LineElement(Pattern.compile("\\*{2}(.*?)\\*{2}"), (result) -> "<strong>%s</strong>".formatted(result.group(1))));
		renderer.add(new LineElement(Pattern.compile("\\_{2}(.*?)\\_{2}"), (result) -> "<i>%s</i>".formatted(result.group(1))));
	
		String input = """
                 **fett**
                 __kursiv__
                 """;
		String expected = """
                <strong>fett</strong>
                <i>kursiv</i>
                 """;
		String result = renderer.render(new StringReader(input));
		
		Assertions.assertThat(result).isEqualToIgnoringWhitespace(expected);
	}

	public record LineElement (Pattern pattern, Function<MatchResult, String> replacer){}
	
	public static class HomeRenderer {
		private final List<LineElement> lineElements = new ArrayList<>();
		
		public void add (LineElement lineElement) {
			lineElements.add(lineElement);
		}
		
		public String render (final Reader reader) throws IOException {
			StringBuilder sb = new StringBuilder();
			try (BufferedReader br = new BufferedReader(reader);) {
				String line = null;
				while ((line = br.readLine()) != null) {
					for (var element : lineElements) {
						var matcher = element.pattern().matcher(line);
						if (matcher.find()) {
							line = matcher.replaceAll(element.replacer());
						}
					}
					sb.append(line);
					sb.append("\r\n");
				}
			}
			return sb.toString();
		}
	}
}
