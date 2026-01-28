package com.condation.cms.core.configuration;

/*-
 * #%L
 * cms-core
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
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

import com.condation.cms.api.utils.ServerUtil;
import io.github.cdimascio.dotenv.Dotenv;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author thorstenmarx
 */
public class EnvironmentVariables {

	private final Dotenv dotenv;

	private static final Pattern ENV_PATTERN
			= Pattern.compile("\\$\\{env:([\\w]+)\\}");

	public EnvironmentVariables(Path path) {
		this.dotenv = Dotenv.configure()
				.directory(path.toAbsolutePath().toString())
				.ignoreIfMissing()
				.load();
	}
	
	public EnvironmentVariables () {
		this(ServerUtil.getHome());
	}

	public String resolveEnvVars(String input) {
		if (input == null || input.isEmpty()) {
			return input;
		}

		Matcher matcher = ENV_PATTERN.matcher(input);
		StringBuffer sb = new StringBuffer();

		while (matcher.find()) {
			String envVarName = matcher.group(1);
			String envValue = dotenv.get(envVarName);

			// Regex-Metazeichen escapen (wichtig!)
			String replacement = envValue != null
					? Matcher.quoteReplacement(envValue)
					: Matcher.quoteReplacement(matcher.group(0));

			matcher.appendReplacement(sb, replacement);
		}
		matcher.appendTail(sb);
		return sb.toString();
	}
}
