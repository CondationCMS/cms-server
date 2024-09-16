package com.condation.modules.manager;

/*-
 * #%L
 * modules-manager
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


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author marx
 */
public class ModuleAPIClassLoader extends ClassLoader {

	private final List<String> apiPackages;

	private final ClassLoader parent;

	public ModuleAPIClassLoader(ClassLoader classLoader, List<String> apiPackages) {
		super(classLoader);

		this.parent = classLoader;
		List<String> temp = apiPackages != null ? apiPackages : new ArrayList<>();
		this.apiPackages = temp.stream().map(c -> !c.endsWith(".") ? c + "." : c).collect(Collectors.toList());

		this.apiPackages.add("com.condation.modules.api.");
		this.apiPackages.add("java.");
		this.apiPackages.add("javax.");
		this.apiPackages.add("com.sun.");
		this.apiPackages.add("sun.");
	}

	private boolean isAllowed(final String name) {
		for (String packageName : apiPackages) {
			if (name.startsWith(packageName)) {
				return true;
			}

			String tempname = packageName.replaceAll("\\.", "/");
			if (name.startsWith("/")) {
				tempname = "/" + tempname;
			}
			if (name.startsWith(tempname)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected Class<?> loadClass(String className, boolean resolve) throws ClassNotFoundException {
		if (isAllowed(className)) {
			return getParent().loadClass(className);
		}

		throw new ClassNotFoundException(className + "not visible");
	}

	@Override
	public Class<?> loadClass(String className) throws ClassNotFoundException {
			if (isAllowed(className)) {
				return getParent().loadClass(className);
			}

		throw new ClassNotFoundException(className + "not visible");
	}

	@Override
	protected Class<?> findClass(String className) throws ClassNotFoundException {
		if (isAllowed(className)) {
			return getParent().loadClass(className);
		}

		throw new ClassNotFoundException(className + "not visible");
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		if (isAllowed(name)) {
			return getParent().getResourceAsStream(name);
		}

		return null;
	}

	@Override
	public Enumeration<URL> getResources(String name) throws IOException {
		if (isAllowed(name)) {
			return getParent().getResources(name);
		}

		return new Enumeration<URL>() {
			Iterator<URL> iter = Collections.EMPTY_LIST.iterator();

			@Override
			public boolean hasMoreElements() {
				return iter.hasNext();
			}

			@Override
			public URL nextElement() {
				return iter.next();
			}
		};
	}

	@Override
	public URL getResource(String name) {
		if (isAllowed(name)) {
			return getParent().getResource(name);
		}
		return null;
	}

	@Override
	protected Enumeration<URL> findResources(String name) throws IOException {
		if (isAllowed(name)) {
			return parent.getResources(name);
		}

		return new Enumeration<URL>() {
			Iterator<URL> iter = Collections.EMPTY_LIST.iterator();

			@Override
			public boolean hasMoreElements() {
				return iter.hasNext();
			}

			@Override
			public URL nextElement() {
				return iter.next();
			}
		};
	}

	@Override
	protected URL findResource(String name) {
		if (isAllowed(name)) {
			return parent.getResource(name);
		}
		return null;
	}

}
