package com.github.thmarx.cms.modules.search;

import java.util.List;

/**
 *
 * @author thmar
 */
public record IndexDocument(String uri, String title, String content, List<String> tags) {

}
