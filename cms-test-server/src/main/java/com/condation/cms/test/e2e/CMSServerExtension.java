package com.condation.cms.test.e2e;

/*-
 * #%L
 * cms-test-server
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

import com.condation.cms.cli.CMSCli;
import com.condation.cms.cli.tools.CLIServerUtils;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 *
 * @author thmar
 */
public class CMSServerExtension implements BeforeAllCallback, AutoCloseable {

    private static final String DEFAULT_SERVER_HOME = "../test-server";

    private final String serverHome;

    public CMSServerExtension() {
        this(DEFAULT_SERVER_HOME);
    }

    public CMSServerExtension(String serverHome) {
        this.serverHome = serverHome;
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        var store = context.getRoot().getStore(ExtensionContext.Namespace.GLOBAL);
        String storeKey = "server:" + serverHome;

        if (store.get(storeKey) != null) {
            return;
        }

        System.setProperty("cms.home", serverHome);

        Thread serverThread = new Thread(() -> CMSCli.main("server", "start"));
        serverThread.setDaemon(true);
        serverThread.start();

        waitForProcess(20);

        // Store a CloseableResource so JUnit shuts the server down after ALL tests
        store.put(storeKey, (AutoCloseable) () -> {
            System.setProperty("cms.home", serverHome);
            CMSCli.main("server", "stop");
            serverThread.join(10_000);
        });
    }

    @Override
    public void close() {
        // Intentionally empty — shutdown is handled via the GLOBAL store above
    }

    private void waitForProcess(int timeoutSeconds) throws Exception {
        var deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;

        while (System.currentTimeMillis() < deadline) {
            var process = CLIServerUtils.getCMSProcess();
            if (process.isPresent()) {
                System.out.println("Server gestartet mit PID: " + process.get().pid());
                return;
            }
            Thread.sleep(200);
        }
        throw new IllegalStateException(
                "Server-PID-File nicht erschienen nach " + timeoutSeconds + "s"
        );
    }
}
