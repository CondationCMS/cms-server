package com.condation.cms.templates.filter;

/*-
 * #%L
 * templates
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
import com.condation.cms.api.feature.features.MarkdownRendererFeature;
import com.condation.cms.api.markdown.MarkdownRenderer;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.request.RequestContextScope;
import com.condation.cms.templates.filter.impl.DateFilter;
import com.condation.cms.templates.filter.impl.MarkdownFilter;
import com.condation.cms.templates.filter.impl.RawFilter;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.text.StringEscapeUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class FilterTest {

	FilterRegistry registry = new FilterRegistry();
	FilterPipeline pipeline = new FilterPipeline(registry);

	@BeforeEach
	public void setup() {
		// Register filters
		registry.register("raw", (input, params) -> input); // Raw does nothing
		registry.register("truncate", (input, params) -> {
			if (input instanceof String stringValue) {
				int length = params.length > 0 ? (Integer) params[0] : stringValue.length();
				return stringValue.length() > length ? stringValue.substring(0, length) + "..." : input;
			}
			return input;
		});

		pipeline.addStep("raw");
		pipeline.addStep("truncate", 20);
	}

	@Test
	void test() {
		Object result = pipeline.execute("Dies ist ein langer Text, der abgeschnitten werden sollte.");
		Assertions.assertThat(result).isEqualTo("Dies ist ein langer ...");
	}

	@Test
	void date() {

		FilterRegistry registry = new FilterRegistry();
		FilterPipeline pipeline = new FilterPipeline(registry);
		registry.register(DateFilter.NAME, new DateFilter());

		pipeline.addStep("date");

		var date = new Date();
		SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
		var formatted = format.format(date);

		Object result = pipeline.execute(date);
		Assertions.assertThat(result).isEqualTo(formatted);
	}

	@Test
	void date_custom_format() {

		FilterRegistry registry = new FilterRegistry();
		FilterPipeline pipeline = new FilterPipeline(registry);
		registry.register(DateFilter.NAME, new DateFilter());

		pipeline.addStep("date", "MM/yyyy");

		var date = new Date();
		SimpleDateFormat format = new SimpleDateFormat("MM/yyyy");
		var formatted = format.format(date);

		Object result = pipeline.execute(date);
		Assertions.assertThat(result).isEqualTo(formatted);
	}

	@Test
	void raw() {
		FilterRegistry registry = new FilterRegistry();
		FilterPipeline pipeline = new FilterPipeline(registry);
		registry.register(RawFilter.NAME, new RawFilter());

		pipeline.addStep("raw");

		String input = """
                 <p>"We believe that great content is the foundation for successful communication and lasting connections. With Condation, we aim to create the base where ideas can grow and thrive. Our goal is to provide a solid, reliable platform that enables everyone to easily create, manage, and share their content—helping to change the world, one idea at a time."</p><p></p>
                 """;

		String escaped = StringEscapeUtils.ESCAPE_HTML4.translate(input);
		Object result = pipeline.execute(escaped);
		Assertions.assertThat(result).isEqualTo(input);
	}

	@Test
	void markdown() {
		FilterRegistry registry = new FilterRegistry();
		FilterPipeline pipeline = new FilterPipeline(registry);
		registry.register(MarkdownFilter.NAME, new MarkdownFilter());
		pipeline.addStep("markdown");

		var markdownRenderer = Mockito.mock(MarkdownRenderer.class);
		Mockito.when(markdownRenderer.render(Mockito.any())).thenReturn("");

		RequestContext context = new RequestContext();
		context.add(MarkdownRendererFeature.class, new MarkdownRendererFeature(markdownRenderer));
		ScopedValue.where(RequestContextScope.REQUEST_CONTEXT, context).run(() -> {
			String input = "input string";
			pipeline.execute(input);
			
			Mockito.verify(markdownRenderer, Mockito.times(1)).render(input);
		});
	}
}
