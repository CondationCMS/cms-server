package com.condation.cms.cli;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 - 2024 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */


import java.io.File;
import picocli.CommandLine;

/**
 *
 * @author t.marx
 */
public class CMSCli {

	static {
		File log4j2File = new File("log4j2.xml");
		if (log4j2File.exists()) {
			System.setProperty("log4j2.configurationFile", log4j2File.toURI().toString());
		}
	}

	public static CommandLine getCommandLine() {
		return new CommandLine(new CLICommand());
	}

	public static void main(String[] args) {
		CMSCli.getCommandLine().execute(args);
	}
}
