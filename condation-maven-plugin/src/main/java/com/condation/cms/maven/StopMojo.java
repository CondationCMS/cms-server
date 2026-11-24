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

import java.io.IOException;
import java.time.Duration;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Stops the CMS server started with <code>condation:start</code>.
 */
@Mojo(name = "stop", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST)
public class StopMojo extends AbstractCondationMojo {

    @Override
    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("condation:stop skipped");
            return;
        }
        var home = requireHome();
        if (!ServerProcess.isRunning(home)) {
            getLog().info("No CMS server running in " + home);
            return;
        }
        var launch = serverLaunch(home, home.resolve("logs/plugin-server.out"));
        try {
            ServerProcess.stop(launch, Duration.ofSeconds(startTimeout));
            getLog().info("CMS server stopped");
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot stop CMS server: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MojoExecutionException("Interrupted while stopping the CMS server", e);
        }
    }
}
