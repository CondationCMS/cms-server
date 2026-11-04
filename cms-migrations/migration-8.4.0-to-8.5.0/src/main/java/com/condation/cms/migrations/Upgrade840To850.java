package com.condation.cms.migrations;

/*-
 * #%L
 * CondationCMS Migrations
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

import com.condation.cms.updater.Migration;
import com.condation.cms.updater.MigrationContext;

/** Example migration that writes the transition versions to standard output. */
public final class Upgrade840To850 implements Migration {
    public static final String ID = "migration-8.4.0-to-8.5.0";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public void execute(MigrationContext context) {
        System.out.printf("Example migration executed: CondationCMS %s -> %s%n",
                context.fromVersion(), context.toVersion());
    }
}
