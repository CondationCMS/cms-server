package com.github.thmarx.cms.api;

/*-
 * #%L
 * cms-api
 * %%
 * Copyright (C) 2023 Marx-Software
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

/**
 *
 * @author thmar
 */
public class Media {

	public enum Format {
		PNG,
		JPEG,
		WEBP;
	}

	public static Format format4String(final String format) {
		return switch (format) {
			case "webp" ->
				Format.WEBP;
			case "jpeg" ->
				Format.JPEG;
			case "png" ->
				Format.PNG;
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
			default ->
				throw new RuntimeException("unknown image format");
		};
	}
}
