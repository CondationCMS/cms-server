package com.condation.cms.api.utils;

/*-
 * #%L
 * cms-api
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

import java.nio.file.Path;

public final class ServerUtil {

    private ServerUtil(){}

    public static Path getHome () {
        if (System.getenv().containsKey("CMS_HOME")) {
			return Path.of(System.getenv("CMS_HOME"));
		}
		if (System.getProperties().containsKey("cms.home")) {
			return Path.of(System.getProperties().getProperty("cms.home"));
		}
        return Path.of(".");
    }

    public static Path getPath (String path) {
        return getHome().resolve(path);
    }
}
