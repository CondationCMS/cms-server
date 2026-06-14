package com.condation.cms.templates.perf;

/*-
 * #%L
 * CMS Templates
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

import com.condation.cms.api.Constants;
import com.condation.cms.api.cache.CacheManager;
import com.condation.cms.core.cache.LocalCacheProvider;
import com.condation.cms.templates.CMSTemplateEngine;
import com.condation.cms.templates.Template;
import com.condation.cms.templates.TemplateEngineFactory;
import com.condation.cms.templates.loaders.StringTemplateLoader;
import com.github.noconnor.junitperf.JUnitPerfInterceptor;
import com.github.noconnor.junitperf.JUnitPerfReportingConfig;
import com.github.noconnor.junitperf.JUnitPerfTest;
import com.github.noconnor.junitperf.JUnitPerfTestActiveConfig;
import com.github.noconnor.junitperf.JUnitPerfTestRequirement;
import com.github.noconnor.junitperf.reporting.providers.ConsoleReportGenerator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.Tag;

@Tag("performance")
@ExtendWith(JUnitPerfInterceptor.class)
public class TemplatePerformanceTest {

    @JUnitPerfTestActiveConfig
    static JUnitPerfReportingConfig perfConfig = JUnitPerfReportingConfig.builder()
                    .reportGenerator(new ConsoleReportGenerator())
                    .build();

    private static CMSTemplateEngine SUT;
    private static Template SIMPLE_TEMPLATE;
    private static Template COMPLEX_TEMPLATE;
    private static Map<String, Object> CONTEXT;

    @BeforeAll
    public static void setup() {
        StringTemplateLoader loader = new StringTemplateLoader()
                .add("simple", "Hello {{ name }}")
                .add("complex", """
                        {% if user.authenticated %}
                            <h1>Welcome, {{ user.name }}!</h1>
                            <ul>
                            {% for item in items %}
                                <li>{{ item.name | upper }}: {{ item.price }}</li>
                            {% endfor %}
                            </ul>
                        {% else %}
                            <p>Please log in.</p>
                        {% endif %}
                        """);

        SUT = TemplateEngineFactory
                .newInstance(loader, true)
                .cache(new LocalCacheProvider().getCache(Constants.CacheNames.TEMPLATE, new CacheManager.CacheConfig(100L, Duration.ofSeconds(60))))
                .defaultFilters()
                .defaultTags()
                .devMode(false)
                .create();

        SIMPLE_TEMPLATE = SUT.getTemplate("simple");
        COMPLEX_TEMPLATE = SUT.getTemplate("complex");

        CONTEXT = Map.of(
                "user", Map.of("authenticated", true, "name", "John Doe"),
                "items", java.util.List.of(
                        Map.of("name", "Product 1", "price", 10.5),
                        Map.of("name", "Product 2", "price", 20.0),
                        Map.of("name", "Product 3", "price", 15.75)
                )
        );
    }

    @Test
    @JUnitPerfTest(threads = 10, durationMs = 10_000, warmUpMs = 2_000)
    @JUnitPerfTestRequirement(percentiles = "90:100", meanLatency = 50)
    public void testSimpleTemplatePerformance() throws java.io.IOException {
        SIMPLE_TEMPLATE.evaluate(Map.of("name", "CondationCMS"));
    }

    @Test
    @JUnitPerfTest(threads = 10, durationMs = 10_000, warmUpMs = 2_000)
    @JUnitPerfTestRequirement(percentiles = "90:200", meanLatency = 100)
    public void testComplexTemplatePerformance() throws java.io.IOException {
        COMPLEX_TEMPLATE.evaluate(CONTEXT);
    }
}
