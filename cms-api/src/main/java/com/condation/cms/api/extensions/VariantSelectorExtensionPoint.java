package com.condation.cms.api.extensions;

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

import com.condation.cms.api.variants.VariantSelector;

/**
 * Extension point for automatic variant selection strategies.
 *
 * <p>Manager and explicit preview selection are handled by the CMS before an
 * extension is invoked. Implementations therefore only select a variant for a
 * normal public request.</p>
 */
public abstract class VariantSelectorExtensionPoint
		extends AbstractExtensionPoint
		implements VariantSelector {

	public abstract String id();

	public abstract String label();
}
