package com.condation.cms.api.ui.elements;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/*-
 * #%L
 * cms-api
 * %%
 * Copyright (C) 2023 - 2025 CondationCMS
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
/**
 *
 * @author thmar
 */
public class ContentTypes {

	public Set<PageTemplate> pageTemplates = new HashSet();

	public Set<SectionTemplate> sectionTemplates = new HashSet<>();
	
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

	public void registerSectionTemplate(Map<String, Object> sectionTempate) {
		sectionTemplates.add(new SectionTemplate(sectionTempate));
	}
	
	public Set<PageTemplate> getPageTemplates () {
		return new HashSet<>(pageTemplates);
	}
	
	public Set<SectionTemplate> getSectionTemplates (String section) {
		return sectionTemplates.stream()
				.filter(template -> template.section().equals(section))
				.collect(Collectors.toSet());
	}
	public Set<SectionTemplate> getSectionTemplates () {
		return new HashSet<>(sectionTemplates);
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
	}

	public static record SectionTemplate(String name, String template, Map<String, Object> data) {

		public SectionTemplate (Map<String, Object> data) {
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
