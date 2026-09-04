package com.condation.cms.ipc;

/*-
 * #%L
 * CMS Server
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.condation.cms.api.IPCProperties;
import com.condation.cms.api.eventbus.Event;
import com.condation.cms.api.eventbus.events.lifecycle.ReIndexHostEvent;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class IPCProtocolTest {

	@Test
	void translatesReindexCommandIntoHostEvent() {
		var event = new AtomicReference<Event>();
		var protocol = new IPCProtocol(event::set, new TestIPCProperties());
		var input = new IPCCommands().toJsonString(
				new Command("reindex_host").setHeader("host", "demo"));

		protocol.processInput(input);

		Assertions.assertThat(event.get())
				.isEqualTo(new ReIndexHostEvent("demo"));
	}

	private record TestIPCProperties() implements IPCProperties {

		@Override
		public int port() {
			return 0;
		}

		@Override
		public Optional<String> password() {
			return Optional.empty();
		}
	}
}
