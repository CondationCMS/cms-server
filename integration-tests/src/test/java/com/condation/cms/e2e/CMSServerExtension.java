package com.condation.cms.e2e;

/*-
 * #%L
 * integration-tests
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

    private static volatile boolean started = false;
    private static Thread serverThread;
    
    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        if (!started) {
            started = true;
            
            System.setProperty("cms.home", "../test-server");
            
            serverThread = new Thread(() -> {
                CMSCli.main("server", "start");
            });
            serverThread.setDaemon(true);
            serverThread.start();
            
            waitForProcess(10);
            
            context.getRoot()
                    .getStore(ExtensionContext.Namespace.GLOBAL)
                    .put("server", this);
            
        }
    }

    @Override
    public void close() throws Exception {
        CMSCli.main("server", "stop");
        serverThread.join(10_000);
    }
    
    private static void waitForProcess(int timeoutSeconds) throws Exception {
    var deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;

    while (System.currentTimeMillis() < deadline) {
        var process = CLIServerUtils.getCMSProcess();
        if (process.isPresent()) {
            // Optional: PID auslesen und prüfen ob Prozess wirklich läuft
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
