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
 * Wraps a {@link Block} with its absolute start/end positions in the original
 * markdown document. The block's own {@code start()}/{@code end()} are relative
 * to the substring the rule received; {@code absoluteStart}/{@code absoluteEnd}
 * are correct offsets into the full document string.
 *
 * @author t.marx
 */
public record LocatedBlock(Block block, int absoluteStart, int absoluteEnd) {}
