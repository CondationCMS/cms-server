package com.condation.cms.content.pipeline;

/*-
 * #%L
 * CMS Content
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
import com.condation.cms.api.hooks.HookSystem;
import com.condation.cms.api.hooks.Hooks;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author thmar
 */
@Slf4j
@RequiredArgsConstructor
public class HTMLPipeline {

	private final HookSystem hookSystem;
	
	public String process(String rawContent) {
		rawContent = updateSlot(Hooks.CONTENT_SLOT_HEADER, "</head>", rawContent);
		return updateSlot(Hooks.CONTENT_SLOT_FOOTER, "</body>", rawContent);
	}

	public String updateSlot (Hooks hook, String elementName, String rawContent) {
		
		if (!rawContent.contains(elementName)) {
			log.debug("No {} found, skipping header slot injection", elementName);
			return rawContent;
		}
		
		var result = hookSystem.doAction(hook.hook());
		
		var hookValues = result.results().stream()
				.filter(Objects::nonNull)
				.filter(String.class::isInstance)
				.map(String.class::cast)
				.toList();
		if (hookValues.isEmpty()) {
			return rawContent;
		}
				
		
		var mergedValue = String.join("\n\n", hookValues);
		
		return rawContent.replace(elementName, mergedValue + "\n" + elementName);
	}
	
}
