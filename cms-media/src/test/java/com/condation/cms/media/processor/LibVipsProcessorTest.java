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

import com.condation.cms.api.media.MediaFormat;
import com.condation.cms.api.media.MediaUtils.Format;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Integration tests for {@link LibVipsProcessor}.
 * Skipped automatically when libvips is not installed on the current system.
 *
 * @author t.marx
 */
@EnabledIf("isLibVipsAvailable")
class LibVipsProcessorTest {

    static final Path SOURCE = Path.of("src/test/resources/assets/test.jpg");
    static final Path OUTPUT_DIR = Path.of("target/libvips-test");

    static LibVipsProcessor processor;

    static boolean isLibVipsAvailable() {
        return new LibVipsProcessor().isAvailable();
    }

    @BeforeAll
    static void setup() throws IOException {
        processor = new LibVipsProcessor();
        Files.createDirectories(OUTPUT_DIR);
    }

    @ParameterizedTest(name = "scale to JPEG 200x150 — output format {0}")
    @EnumSource(Format.class)
    void scale_without_crop(Format outputFormat) throws IOException {
        String ext = ext(outputFormat);
        Path target = OUTPUT_DIR.resolve("scale_no_crop" + ext);

        MediaFormat format = new MediaFormat("test", 200, 150, outputFormat, false);

        processor.process(SOURCE, target, format, null);

        Assertions.assertThat(target)
                .exists()
                .isRegularFile()
                .isNotEmptyFile();
    }

    @ParameterizedTest(name = "scale with crop — output format {0}")
    @EnumSource(Format.class)
    void scale_with_crop(Format outputFormat) throws IOException {
        String ext = ext(outputFormat);
        Path target = OUTPUT_DIR.resolve("scale_with_crop" + ext);

        MediaFormat format = new MediaFormat("test", 100, 100, outputFormat, false, true);

        // crop a 200x200 region from the top-left of the source
        var crop = new com.condation.cms.media.CropCalculator.CropArea(0, 0, 200, 200);

        processor.process(SOURCE, target, format, crop);

        Assertions.assertThat(target)
                .exists()
                .isRegularFile()
                .isNotEmptyFile();
    }

    private static String ext(Format format) {
        return switch (format) {
            case JPEG -> ".jpeg";
            case PNG  -> ".png";
            case WEBP -> ".webp";
            case AVIF -> ".avif";
        };
    }
}
