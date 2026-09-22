package com.condation.cms.api.site;

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

import java.util.Optional;
import java.util.stream.Stream;

/**
 *
 * @author thmar
 */
public interface SiteService {
	void add (Site site);
	
	Stream<Site> sites ();

	default Stream<SiteDescriptor> descriptors() {
		return sites().map(Site::descriptor);
	}

	default Optional<SiteDescriptor> get(String siteId) {
		return descriptors()
				.filter(site -> site.id().equals(siteId))
				.findFirst();
	}

	default Stream<SiteDescriptor> sitesInGroup(String group) {
		if (group == null || group.isBlank()) {
			return Stream.empty();
		}
		return descriptors().filter(site -> group.equals(site.group()));
	}

	default Stream<SiteDescriptor> relatedSites(String siteId) {
		var current = get(siteId);
		if (current.isEmpty() || !current.get().grouped()) {
			return Stream.empty();
		}

		return sitesInGroup(current.get().group())
				.filter(site -> !site.id().equals(siteId));
	}
}
