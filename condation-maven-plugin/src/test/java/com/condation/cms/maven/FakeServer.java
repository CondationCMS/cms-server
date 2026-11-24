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

import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Test double for CMSCli: "server start" opens the configured port and writes cms.pid,
 * "server stop" removes cms.pid (the running fake then exits).
 * Behaviour switches via -Dfake.mode=exit1|hang|ignorestop.
 */
public class FakeServer {

    public static void main(String[] args) throws Exception {
        var mode = System.getProperty("fake.mode", "normal");
        // CMSCli breaks on an absolute cms.home (Path.of(".").relativize(...)), so it must be relative
        if (Path.of(System.getProperty("cms.home")).isAbsolute()) {
            System.err.println("absolute cms.home is not supported by CMSCli");
            System.exit(3);
        }
        var home = Path.of(System.getProperty("cms.home"));
        var pidFile = home.resolve("cms.pid");

        if (args.length >= 2 && args[1].equals("stop")) {
            if (!mode.equals("ignorestop")) {
                Files.deleteIfExists(pidFile);
            }
            return;
        }

        if (mode.equals("exit1")) {
            System.exit(1);
        }
        if (mode.equals("hang")) {
            Thread.sleep(Long.MAX_VALUE);
        }

        try (var socket = new ServerSocket(ServerPort.read(home))) {
            var handle = ProcessHandle.current();
            var props = new Properties();
            props.setProperty("pid", Long.toString(handle.pid()));
            props.setProperty("startedAt", Long.toString(handle.info().startInstant().get().toEpochMilli()));
            // atomic like CLIServerUtils.writePidFile
            var tmp = home.resolve("cms.pid.tmp");
            try (var out = Files.newBufferedWriter(tmp)) {
                props.store(out, null);
            }
            Files.move(tmp, pidFile, java.nio.file.StandardCopyOption.ATOMIC_MOVE);
            var accepter = new Thread(() -> {
                try {
                    while (true) {
                        socket.accept().close();
                    }
                } catch (Exception e) {
                    // closed
                }
            });
            accepter.setDaemon(true);
            accepter.start();
            while (Files.exists(pidFile) || mode.equals("ignorestop")) {
                Thread.sleep(100);
            }
        }
    }
}
