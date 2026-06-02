package com.condation.cms.api.media;

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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */


/**
 *
 * @author thmar
 */
public class MediaUtils {

	public enum Format {
		PNG,
		JPEG,
		WEBP,
		AVIF;
	}

	public static Format format4String(final String format) {
		return switch (format) {
			case "webp" ->
				Format.WEBP;
			case "jpeg" ->
				Format.JPEG;
			case "jpg" -> 
				Format.JPEG;
			case "png" ->
				Format.PNG;
			case "avif" -> 
				Format.AVIF;
			default ->
				throw new RuntimeException("unknown image format");
		};
	}

	public static String mime4Format(final Format format) {
		return switch (format) {
			case JPEG ->
				"image/jpeg";
			case PNG ->
				"image/png";
			case WEBP ->
				"image/webp";
			case AVIF -> 
				"image/avif";
			default ->
				throw new RuntimeException("unknown image format");
		};
	}

	public static String fileending4Format(final Format format) {
		return switch (format) {
			case JPEG ->
				".jpeg";
			case PNG ->
				".png";
			case WEBP ->
				".webp";
			case AVIF ->
				".avif";
			default ->
				throw new RuntimeException("unknown image format");
		};
	}
}
