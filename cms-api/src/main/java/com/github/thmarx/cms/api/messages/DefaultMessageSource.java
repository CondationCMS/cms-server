package com.github.thmarx.cms.api.messages;

/*-
 * #%L
 * cms-api
 * %%
 * Copyright (C) 2023 - 2024 Marx-Software
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

import com.github.thmarx.cms.api.SiteProperties;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultMessageSource implements MessageSource {
	
	final SiteProperties siteProperties;
	final Path messageFolder;
	
	@Override
	public String getLabel (final String bundle, final String label) {
		return getLabel(bundle, label, List.of());
	}
	
	@Override
	public String getLabel (final String bundle, final String label, final List<Object> data) {
		try {
			var resourceBundle = fromClassLoader(bundle, siteProperties.locale());
			if (resourceBundle != null) {
				var messageFormat = new MessageFormat(resourceBundle.getString(label), siteProperties.locale());
				return messageFormat.format(data.toArray());
			}
		} catch (Exception e) {
			log.error("bundle not found", bundle);
		}
		return "[" + label + "]";
	}
	
	protected ResourceBundle fromClassLoader(final String bundleName) throws Exception {
        return fromClassLoader(bundleName, Locale.getDefault());
    }
	
	protected ResourceBundle fromClassLoader(final String bundleName, final Locale locale) throws Exception {
        URL[] urls = {messageFolder.toUri().toURL()};
        ClassLoader loader = new URLClassLoader(urls);
        return ResourceBundle.getBundle(bundleName, locale, loader);
    }
}
