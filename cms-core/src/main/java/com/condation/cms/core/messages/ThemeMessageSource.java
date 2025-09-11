package com.condation.cms.core.messages;

/*-
 * #%L
 * cms-core
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


import com.condation.cms.api.SiteProperties;
import com.condation.cms.api.cache.ICache;
import com.condation.cms.api.messages.MessageSource;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
public class ThemeMessageSource extends DefaultMessageSource {

	final MessageSource prioritizedMessageSource;

	public ThemeMessageSource(
			SiteProperties siteProperties, 
			Path messageFolder, 
			MessageSource prioritizedMessageSource,
			ICache<String, String> cache) {
		super(siteProperties, messageFolder, cache);
		this.prioritizedMessageSource = prioritizedMessageSource;
	}
	
	@Override
	public String getLabel (final String bundle, final String label) {
		
		var message = prioritizedMessageSource.getLabel(bundle, label);
		if (!("[" + label + "]").equals(message)) {
			return message;
		}
		
		return getLabel(bundle, label, List.of());
	}
	
	
	@Override
	public String getLabel (final String bundle, final String label, final List<Object> data) {
		
		var message = prioritizedMessageSource.getLabel(bundle, label, data);
		if (!("[" + label + "]").equals(message)) {
			return message;
		}
		
		try {
			if (!messages.contains(label)) {
				fromClassLoader(bundle, siteProperties.locale());
			}
			var messageFormat = new MessageFormat(messages.get(label), siteProperties.locale());
			return messageFormat.format(data.toArray());
		} catch (Exception e) {
			log.error("bundle not found", bundle);
		}
		return "[" + label + "]";
	}
}
