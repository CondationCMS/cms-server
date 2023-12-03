package com.github.thmarx.cms.api.db;

import com.github.thmarx.cms.api.Constants;
import com.github.thmarx.cms.api.PreviewContext;
import com.github.thmarx.cms.api.utils.SectionUtil;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author t.marx
 */
public record ContentNode(String uri, String name, Map<String, Object> data, 
		boolean directory, Map<String, ContentNode> children)  {

		public ContentNode(String uri, String name, Map<String, Object> data, boolean directory) {
			this(uri, name, data, directory, new HashMap<String, ContentNode>());
		}

		public ContentNode(String uri, String name, Map<String, Object> data) {
			this(uri, name, data, false, new HashMap<String, ContentNode>());
		}
		
		public boolean isDirectory() {
			return directory;
		}

		public boolean isHidden() {
			return name.startsWith(".");
		}

		public boolean isDraft() {
			return (boolean) data().getOrDefault(Constants.MetaFields.DRAFT, false);
		}

		public boolean isPublished() {
			if (PreviewContext.IS_PREVIEW.get()) {
				return true;
			}
			var localDate = (Date) data.getOrDefault(Constants.MetaFields.PUBLISHED, Date.from(Instant.now()));
			var now = Date.from(Instant.now());
			return !isDraft() && (localDate.before(now) || localDate.equals(now));
		}

		public boolean isSection() {
			return SectionUtil.isSection(name);
		}
		
		@Override
		public boolean equals (Object other) {
			
			if (this == other) {
				return true;
			}
			
			if (other == null) {
				return false;
			}
			
			if (!(other instanceof ContentNode)) {
				return false;
			}
			
			var otherNode = (ContentNode)other;
			
			return uri.equals(otherNode.uri);
		}
	}
