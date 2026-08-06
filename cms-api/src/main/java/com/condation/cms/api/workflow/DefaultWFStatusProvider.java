package com.condation.cms.api.workflow;

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
import com.condation.cms.api.db.ContentNode;
import com.condation.cms.api.db.ContentQuery;
import com.condation.cms.api.utils.DateRange;
import java.util.Date;
import java.util.Optional;

/**
 *
 * @author t.marx
 */
@SuppressWarnings("deprecation")
public class DefaultWFStatusProvider implements WFStatusQueryProvider {

	public static final String STATUS_DRAFT = "draft";
	public static final String STATUS_PUBLISHED = "published";

	@Override
	public boolean isPublished(ContentNode node) {
		return STATUS_PUBLISHED.equals(statusValue(node));
	}

	@Override
	public Status status(ContentNode node) {
		var published = isPublished(node);
		var publish_date = (Date) node.data().getOrDefault(Constants.MetaFields.PUBLISH_DATE, null);
		var unpublish_date = (Date) node.data().getOrDefault(Constants.MetaFields.UNPUBLISH_DATE, null);

		return new Status(published, publish_date, unpublish_date, DateRange.isNowWithin(publish_date, unpublish_date), statusValue(node));
	}

	private String statusValue(ContentNode node) {
		if (node.data().containsKey(Constants.MetaFields.STATUS)) {
			return (String) node.data().getOrDefault(Constants.MetaFields.STATUS, STATUS_DRAFT);
		}
		return (boolean) node.data().getOrDefault(Constants.MetaFields.PUBLISHED, false)
				? STATUS_PUBLISHED
				: STATUS_DRAFT;
	}

	@Override
	public String newNodeStatus() {
		return STATUS_DRAFT;
	}

	@Override
	public <T> Optional<ContentQuery<T>> published(ContentQuery<T> query) {
		return Optional.of(query.expression(
				"(%s = '%s') OR (%s NOT EXISTS AND %s = true)".formatted(
						Constants.MetaFields.STATUS,
						STATUS_PUBLISHED,
						Constants.MetaFields.STATUS,
						Constants.MetaFields.PUBLISHED)));
	}

	@Override
	public <T> ContentQuery<T> unpublished(
			ContentQuery<T> query) {
		return query.expression(
				"(%s != '%s') OR (%s NOT EXISTS AND (%s NOT EXISTS OR %s = false))".formatted(
						Constants.MetaFields.STATUS,
						STATUS_PUBLISHED,
						Constants.MetaFields.STATUS,
						Constants.MetaFields.PUBLISHED,
						Constants.MetaFields.PUBLISHED));
	}
}
