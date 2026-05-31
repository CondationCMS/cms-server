package com.condation.cms.media.processor;

/*-
 * #%L
 * CMS Media
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

import com.condation.cms.api.configuration.configs.MediaConfiguration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Selects and provides the active {@link ImageProcessor}.
 *
 * <p>Configured via the {@code processor} field in {@code media.toml}:
 * <ul>
 *   <li>{@code auto}        – tries libvips → imagemagick → imageio in order (default)</li>
 *   <li>{@code libvips}     – use libvips</li>
 *   <li>{@code imagemagick} – use imagemagick</li>
 *   <li>{@code imageio}     – use the built-in Java ImageIO processor</li>
 * </ul>
 * An optional {@code bin_path} overrides the executable path for the selected processor.
 */
@Slf4j
public class ImageProcessorFactory {

    private ImageProcessor resolved = null;
    private final String configuredName;
    private final String binPath;

    public ImageProcessorFactory(MediaConfiguration config) {
        this.configuredName = config.getProcessor() == null ? "auto" : config.getProcessor().trim().toLowerCase();
        this.binPath = config.getBinPath();
    }

    public ImageProcessor get() {
        if (resolved == null) {
            resolved = resolve();
        }
        return resolved;
    }

    public void reset() {
        resolved = null;
    }

    private ImageProcessor resolve() {
        ImageProcessor processor = create(configuredName);
        log.info("Image processor: {} (configured)", processor.name());
        return processor;
    }

    private ImageProcessor create(String name) {
        return switch (name) {
            case "libvips" -> new LibVipsProcessor(binPath);
            case "imagemagick" -> new ImageMagickProcessor(binPath);
            case "imageio" -> new ImageIOProcessor();
            default -> throw new IllegalArgumentException("Unknown image processor: '" + name + "'");
        };
    }
}
