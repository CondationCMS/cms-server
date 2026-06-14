package com.condation.cms.content.perf;

/*-
 * #%L
 * CMS Content
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

import com.condation.cms.content.markdown.CMSMarkdown;
import com.condation.cms.content.markdown.Options;
import com.github.noconnor.junitperf.JUnitPerfTest;
import com.github.noconnor.junitperf.JUnitPerfTestRequirement;
import com.github.noconnor.junitperf.reporting.providers.ConsoleReportGenerator;
import com.github.noconnor.junitperf.JUnitPerfReportingConfig;
import com.github.noconnor.junitperf.JUnitPerfTestActiveConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import com.github.noconnor.junitperf.JUnitPerfInterceptor;
import org.junit.jupiter.api.Tag;

@Tag("performance")
@ExtendWith(JUnitPerfInterceptor.class)
public class MarkdownPerformanceTest {

    @JUnitPerfTestActiveConfig
    static JUnitPerfReportingConfig perfConfig = JUnitPerfReportingConfig.builder()
                    .reportGenerator(new ConsoleReportGenerator())
                    .build();

    private static CMSMarkdown SUT;
    private static String LARGE_MD;

    @BeforeAll
    public static void setup() {
        SUT = new CMSMarkdown(Options.all());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append("# Heading ").append(i).append("\n\n");
            sb.append("This is some **bold** text and some *italic* text.\n\n");
            sb.append("A list:\n");
            sb.append("- Item 1\n");
            sb.append("- Item 2\n");
            sb.append("- Item 3\n\n");
            sb.append("> A blockquote with some content.\n\n");
            sb.append("```java\npublic class Test {\n  public static void main(String[] args) {\n    System.out.println(\"Hello\");\n  }\n}\n```\n\n");
            sb.append("Check out [CondationCMS](https://condation.com).\n\n");
            sb.append("--- \n\n");
        }
        LARGE_MD = sb.toString();
    }

    @Test
    @JUnitPerfTest(threads = 10, durationMs = 10_000, warmUpMs = 2_000, maxExecutionsPerSecond = 100)
    @JUnitPerfTestRequirement(maxLatency = 500, meanLatency = 200)
    public void testMarkdownRenderingPerformance() throws java.io.IOException {
        SUT.render(LARGE_MD);
    }
}
