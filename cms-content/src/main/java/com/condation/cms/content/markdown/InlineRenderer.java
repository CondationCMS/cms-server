package com.condation.cms.content.markdown;

/*-
 * #%L
 * CMS Content
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

/**
 * Renders inline markdown content. The {@code documentOffset} is the absolute
 * character position of {@code inline_md} in the full document, used to
 * compute correct absolute positions for inline elements like images.
 *
 * @author t.marx
 */
public interface InlineRenderer {

	String render(String inline_md, int documentOffset);

	default String render(String inline_md) {
		return render(inline_md, 0);
	}
}
