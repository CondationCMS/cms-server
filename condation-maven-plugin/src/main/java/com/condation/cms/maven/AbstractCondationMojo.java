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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;

/**
 * Shared parameters and helpers of the condation goals.
 */
public abstract class AbstractCondationMojo extends AbstractMojo {

    static final String MAIN_CLASS = "com.condation.cms.cli.CMSCli";

    @Parameter(property = "condation.home", defaultValue = "${project.basedir}/cms-home")
    protected File cmsHome;

    @Parameter(property = "condation.version", defaultValue = "${plugin.version}")
    protected String cmsVersion;

    @Parameter(property = "condation.moduleZip")
    protected File moduleZip;

    @Parameter(property = "condation.installModule", defaultValue = "true")
    protected boolean installModule;

    @Parameter(property = "condation.startTimeout", defaultValue = "60")
    protected int startTimeout;

    @Parameter(property = "condation.jvmArgs")
    protected List<String> jvmArgs;

    @Parameter(property = "condation.skip", defaultValue = "false")
    protected boolean skip;

    @Parameter(defaultValue = "${project.build.directory}", readonly = true)
    protected File buildDirectory;

    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    protected RepositorySystemSession repositorySession;

    @Parameter(defaultValue = "${project.remotePluginRepositories}", readonly = true)
    protected List<RemoteRepository> remoteRepositories;

    @Component
    protected RepositorySystem repositorySystem;

    protected Path requireHome() throws MojoExecutionException {
        var home = cmsHome.toPath();
        if (!Files.isDirectory(home)) {
            throw new MojoExecutionException("CMS home does not exist: " + home
                    + " - create it (server.toml, hosts/, themes/) or set -Dcondation.home=<dir>");
        }
        return home;
    }

    protected void requireServerConfig(Path home) throws MojoExecutionException {
        try {
            ServerPort.read(home);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    protected void ensureNotRunning(Path home) throws MojoExecutionException {
        if (ServerProcess.isRunning(home)) {
            throw new MojoExecutionException("A CMS server is already running in " + home
                    + " - stop it first with condation:stop");
        }
    }

    protected void installModuleIfRequested(Path home) throws MojoExecutionException {
        if (!installModule) {
            return;
        }
        try {
            var attached = project == null ? null : project.getAttachedArtifacts().stream()
                    .filter(a -> "bin".equals(a.getClassifier()) && "zip".equals(a.getType()))
                    .map(Artifact::getFile)
                    .filter(f -> f != null)
                    .map(File::toPath)
                    .findFirst().orElse(null);
            var zip = ModuleZipLocator.locate(moduleZip == null ? null : moduleZip.toPath(), attached,
                    buildDirectory == null ? Path.of("target") : buildDirectory.toPath());
            var id = ModuleInstaller.install(zip, home);
            getLog().info("Installed module '" + id + "' from " + zip + " into " + home.resolve("modules"));
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    protected ServerLaunch serverLaunch(Path home, Path logFile) throws MojoExecutionException {
        var classpath = ClasspathResolver.resolve(repositorySystem, repositorySession, remoteRepositories, cmsVersion);
        return new ServerLaunch(home, classpath, MAIN_CLASS, jvmArgs == null ? List.of() : jvmArgs, logFile);
    }
}
