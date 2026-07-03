package com.condation.cms.templates.expression;

/*-
 * #%L
 * CMS Templates
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

import org.apache.commons.jexl3.introspection.JexlPermissions;


public class CMSPermissions  {
    
	public static JexlPermissions PERMISSIONS = JexlPermissions.parse(
            "# Restricted Uberspect Permissions",
			"com.condation.cms.*",
			"com.google.gson.*",
            "java.nio.*",
            "java.io.*",
            "java.lang.*",
            "java.math.*",
            "java.text.*",
            "java.util.*",
            "org.w3c.dom.*",
            "org.apache.commons.jexl3.*",
            "org.apache.commons.jexl3 { JexlBuilder {} }",
            "org.apache.commons.jexl3.internal { Engine {} }",
            "java.lang { Runtime{} System{} ProcessBuilder{} Process{}" +
                    " RuntimePermission{} SecurityManager{}" +
                    " Thread{} ThreadGroup{} Class{} }",
            "java.lang.annotation {}",
            "java.lang.instrument {}",
            "java.lang.invoke {}",
            "java.lang.management {}",
            "java.lang.ref {}",
            "java.lang.reflect {}",
            "java.net {}",
            "java.io { File{} FileDescriptor{} }",
            "java.nio { Path { } Paths { } Files { } }",
            "java.rmi"
    );
	
}
