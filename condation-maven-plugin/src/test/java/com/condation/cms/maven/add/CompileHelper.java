package com.condation.cms.maven.add;

/*-
 * #%L
 * CondationCMS Maven Plugin
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

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.nio.file.Files;
import java.util.List;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

/**
 * Compiles a generated source against the test classpath (cms-api, modules-api, jetty).
 */
public final class CompileHelper {

    private CompileHelper() {
    }

    public static void compile(String packageName, String className, String source) {
        var compiler = ToolProvider.getSystemJavaCompiler();
        var diagnostics = new DiagnosticCollector<JavaFileObject>();
        var file = new SimpleJavaFileObject(
                URI.create("string:///" + packageName.replace('.', '/') + "/" + className + ".java"),
                JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return source;
            }
        };
        try {
            var out = Files.createTempDirectory("compile-helper");
            var task = compiler.getTask(new StringWriter(), null, diagnostics,
                    List.of("-proc:none", "-d", out.toString(), "-classpath", System.getProperty("java.class.path")),
                    null, List.of(file));
            if (!task.call()) {
                var message = new StringBuilder("Generated source does not compile:\n");
                diagnostics.getDiagnostics().forEach(d -> message.append(d).append('\n'));
                message.append("---- source ----\n").append(source);
                throw new AssertionError(message.toString());
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
