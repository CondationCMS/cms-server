package com.condation.cms.cli.commands;

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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

class HostCommandsTest {

	@Test
	void exposesReindexSubcommand() {
		var commandLine = new CommandLine(new HostCommands());
		var parsed = commandLine.parseArgs("reindex", "demo");

		Assertions.assertThat(commandLine.getSubcommands()).containsKey("reindex");
		Assertions.assertThat(parsed.subcommand().commandSpec().name()).isEqualTo("reindex");
	}
}
