package com.condation.cms.api.ui.elements;

/*-
 * #%L
 * CMS Api
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

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
/**
 *
 * @author thmar
 */
public class ContentTypes {

	public Set<PageTemplate> pageTemplates = new HashSet();

	public Set<SectionEntryTemplate> sectionEntryTemplates = new HashSet<>();
	
	public Set<ListItemType> listItemTypes = new HashSet<>();

	public void registerListItemType(Map<String, Object> listItemType) {
		listItemTypes.add(new ListItemType(listItemType));
	}
	
	public Set<ListItemType> getListItemTypes () {
		return new HashSet<>(listItemTypes);
	}
	
	public Optional<PageTemplate> getPageTemplate (String name) {
		return pageTemplates.stream().filter(pt -> pt.name.equals(name)).findFirst();
	}
	
	public void registerPageTemplate(Map<String, Object> pageTemplate) {
		pageTemplates.add(new PageTemplate(pageTemplate));
	}

	public void registerSectionEntryTemplate(Map<String, Object> sectionEntryTemplate) {
		sectionEntryTemplates.add(new SectionEntryTemplate(sectionEntryTemplate));
	}
	
	public Set<PageTemplate> getPageTemplates () {
		return new HashSet<>(pageTemplates);
	}
	
	public Set<SectionEntryTemplate> getSectionEntryTemplates (String section) {
		return sectionEntryTemplates.stream()
				.filter(template -> template.section().equals(section))
				.collect(Collectors.toSet());
	}
	public Set<SectionEntryTemplate> getSectionEntryTemplates () {
		return new HashSet<>(sectionEntryTemplates);
	}

	public static record PageTemplate(String name, String template, Map<String, Object> data) {

		public PageTemplate (Map<String, Object> data) {
			this(
					(String) data.getOrDefault("name", "<no name>"),
					(String) data.getOrDefault("template", "<no template>"),
					data);
		}
		
		public Map<String, Object> getForm (String name) {
			var forms = (Map<String, Object>)data.getOrDefault("forms", Collections.emptyMap());
			return (Map<String, Object>)forms.getOrDefault(name, Collections.emptyMap());
		}
        
        public String getContentFolder () {
            return (String) data.getOrDefault("contentFolder", "");
        }
        public boolean addCreateButton () {
            return (boolean) data.getOrDefault("createButton", true);
        }
	}

	public static record SectionEntryTemplate(String name, String template, Map<String, Object> data) {

		public SectionEntryTemplate (Map<String, Object> data) {
			this(
					(String) data.getOrDefault("name", "<no name>"),
					(String) data.getOrDefault("template", "<no template>"),
					data);
		}

		public String section() {
			return (String) data.getOrDefault("section", "<no section>");
		}
	}
	
	public static record ListItemType(String name, Map<String, Object> data) {

		public ListItemType (Map<String, Object> data) {
			this(
					(String) data.getOrDefault("name", "<no name>"),
					data);
		}

		public Map<String, Object> getForm (String name) {
			return (Map<String, Object>)data.getOrDefault("form", Collections.emptyMap());
		}
	}
}
