package com.condation.cms.api.db;

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

import com.condation.cms.api.Constants;
import com.condation.cms.api.utils.DateRange;
import java.time.Instant;
import java.util.Date;

/**
 *
 * @author t.marx
 */
public class DefaultWFStatusProvider implements WFStatusProvider {

	public static final String STATUS_DRAFT = "draft";
	public static final String STATUS_PUBLISHED = "published";

	@Override
	public boolean isPublished(ContentNode node) {
		return STATUS_PUBLISHED.equals(statusValue(node));
	}
    
    @Override
    public Status status (ContentNode node) {
		var published = isPublished(node);
		var publish_date = (Date) node.data().getOrDefault(Constants.MetaFields.PUBLISH_DATE, Date.from(Instant.now()));
		
		var unpublish_date = (Date) node.data().getOrDefault(Constants.MetaFields.UNPUBLISH_DATE, null);
		
		return new Status(published, DateRange.isNowWithin(publish_date, unpublish_date));
	}
	
	private String statusValue(ContentNode node) {
		if (node.data().containsKey(Constants.MetaFields.STATUS)) {
			return (String) node.data().getOrDefault(Constants.MetaFields.STATUS, STATUS_DRAFT);
		}
		return (boolean) node.data().getOrDefault(Constants.MetaFields.PUBLISHED, false)
				? STATUS_PUBLISHED
				: STATUS_DRAFT;
	}
	
}
