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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ServerProcessTest {

    @TempDir
    Path home;

    private Process started;

    @AfterEach
    void cleanup() throws Exception {
        if (started != null) {
            started.destroyForcibly();
        }
        if (Files.exists(home.resolve("cms.pid"))) {
            var props = new java.util.Properties();
            try (var in = Files.newBufferedReader(home.resolve("cms.pid"))) {
                props.load(in);
            }
            ProcessHandle.of(Long.parseLong(props.getProperty("pid"))).ifPresent(ProcessHandle::destroyForcibly);
        }
    }

    private ServerLaunch launch(String mode) throws IOException {
        int port;
        try (var s = new ServerSocket(0)) {
            port = s.getLocalPort();
        }
        Files.writeString(home.resolve("server.toml"), "[server]\nport = " + port + "\n");
        var cp = Arrays.stream(System.getProperty("java.class.path").split(File.pathSeparator)).map(Path::of).toList();
        return new ServerLaunch(home, cp, FakeServer.class.getName(),
                List.of("-Dfake.mode=" + mode), home.resolve("logs/server.out"));
    }

    @Test
    void should_be_ready_when_pid_file_and_port_available() throws Exception {
        var l = launch("normal");
        started = ServerProcess.launch(l, "server", "start");

        ServerProcess.awaitReady(started, home, Duration.ofSeconds(20), l.logFile());

        assertThat(ServerProcess.isRunning(home)).isTrue();
    }

    @Test
    void should_fail_fast_with_exit_code_when_process_ends_before_ready() throws Exception {
        var l = launch("exit1");
        started = ServerProcess.launch(l, "server", "start");
        var begin = System.nanoTime();

        assertThatThrownBy(() -> ServerProcess.awaitReady(started, home, Duration.ofSeconds(30), l.logFile()))
                .isInstanceOf(MojoExecutionException.class)
                .hasMessageContaining("exit code 1")
                .hasMessageContaining("server.out");

        assertThat(Duration.ofNanos(System.nanoTime() - begin)).isLessThan(Duration.ofSeconds(25));
    }

    @Test
    void should_fail_when_timeout_exceeded() throws Exception {
        var l = launch("hang");
        started = ServerProcess.launch(l, "server", "start");

        assertThatThrownBy(() -> ServerProcess.awaitReady(started, home, Duration.ofSeconds(2), l.logFile()))
                .isInstanceOf(MojoExecutionException.class)
                .hasMessageContaining("did not become ready");
    }

    @Test
    void should_report_not_running_when_no_pid_file() {
        assertThat(ServerProcess.isRunning(home)).isFalse();
    }

    @Test
    void should_stop_server_when_stop_called() throws Exception {
        var l = launch("normal");
        started = ServerProcess.launch(l, "server", "start");
        ServerProcess.awaitReady(started, home, Duration.ofSeconds(20), l.logFile());

        ServerProcess.stop(l, Duration.ofSeconds(10));

        assertThat(ServerProcess.isRunning(home)).isFalse();
        assertThat(started.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)).isTrue();
    }

    @Test
    void should_terminate_process_when_server_ignores_stop_command() throws Exception {
        var l = launch("ignorestop");
        started = ServerProcess.launch(l, "server", "start");
        ServerProcess.awaitReady(started, home, Duration.ofSeconds(20), l.logFile());

        ServerProcess.stop(l, Duration.ofSeconds(2));

        assertThat(started.waitFor(10, java.util.concurrent.TimeUnit.SECONDS)).isTrue();
        assertThat(ServerProcess.isRunning(home)).isFalse();
    }

    private static long fakeServersAlive() {
        return ProcessHandle.current().children()
                .filter(h -> h.info().commandLine().map(c -> c.contains(FakeServer.class.getName())).orElse(false))
                .count();
    }

    @Test
    void should_terminate_launched_process_when_terminate_called() throws Exception {
        var l = launch("hang");
        started = ServerProcess.launch(l, "server", "start");

        ServerProcess.terminate(started);

        assertThat(started.isAlive()).isFalse();
    }

    @Test
    void should_not_leave_process_running_when_startup_times_out() throws Exception {
        var l = launch("hang");

        assertThatThrownBy(() -> ServerProcess.startAndAwait(l, Duration.ofSeconds(2)))
                .isInstanceOf(MojoExecutionException.class)
                .hasMessageContaining("did not become ready");

        assertThat(fakeServersAlive()).isZero();
    }

    @Test
    void should_return_running_process_when_startup_succeeds() throws Exception {
        var l = launch("normal");

        started = ServerProcess.startAndAwait(l, Duration.ofSeconds(20));

        assertThat(started.isAlive()).isTrue();
        assertThat(ServerProcess.isRunning(home)).isTrue();
    }

    @Test
    void should_use_java_exe_on_windows() {
        assertThat(ServerProcess.javaExecutable("Windows 11", "C:\\jdk")).isEqualTo(Path.of("C:\\jdk", "bin", "java.exe"));
    }

    @Test
    void should_use_plain_java_binary_on_unix() {
        assertThat(ServerProcess.javaExecutable("Mac OS X", "/jdk")).isEqualTo(Path.of("/jdk", "bin", "java"));
        assertThat(ServerProcess.javaExecutable(null, "/jdk")).isEqualTo(Path.of("/jdk", "bin", "java"));
    }
}
