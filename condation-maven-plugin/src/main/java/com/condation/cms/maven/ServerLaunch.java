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

import java.nio.file.Path;
import java.util.List;

/**
 * Everything needed to fork a CMS server JVM.
 *
 * @param cmsHome   server home, becomes <code>-Dcms.home</code> and working directory
 * @param classpath resolved classpath of cms-server
 * @param mainClass main class, normally <code>com.condation.cms.cli.CMSCli</code>
 * @param jvmArgs   additional JVM arguments
 * @param logFile   file receiving stdout/stderr, <code>null</code> to inherit the console
 */
public record ServerLaunch(Path cmsHome, List<Path> classpath, String mainClass, List<String> jvmArgs, Path logFile) {
}
