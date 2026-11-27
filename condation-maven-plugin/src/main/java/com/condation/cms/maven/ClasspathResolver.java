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

import java.nio.file.Path;
import java.util.List;
import org.apache.maven.plugin.MojoExecutionException;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;

/**
 * Resolves cms-server with its runtime dependencies from the Maven repositories.
 */
public final class ClasspathResolver {

    private ClasspathResolver() {
    }

    public static List<Path> resolve(RepositorySystem system, RepositorySystemSession session,
            List<RemoteRepository> repositories, String cmsVersion) throws MojoExecutionException {
        var artifact = new DefaultArtifact("com.condation.cms", "cms-server", "jar", cmsVersion);
        var request = new CollectRequest(new Dependency(artifact, "runtime"), repositories);
        try {
            var result = system.resolveDependencies(session, new DependencyRequest(request, null));
            return result.getArtifactResults().stream()
                    .map(r -> r.getArtifact().getFile().toPath())
                    .toList();
        } catch (DependencyResolutionException e) {
            throw new MojoExecutionException("Cannot resolve com.condation.cms:cms-server:" + cmsVersion
                    + " - is it installed? (" + e.getMessage() + ")", e);
        }
    }
}
