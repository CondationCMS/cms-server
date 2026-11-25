package com.condation.cms.maven;

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

import com.condation.cms.maven.add.ExtensionGenerator;
import com.condation.cms.maven.add.ExtensionSpec;
import com.condation.cms.maven.add.ExtensionType;
import com.condation.cms.maven.add.ExtensionTypes;
import com.condation.cms.maven.add.JLinePrompter;
import com.condation.cms.maven.add.Names;
import com.condation.cms.maven.add.PackageDetector;
import com.condation.cms.maven.add.PromptUnavailableException;
import com.condation.cms.maven.add.Prompter;
import com.condation.cms.maven.add.Question;
import com.condation.cms.maven.add.UnavailablePrompter;
import com.condation.cms.maven.add.UserAbortException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Creates a new extension class in the module, asking for the details interactively.
 * Every value can also be given as property (for scripts and tests).
 */
@Mojo(name = "add", requiresDirectInvocation = true)
public class AddMojo extends AbstractCondationMojo {

    @Parameter(property = "condation.add.type")
    protected String addType;

    @Parameter(property = "condation.add.name")
    protected String addName;

    @Parameter(property = "condation.add.package")
    protected String addPackage;

    /** <code>key=value,key=value</code>, answers to the questions of the type. */
    @Parameter(property = "condation.add.options")
    protected String addOptions;

    /** Skips only this goal, independent of <code>condation.skip</code> which switches off the server goals. */
    @Parameter(property = "condation.add.skip", defaultValue = "false")
    protected boolean addSkip;

    @Parameter(property = "condation.add.overwrite", defaultValue = "false")
    protected boolean addOverwrite;

    @Parameter(defaultValue = "${project.build.sourceDirectory}", readonly = true)
    protected File sourceDirectory;

    @Parameter(defaultValue = "${project.groupId}", readonly = true)
    protected String groupId;

    @Parameter(defaultValue = "${session}", readonly = true)
    protected MavenSession session;

    @Parameter(defaultValue = "${project.packaging}", readonly = true)
    protected String packaging;

    /** Set by tests; otherwise created on demand. */
    protected Prompter prompter;

    private boolean ownsPrompter;

    @Override
    public void execute() throws MojoExecutionException {
        if (addSkip) {
            getLog().info("condation:add skipped");
            return;
        }
        if ("pom".equals(packaging)) {
            getLog().info("condation:add skipped for pom project, run it in the module project");
            return;
        }
        try {
            run();
        } catch (UserAbortException e) {
            throw new MojoExecutionException("Aborted, nothing was created");
        } catch (IllegalArgumentException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } finally {
            closeOwnPrompter();
        }
    }

    private void closeOwnPrompter() {
        if (ownsPrompter && prompter instanceof AutoCloseable closeable) {
            try {
                closeable.close();
            } catch (Exception e) {
                getLog().debug("Cannot close terminal", e);
            }
        }
    }

    private void run() throws MojoExecutionException {
        var type = resolveType();
        var source = sourceDirectory.toPath();

        var className = value(addName, "condation.add.name=<ClassName>",
                () -> prompter().ask("Class name", Names.defaultClassName(type),
                        s -> isValid(() -> Names.requireClassName(s)), "Not a valid Java class name"));
        Names.requireClassName(className);

        var packageName = value(addPackage, "condation.add.package=<package>",
                () -> prompter().ask("Package", PackageDetector.detect(source, groupId),
                        s -> isValid(() -> Names.requirePackage(s)), "Not a valid package"));
        Names.requirePackage(packageName);

        var spec = new ExtensionSpec(packageName, className, resolveOptions(type));

        var target = ExtensionGenerator.target(source, spec);
        var overwrite = addOverwrite;
        if (!overwrite && Files.exists(target)) {
            var question = source.relativize(target).toString().replace('\\', '/') + " already exists, overwrite?";
            overwrite = confirmOverwrite(question);
            if (!overwrite) {
                getLog().info("Nothing written, " + target + " already exists");
                return;
            }
        }
        try {
            var file = ExtensionGenerator.write(source, type, spec, overwrite);
            getLog().info("Created " + type.label() + " extension " + file);
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot write extension: " + e.getMessage(), e);
        }
    }

    private ExtensionType resolveType() throws MojoExecutionException {
        var ids = ExtensionTypes.all().stream().map(ExtensionType::id).toList();
        var id = value(addType, "condation.add.type=<" + String.join("|", ids) + ">",
                () -> prompter().select("Extension type", ids, ids.get(0)));
        return ExtensionTypes.byId(id).orElseThrow(() -> new MojoExecutionException(
                "Unknown extension type '" + id + "', known types: " + String.join(", ", ids)));
    }

    private Map<String, String> resolveOptions(ExtensionType type) throws MojoExecutionException {
        var given = parseOptions(addOptions);
        var known = type.questions().stream().map(Question::key).toList();
        for (var key : given.keySet()) {
            if (!known.contains(key)) {
                throw new MojoExecutionException("Unknown option '" + key + "' for type '" + type.id()
                        + "', known options: " + String.join(", ", known));
            }
        }
        var result = new LinkedHashMap<String, String>();
        for (var question : type.questions()) {
            var hint = "condation.add.options=" + question.key() + "=<value>"
                    + (question.choices().isEmpty() ? "" : " (one of: " + String.join(", ", question.choices()) + ")");
            var raw = value(given.get(question.key()), hint, () -> question.choices().isEmpty()
                    ? prompter().ask(question.label(), question.defaultValue(), question.validator(), question.errorMessage())
                    : prompter().select(question.label(), question.choices(), question.defaultValue()));
            result.put(question.key(), normalize(question, raw));
        }
        return result;
    }

    private static String normalize(Question question, String raw) {
        if (question.choices().isEmpty()) {
            if (!question.validator().test(raw)) {
                throw new IllegalArgumentException("Invalid value '" + raw + "' for '" + question.key() + "': "
                        + question.errorMessage());
            }
            return raw;
        }
        return question.choices().stream().filter(c -> c.equalsIgnoreCase(raw)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid value '" + raw + "' for '" + question.key()
                        + "', allowed: " + String.join(", ", question.choices())));
    }

    static Map<String, String> parseOptions(String options) {
        var result = new LinkedHashMap<String, String>();
        if (options == null || options.isBlank()) {
            return result;
        }
        for (var pair : options.split(",")) {
            var index = pair.indexOf('=');
            if (index <= 0) {
                throw new IllegalArgumentException("Invalid option '" + pair + "', expected key=value");
            }
            result.put(pair.substring(0, index).trim(), pair.substring(index + 1));
        }
        return result;
    }

    /** The given value, otherwise the answer of the prompt; without terminal an error naming the property. */
    private String value(String given, String hint, Supplier<String> prompt) throws MojoExecutionException {
        if (given != null && !given.isBlank()) {
            return given;
        }
        try {
            return prompt.get();
        } catch (PromptUnavailableException e) {
            throw new MojoExecutionException(e.getMessage() + ", set -D" + hint);
        }
    }

    private boolean confirmOverwrite(String question) throws MojoExecutionException {
        try {
            return prompter().confirm(question, false);
        } catch (PromptUnavailableException e) {
            throw new MojoExecutionException(e.getMessage() + ", set -Dcondation.add.overwrite=true");
        }
    }

    private static boolean isValid(Runnable check) {
        try {
            check.run();
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private Prompter prompter() {
        if (prompter == null) {
            prompter = createPrompter();
            ownsPrompter = true;
        }
        return prompter;
    }

    /** Prompts need a terminal and must not be used in batch mode (-B), CI jobs may have a pty. */
    protected boolean canPrompt(boolean terminalAvailable) {
        var interactive = session == null || session.getRequest() == null || session.getRequest().isInteractiveMode();
        return terminalAvailable && interactive;
    }

    /** JLine prompts when stdin/stdout are a terminal, otherwise every missing value is an error. */
    protected Prompter createPrompter() {
        if (canPrompt(JLinePrompter.terminalAvailable())) {
            try {
                return JLinePrompter.system();
            } catch (IOException | RuntimeException | LinkageError e) {
                getLog().debug("No JLine terminal", e);
            }
        }
        return new UnavailablePrompter();
    }
}
