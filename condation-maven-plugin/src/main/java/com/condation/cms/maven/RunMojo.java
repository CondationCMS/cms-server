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
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Runs the CMS server in the foreground, logging to the console. Ctrl+C stops it.
 */
@Mojo(name = "run")
public class RunMojo extends AbstractCondationMojo {

    @Override
    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("condation:run skipped");
            return;
        }
        var home = requireHome();
        ensureNotRunning(home);
        requireServerConfig(home);
        installModuleIfRequested(home);

        var launch = serverLaunch(home, null);
        try {
            var process = ServerProcess.launch(launch, "server", "start");
            var stoppedByHook = new AtomicBoolean(false);
            var hook = new Thread(() -> {
                stoppedByHook.set(true);
                try {
                    ServerProcess.stop(launch, Duration.ofSeconds(30));
                    // the server may not have written cms.pid yet (still starting): stop the process itself
                    if (process.isAlive()) {
                        ServerProcess.terminate(process);
                    }
                } catch (IOException | InterruptedException e) {
                    // shutting down anyway
                }
            }, "condation-stop");
            Runtime.getRuntime().addShutdownHook(hook);
            getLog().info("CMS server running, press Ctrl+C to stop");
            var exit = process.waitFor();
            try {
                Runtime.getRuntime().removeShutdownHook(hook);
            } catch (IllegalStateException _) {
                // already shutting down
            }
            if (isFailure(exit, stoppedByHook.get())) {
                throw new MojoExecutionException("CMS server terminated with exit code " + exit);
            }
            getLog().info("CMS server exited with code " + exit);
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot start CMS server: " + e.getMessage(), e);
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Exit codes 130/143 (SIGINT/SIGTERM) and exits caused by our own stop request are regular shutdowns.
     */
    static boolean isFailure(int exitCode, boolean stoppedByHook) {
        return exitCode != 0 && exitCode != 130 && exitCode != 143 && !stoppedByHook;
    }
}
