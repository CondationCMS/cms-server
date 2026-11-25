package com.condation.cms.maven.add.types;

/*-
 * #%L
 * CondationCMS Maven Plugin
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

import com.condation.cms.maven.add.ExtensionSpec;

/**
 * Post processing of a created backup file.
 */
public final class BackupPostProcessingType extends BaseType {

    public BackupPostProcessingType() {
        super("backup-post-processing", "Backup file post processing");
    }

    @Override
    public String render(ExtensionSpec spec) {
        return fill(spec, """
                package {{package}};

                import com.condation.cms.api.extensions.BackupFilePostProcessingExtensionPoint;
                import com.condation.modules.api.annotation.Extension;
                import java.nio.file.Path;

                @Extension(BackupFilePostProcessingExtensionPoint.class)
                public class {{class}} extends BackupFilePostProcessingExtensionPoint {

                    @Override
                    public void postProcess(Path backupFile) {
                        // e.g. copy the backup file to a remote storage
                    }
                }
                """);
    }
}
