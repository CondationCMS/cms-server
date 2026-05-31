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
import com.condation.cms.media.CropCalculator.CropArea;
import com.google.common.base.Strings;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Image processor using libvips CLI ({@code vips} command). Crop is applied via
 * {@code vips extract_area}, scaling via {@code vips thumbnail}.
 */
@Slf4j
public class LibVipsProcessor implements ImageProcessor {

    private Boolean available = null;

    private final String binPath;

    public LibVipsProcessor() {
        this("vips");
    }

    public LibVipsProcessor(String binPath) {
        if (Strings.isNullOrEmpty(binPath)) {
            binPath = "vips";
        }
        this.binPath = binPath;
    }

    @Override
    public String name() {
        return "libvips";
    }

    @Override
    public boolean isAvailable() {
        if (available == null) {
            available = checkAvailable();
        }
        return available;
    }

    private boolean checkAvailable() {
        try {
            int exit = new ProcessBuilder(binPath, "--version")
                    .redirectErrorStream(true)
                    .start()
                    .waitFor();
            return exit == 0;
        } catch (Exception e) {
            log.debug("libvips not available: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void process(Path source, Path target, MediaFormat format, CropArea crop) throws IOException {
        Path inputForScale = source;

        if (crop != null) {
            String fileName = target.getFileName().toString();
            String ext = fileName.substring(fileName.lastIndexOf('.'));

            inputForScale = Files.createTempFile(
                    target.getParent(),
                    "crop-",
                    ext
            );
            runExtractArea(source, inputForScale, crop);
        }

        try {
            runThumbnail(inputForScale, target, format);
        } finally {
            if (crop != null) {
                Files.deleteIfExists(inputForScale);
            }
        }
    }

    private void runExtractArea(Path source, Path target, CropArea crop) throws IOException {
        // vips extract_area source.jpg target.jpg left top width height
        List<String> cmd = List.of(
                binPath, "extract_area",
                source.toString(),
                target.toString(),
                String.valueOf(crop.x()),
                String.valueOf(crop.y()),
                String.valueOf(crop.width()),
                String.valueOf(crop.height())
        );
        runCommand(cmd);
    }

    private void runThumbnail(Path source, Path target, MediaFormat format) throws IOException {
        // vips thumbnail source.jpg target.jpg width --height height --size both
        List<String> cmd = new ArrayList<>(List.of(
                binPath, "thumbnail",
                source.toString(),
                formatTarget(target, format),
                String.valueOf(format.width()),
                "--height", String.valueOf(format.height()),
                "--size", "both"
        ));
        runCommand(cmd);
    }

    /**
     * libvips determines output format from the file extension. The target Path
     * already has the correct extension from MediaUtils.
     */
    private String formatTarget(Path target, MediaFormat format) {
        return target.toString();
    }

    private void runCommand(List<String> cmd) throws IOException {
        log.debug("libvips: {}", String.join(" ", cmd));

        Process process = null;

        try {
            process = new ProcessBuilder(cmd)
                    .redirectErrorStream(true)
                    .start();

            String output;
            try (var reader = process.inputReader()) {
                output = reader.lines()
                        .reduce("", (a, b) -> a + System.lineSeparator() + b);
            }

            int exit = process.waitFor();

            if (exit != 0) {
                throw new IOException("""
                vips exited with code %d
                
                Command:
                %s
                
                Output:
                %s
                """.formatted(exit, String.join(" ", cmd), output));
            }

            if (!output.isBlank()) {
                log.debug("vips output:\n{}", output);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("vips interrupted", e);
        }
    }
}
