package com.condation.cms.extensions.repository;

/*-
 * #%L
 * cms-extensions
 * %%
 * Copyright (C) 2023 - 2024 CondationCMS
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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *
 * @author marx
 */
public class InstallationHelper {

	private final static int THRESHOLD_ENTRIES = 1000;
	private final static int THRESHOLD_SIZE = 100_000_000;
	private final static double THRESHOLD_RATIO = 10;

	/**
	 * Delets a dir recursively deleting anything inside it.
	 *
	 * @param dir The dir to delete
	 * @return true if the dir was successfully deleted
	 */
	public static boolean deleteDirectory(File dir) {
		if (!dir.exists() || !dir.isDirectory()) {
			return false;
		}

		String[] files = dir.list();
		for (int i = 0, len = files.length; i < len; i++) {
			File f = new File(dir, files[i]);
			if (f.isDirectory()) {
				deleteDirectory(f);
			} else {
				f.delete();
			}
		}
		return dir.delete();
	}

	public static boolean moveDirectoy(final File src, final File dest) {
		return src.renameTo(dest);
	}

	/**
	 * Unpack a zip file
	 *
	 * @param theFile
	 * @param targetDir
	 * @return the file
	 * @throws IOException
	 */
	public static File unpackArchive(File theFile, File targetDir) throws IOException {
		if (!theFile.exists()) {
			throw new IOException(theFile.getAbsolutePath() + " does not exist");
		}
		if (!buildDirectory(targetDir)) {
			throw new IOException("Could not create directory: " + targetDir);
		}
		boolean found = false;
		String moduleid = null;

		int totalSizeArchive = 0;
		int totalEntryArchive = 0;

		try (ZipFile zipFile = new ZipFile(theFile)) {
			for (Enumeration entries = zipFile.entries(); entries.hasMoreElements();) {

				totalEntryArchive++;

				ZipEntry entry = (ZipEntry) entries.nextElement();
				File file = new File(targetDir, File.separator + entry.getName());
				if (entry.isDirectory() && !found) {
					moduleid = file.getName();
					found = true;
				}
				if (!buildDirectory(file.getParentFile())) {
					throw new IOException("Could not create directory: " + file.getParentFile());
				}
				if (!entry.isDirectory()) {
					int totalSizeEntry = copyInputStream(entry, zipFile.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(file)));
					totalSizeArchive += totalSizeEntry;
				} else if (!buildDirectory(file)) {
					throw new IOException("Could not create directory: " + file);
				}
				if (totalSizeArchive > THRESHOLD_SIZE) {
					// the uncompressed data size is too much for the application resource capacity
					throw new InstallationSecurityException("max size (%d bytes) reached".formatted(THRESHOLD_SIZE));
				}

				if (totalEntryArchive > THRESHOLD_ENTRIES) {
					// too much entries in this archive, can lead to inodes exhaustion of the system
					throw new InstallationSecurityException("max entries (%d) reached".formatted(THRESHOLD_ENTRIES));
				}
			}
		}
		return new File(targetDir, moduleid);
	}

	private static int copyInputStream(ZipEntry ze, InputStream in, OutputStream out) throws IOException {
		int totalSizeEntry = 0;
		try (in; out) {
			byte[] buffer = new byte[1024];
			int len = -1;
			while ((len = in.read(buffer)) > 0) {
				totalSizeEntry += len;
				out.write(buffer, 0, len);

				double compressionRatio = totalSizeEntry / ze.getCompressedSize();
				if (compressionRatio > THRESHOLD_RATIO) {
					throw new InstallationSecurityException("compression ratio () to high, maybe zip bomb detected".formatted(THRESHOLD_RATIO));
				}
			}
		}
		return totalSizeEntry;
	}

	private static boolean buildDirectory(File file) {
		return file.exists() || file.mkdirs();
	}
}
