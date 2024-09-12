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

	final MessageSource priorizedMessageSource;

	public ThemeMessageSource(SiteProperties siteProperties, Path messageFolder, MessageSource priorizedMessageSource) {
		super(siteProperties, messageFolder);
		this.priorizedMessageSource = priorizedMessageSource;
	}
	
	@Override
	public String getLabel (final String bundle, final String label) {
		
		var message = priorizedMessageSource.getLabel(bundle, label);
		if (!("[" + label + "]").equals(label)) {
			return message;
		}
		
		return getLabel(bundle, label, List.of());
	}
	
	
	@Override
	public String getLabel (final String bundle, final String label, final List<Object> data) {
		
		var message = priorizedMessageSource.getLabel(bundle, bundle, data);
		if (!("[" + label + "]").equals(label)) {
			return message;
		}
		
		try {
			var resourceBundle = fromClassLoader(bundle, siteProperties.locale());
			if (resourceBundle != null) {
				var messageFormat = new MessageFormat(resourceBundle.getString(label), siteProperties.locale());
				return messageFormat.format(data);
			}
		} catch (Exception e) {
			log.error("bundle not found", bundle);
		}
		return "[" + label + "]";
	}
}
