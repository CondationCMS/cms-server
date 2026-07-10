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

import com.condation.cms.api.workflow.WFStatusProvider;
import com.condation.cms.api.Constants;
import com.condation.cms.api.feature.features.IsPreviewFeature;
import com.condation.cms.api.feature.features.WorkflowFeature;
import com.condation.cms.api.request.RequestContextScope;
import com.condation.cms.api.utils.DateRange;
import com.condation.cms.api.workflow.DefaultWFStatusProvider;
import java.time.Instant;
import java.util.Date;
import org.jspecify.annotations.NonNull;

/**
 *
 * @author thmar
 */
public class NodeVisibility {

	private static final WFStatusProvider DEFAULT_WF_STATUS_PROVIDER = new DefaultWFStatusProvider();
    
    public static boolean isVisible (@NonNull ContentNode node) {
        if (RequestContextScope.REQUEST_CONTEXT.isBound()
				&& RequestContextScope.REQUEST_CONTEXT.get().has(IsPreviewFeature.class)) {
			return true;
		}
		
		if (!wfStatusProvider().isPublished(node)) {
			return false;
		}
		
		var publish_date = (Date) node.data().getOrDefault(Constants.MetaFields.PUBLISH_DATE, Date.from(Instant.now()));
		
		var unpublish_date = (Date) node.data().getOrDefault(Constants.MetaFields.UNPUBLISH_DATE, null);
		
		return DateRange.isNowWithin(publish_date, unpublish_date);
    }

	private static WFStatusProvider wfStatusProvider() {
		if (RequestContextScope.REQUEST_CONTEXT.isBound()
				&& RequestContextScope.REQUEST_CONTEXT.get().has(WorkflowFeature.class)) {
			return RequestContextScope.REQUEST_CONTEXT.get()
					.get(WorkflowFeature.class)
					.workflow().getStatusProvider();
		}

		return DEFAULT_WF_STATUS_PROVIDER;
	}
}
