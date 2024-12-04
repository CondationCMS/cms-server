/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.condation.cms.benchmark;

/*-
 * #%L
 * benchmark
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

import com.condation.cms.api.cache.CacheManager;
import com.condation.cms.api.cache.CacheProvider;
import com.condation.cms.core.cache.LocalCacheProvider;
import com.condation.cms.templates.Template;
import com.condation.cms.templates.CMSTemplateEngine;
import com.condation.cms.templates.TemplateEngineFactory;
import com.condation.cms.templates.loaders.StringTemplateLoader;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

/**
 *
 * @author thmar
 */
// Benchmarks in JMH
@Fork(5)
@Warmup(iterations = 2)
@Measurement(iterations = 5)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class TemplateEngineBenchmark {

	private static final CacheProvider cacheProvider = new LocalCacheProvider();
	
	private CMSTemplateEngine engine; // Deine Template-Engine

	private Template simple;
	private Template page;
	private Template layout;
	
	@Setup(Level.Trial) // Einmalige Initialisierung vor allen Benchmarks
	public void setup() {
		var loader = new StringTemplateLoader();
		loader.add("simple", "Hallo, {{name}}!");
		loader.add("layout", """
                       {% block title%}
							the default title
                       {% endblock %}
                       {% block content%}
						the default content
					  {% endblock %}
                       """);
		
		loader.add("page", """
					   {% extends "layout" %}
                       {% block title%}
							the default title
                       {% endblock %}
                       {% block content%}
						the default content
					   {% endblock %}
                       """);
		
		engine = TemplateEngineFactory.newInstance(loader)
				.cache(cacheProvider.getCache("templates", new CacheManager.CacheConfig(100l, Duration.ofSeconds(60))))
				.defaultFilters()
				.defaultTags()
				.devMode(false)
				.create();
		
		simple = engine.getTemplate("simple");
		layout = engine.getTemplate("layout");
		page = engine.getTemplate("page");
	}

	@org.openjdk.jmh.annotations.Benchmark
	public String render_simple_template() throws IOException {
		Map<String, Object> data = Map.of("name", "Thorsten");
		return simple.evaluate(data);
	}
	@org.openjdk.jmh.annotations.Benchmark
	public String render_simple_layout() throws IOException {
		return page.evaluate();
	}
}
