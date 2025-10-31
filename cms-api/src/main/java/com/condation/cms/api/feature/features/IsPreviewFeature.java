package com.condation.cms.api.feature.features;

/*-
 * #%L
 * cms-api
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


import com.condation.cms.api.annotations.FeatureScope;
import com.condation.cms.api.feature.Feature;

/**
 *
 * @author t.marx
 */
@FeatureScope({FeatureScope.Scope.REQUEST})
public record IsPreviewFeature(Mode mode) implements Feature {

	public IsPreviewFeature() {
		this(Mode.PREVIEW);
	}
	
	public static enum Mode {
		MANAGER("manager"),
		PREVIEW("preview");
		
		private final String value;
		
		private Mode (String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		public static Mode forValue (String value) {
			for (var mode : values()) {
				if (mode.value.equals(value)) {
					return mode;
				}
			}
			return PREVIEW;
		}
	}
}
