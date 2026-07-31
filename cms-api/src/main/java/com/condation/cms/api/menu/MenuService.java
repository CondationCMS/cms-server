package com.condation.cms.api.menu;

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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Storage abstraction for site navigation menus.
 */
public interface MenuService {

	List<Menu> list() throws IOException;

	Optional<Menu> get(String id) throws IOException;

	Menu create(Menu menu) throws IOException;

	Menu update(Menu menu) throws IOException;

	boolean delete(String id) throws IOException;
}
