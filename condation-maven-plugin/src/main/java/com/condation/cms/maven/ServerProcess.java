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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Forks, observes and stops the CMS server process.
 */
public final class ServerProcess {

    private static final String PID_FILE = "cms.pid";

    private ServerProcess() {
    }

    /** The java binary of the running JDK, with the explicit <code>.exe</code> on Windows. */
    static Path javaExecutable(String osName, String javaHome) {
        var windows = osName != null && osName.toLowerCase().contains("win");
        return Path.of(javaHome, "bin", windows ? "java.exe" : "java");
    }

    public static Process launch(ServerLaunch launch, String... cliArgs) throws IOException {
        var home = launch.cmsHome().toAbsolutePath();
        var cmd = new ArrayList<String>();
        cmd.add(javaExecutable(System.getProperty("os.name"), System.getProperty("java.home")).toString());
        // relative to the working directory: CMSCli cannot handle an absolute cms.home
        cmd.add("-Dcms.home=.");
        if (launch.jvmArgs() != null) {
            cmd.addAll(launch.jvmArgs());
        }
        cmd.add("-cp");
        cmd.add(launch.classpath().stream().map(Path::toString).collect(Collectors.joining(File.pathSeparator)));
        cmd.add(launch.mainClass());
        cmd.addAll(java.util.List.of(cliArgs));

        var builder = new ProcessBuilder(cmd).directory(home.toFile());
        if (launch.logFile() == null) {
            builder.inheritIO();
        } else {
            Files.createDirectories(launch.logFile().toAbsolutePath().getParent());
            builder.redirectErrorStream(true);
            builder.redirectOutput(ProcessBuilder.Redirect.appendTo(launch.logFile().toFile()));
        }
        return builder.start();
    }

    /**
     * Launches <code>server start</code> and waits until it is ready. A process that does not
     * become ready is terminated, so a failed start never leaves an orphaned server behind.
     */
    public static Process startAndAwait(ServerLaunch launch, Duration timeout)
            throws IOException, InterruptedException, MojoExecutionException {
        var process = launch(launch, "server", "start");
        try {
            awaitReady(process, launch.cmsHome(), timeout, launch.logFile());
            return process;
        } catch (MojoExecutionException | InterruptedException | RuntimeException e) {
            terminate(process);
            throw e;
        }
    }

    /**
     * Terminates the process and its children: graceful first, forcibly after a grace period.
     */
    public static void terminate(Process process) throws InterruptedException {
        process.descendants().forEach(ProcessHandle::destroy);
        process.destroy();
        if (!process.waitFor(5, TimeUnit.SECONDS)) {
            process.descendants().forEach(ProcessHandle::destroyForcibly);
            process.destroyForcibly();
            process.waitFor(5, TimeUnit.SECONDS);
        }
    }

    public static void awaitReady(Process process, Path cmsHome, Duration timeout, Path logFile)
            throws MojoExecutionException, InterruptedException {
        var deadline = System.nanoTime() + timeout.toNanos();
        var logHint = logFile == null ? "" : " (see " + logFile + ")";
        while (System.nanoTime() < deadline) {
            if (!process.isAlive()) {
                throw new MojoExecutionException("CMS server terminated with exit code "
                        + process.exitValue() + " before it was ready" + logHint);
            }
            if (Files.exists(cmsHome.resolve(PID_FILE)) && portOpen(cmsHome)) {
                return;
            }
            Thread.sleep(200);
        }
        throw new MojoExecutionException("CMS server did not become ready within "
                + timeout.toSeconds() + "s" + logHint);
    }

    private static boolean portOpen(Path cmsHome) throws MojoExecutionException {
        int port;
        try {
            port = ServerPort.read(cmsHome);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
        try (var socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", port), 500);
            return true;
        } catch (IOException _) {
            return false;
        }
    }

    public static boolean isRunning(Path cmsHome) {
        return serverProcess(cmsHome).isPresent();
    }

    private static Optional<ProcessHandle> serverProcess(Path cmsHome) {
        var pidFile = cmsHome.resolve(PID_FILE);
        if (!Files.exists(pidFile)) {
            return Optional.empty();
        }
        var props = new Properties();
        try (var in = Files.newBufferedReader(pidFile)) {
            props.load(in);
            var pid = Long.parseLong(props.getProperty("pid"));
            var startedAt = Long.parseLong(props.getProperty("startedAt"));
            return ProcessHandle.of(pid)
                    .filter(ProcessHandle::isAlive)
                    .filter(h -> h.info().startInstant().map(i -> i.toEpochMilli() == startedAt).orElse(false));
        } catch (IOException | RuntimeException _) {
            return Optional.empty();
        }
    }

    public static void stop(ServerLaunch launch, Duration timeout) throws IOException, InterruptedException {
        var home = launch.cmsHome();
        var server = serverProcess(home);
        if (server.isEmpty()) {
            return;
        }
        var handle = server.get();

        var stopper = launch(launch, "server", "stop");
        if (!stopper.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
            stopper.destroyForcibly();
        }
        if (!waitUntilGone(handle, timeout)) {
            handle.destroy();
            if (!waitUntilGone(handle, Duration.ofSeconds(5))) {
                handle.destroyForcibly();
                waitUntilGone(handle, Duration.ofSeconds(5));
            }
        }
        Files.deleteIfExists(home.resolve(PID_FILE));
    }

    private static boolean waitUntilGone(ProcessHandle handle, Duration timeout) throws InterruptedException {
        var deadline = System.nanoTime() + timeout.toNanos();
        while (handle.isAlive() && System.nanoTime() < deadline) {
            Thread.sleep(100);
        }
        return !handle.isAlive();
    }
}
