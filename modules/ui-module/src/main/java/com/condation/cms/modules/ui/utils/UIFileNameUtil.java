package com.condation.cms.modules.ui.utils;

import java.util.Arrays;

/*-
 * #%L
 * ui-module
 * %%
 * Copyright (C) 2023 - 2025 CondationCMS
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

/**
 *
 * @author thorstenmarx
 */
public class UIFileNameUtil {

   public static String createSectionFileName(String parentUri, String section, String sectionItem) {
        // Pfadteile per "/" splitten
        String[] parts = parentUri.split("/");

        // Letztes Segment = Dateiname
        String fileName = parts[parts.length - 1];

        // Basisname und Endung trennen
        int dotIndex = fileName.lastIndexOf('.');
        String baseName;
        String extension = "";

        if (dotIndex > 0) {
            baseName = fileName.substring(0, dotIndex);
            extension = fileName.substring(dotIndex); // inkl. Punkt
        } else {
            baseName = fileName;
        }

        // Neuen Dateinamen erstellen
        String newFileName = baseName + "." + section + "." + sectionItem + extension;

        // Pfad wieder zusammenbauen
        if (parts.length > 1) {
            String pathPrefix = String.join("/", Arrays.copyOf(parts, parts.length - 1));
            return pathPrefix + "/" + newFileName;
        } else {
            return newFileName;
        }
    }
}
