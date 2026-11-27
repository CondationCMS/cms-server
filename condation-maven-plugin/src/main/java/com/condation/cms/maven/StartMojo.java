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
 * Starts the CMS server in the background and waits until it is ready.
 */
@Mojo(name = "start", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST)
public class StartMojo extends AbstractCondationMojo {

    @Override
    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("condation:start skipped");
            return;
        }
        var home = requireHome();
        ensureNotRunning(home);
        requireServerConfig(home);
        installModuleIfRequested(home);

        var logFile = home.resolve("logs/plugin-server.out");
        var launch = serverLaunch(home, logFile);
        try {
            ServerProcess.startAndAwait(launch, Duration.ofSeconds(startTimeout));
            getLog().info("CMS server started on port " + ServerPort.read(home) + " (log: " + logFile + ")");
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot start CMS server: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MojoExecutionException("Interrupted while waiting for the CMS server", e);
        }
    }
}
