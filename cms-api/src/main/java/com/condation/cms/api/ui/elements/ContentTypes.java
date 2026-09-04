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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Registry populated by the manager content type hook.
 *
 * JavaScript extensions can keep passing object literals to the map overloads.
 * The values are converted at that boundary, so consumers of this registry
 * always receive typed definitions. Java extensions can use the typed
 * overloads directly.
 *
 * @author thmar
 */
public class ContentTypes {

	private final Set<PageTemplate> pageTemplates = new LinkedHashSet<>();
	private final Set<SectionEntryTemplate> sectionEntryTemplates = new LinkedHashSet<>();
	private final Set<ListItemType> listItemTypes = new LinkedHashSet<>();
	private final Set<CollectionType> collectionTypes = new LinkedHashSet<>();

	public void registerCollection(CollectionType collectionType) {
		var registeredType = Objects.requireNonNull(collectionType, "collectionType");
		collectionTypes.removeIf(existing -> existing.name().equals(registeredType.name()));
		collectionTypes.add(registeredType);
	}

	/** JavaScript interop overload. */
	public void registerCollection(Map<String, Object> collectionType) {
		registerCollection(CollectionType.fromMap(collectionType));
	}

	public Optional<CollectionType> getCollection(String name) {
		return collectionTypes.stream().filter(type -> type.name().equals(name)).findFirst();
	}

	public Set<CollectionType> getCollections() {
		return Collections.unmodifiableSet(new LinkedHashSet<>(collectionTypes));
	}

	public void registerListItemType(ListItemType listItemType) {
		listItemTypes.add(Objects.requireNonNull(listItemType, "listItemType"));
	}

	/**
	 * JavaScript interop overload. Prefer {@link #registerListItemType(ListItemType)}
	 * from Java code.
	 */
	public void registerListItemType(Map<String, Object> listItemType) {
		registerListItemType(ListItemType.fromMap(listItemType));
	}

	public Set<ListItemType> getListItemTypes() {
		return Collections.unmodifiableSet(new LinkedHashSet<>(listItemTypes));
	}

	public Optional<PageTemplate> getPageTemplate(String name) {
		return pageTemplates.stream().filter(template -> template.name().equals(name)).findFirst();
	}

	public void registerPageTemplate(PageTemplate pageTemplate) {
		pageTemplates.add(Objects.requireNonNull(pageTemplate, "pageTemplate"));
	}

	/**
	 * JavaScript interop overload. Prefer {@link #registerPageTemplate(PageTemplate)}
	 * from Java code.
	 */
	public void registerPageTemplate(Map<String, Object> pageTemplate) {
		registerPageTemplate(PageTemplate.fromMap(pageTemplate));
	}

	public void registerSectionEntryTemplate(SectionEntryTemplate sectionEntryTemplate) {
		sectionEntryTemplates.add(Objects.requireNonNull(sectionEntryTemplate, "sectionEntryTemplate"));
	}

	/**
	 * JavaScript interop overload. Prefer
	 * {@link #registerSectionEntryTemplate(SectionEntryTemplate)} from Java code.
	 */
	public void registerSectionEntryTemplate(Map<String, Object> sectionEntryTemplate) {
		registerSectionEntryTemplate(SectionEntryTemplate.fromMap(sectionEntryTemplate));
	}

	public Set<PageTemplate> getPageTemplates() {
		return Collections.unmodifiableSet(new LinkedHashSet<>(pageTemplates));
	}

	public Set<SectionEntryTemplate> getSectionEntryTemplates(String section) {
		Set<SectionEntryTemplate> result = new LinkedHashSet<>();
		sectionEntryTemplates.stream()
				.filter(template -> template.section().equals(section))
				.forEach(result::add);
		return Collections.unmodifiableSet(result);
	}

	public Set<SectionEntryTemplate> getSectionEntryTemplates() {
		return Collections.unmodifiableSet(new LinkedHashSet<>(sectionEntryTemplates));
	}
}
