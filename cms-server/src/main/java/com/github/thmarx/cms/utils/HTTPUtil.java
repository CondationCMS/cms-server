package com.github.thmarx.cms.utils;

import com.google.common.base.Strings;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 * @author t.marx
 */
public class HTTPUtil {

	public static Map<String, List<String>> queryParameters(String query) {
		if (Strings.isNullOrEmpty(query)) {
			return Collections.emptyMap();
		}
		return Pattern.compile("&")
				.splitAsStream(query)
				.map(s -> Arrays.copyOf(s.split("=", 2), 2))
				.collect(Collectors.groupingBy(s -> decode(s[0]), Collectors.mapping(s -> decode(s[1]), Collectors.toList())));
	}

	private static String decode(final String encoded) {
		return Optional.ofNullable(encoded)
				.map(e -> URLDecoder.decode(e, StandardCharsets.UTF_8))
				.orElse(null);
	}
}
