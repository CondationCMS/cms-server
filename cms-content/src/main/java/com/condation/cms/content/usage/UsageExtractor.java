package com.condation.cms.content.usage;

/*-
 * #%L
 * CMS Content
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

import com.condation.cms.api.ui.elements.*;
import com.condation.cms.api.ui.elements.fields.*;
import com.condation.cms.api.usage.*;
import com.condation.cms.api.utils.MapUtil;
import com.condation.cms.api.utils.SectionUtil;
import com.condation.cms.content.markdown.InlineElementTokenizer;
import com.condation.cms.content.markdown.Options;
import com.condation.cms.content.markdown.rules.inline.*;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.TextNode;

/** Static extraction only: no template evaluation, shortcode execution or media rendering. */
final class UsageExtractor {
    private final ContentTypes types;
    private final UsageSite site;
    private final List<UsageProblem> problems;
    private final InlineElementTokenizer tokenizer;
    private static final Pattern INLINE_CODE = Pattern.compile("(`+)([\\s\\S]*?)(?<!`)\\1(?!`)");

    UsageExtractor(ContentTypes types, UsageSite site, List<UsageProblem> problems) {
        this.types = types;
        this.site = site;
        this.problems = problems;
        var options = new Options();
        options.addInlineRule(new ImageLinkInlineRule(false));
        options.addInlineRule(new ImageInlineRule());
        options.addInlineRule(new LinkInlineRule(false));
        tokenizer = new InlineElementTokenizer(options);
    }

    List<UsageReference> extract(UsageResource source, Map<String, Object> metadata, String body) throws IOException {
        var result = new ArrayList<UsageReference>();
        var forms = forms(source, metadata);
        Set<String> listForms = new HashSet<>();
        forms.values().forEach(form -> fields(form).stream().filter(field -> field instanceof ListField)
                .forEach(field -> listForms.add(field.getName())));
        for (var entry : forms.entrySet()) {
            if (!listForms.contains(entry.getKey())) {
                extractForm(source, entry.getValue(), forms, metadata, "metadata", result, 0);
            }
        }
        extractText(body, "body", result);
        return List.copyOf(new LinkedHashSet<>(result));
    }

    private Map<String, FormDefinition> forms(UsageResource source, Map<String, Object> metadata) {
        if (source.kind() == UsageResource.Kind.COLLECTION_ITEM) {
            String collection = source.path().split("/", 2)[0];
            var type = types.getCollection(collection);
            if (type.isPresent()) return type.get().forms();
        } else {
            Object template = metadata.get("template");
            String name = source.path().substring(source.path().lastIndexOf('/') + 1);
            List<Map<String, FormDefinition>> matches;
            if (SectionUtil.isSectionEntry(name)) {
                matches = types.getSectionEntryTemplates(SectionUtil.getSectionName(name)).stream()
                        .filter(type -> type.template().equals(template)).map(SectionEntryTemplate::forms).toList();
            } else {
                matches = types.getPageTemplates().stream().filter(type -> type.template().equals(template))
                        .map(PageTemplate::forms).toList();
            }
            if (matches.size() == 1) return matches.getFirst();
        }
        problems.add(new UsageProblem(site.id(), source.path(), "Missing or ambiguous content type; only body references indexed"));
        return Map.of();
    }

    private void extractForm(UsageResource source, FormDefinition form, Map<String, FormDefinition> forms,
            Map<String, Object> values, String location, List<UsageReference> result, int depth) throws IOException {
        if (depth > 64) throw new IOException("Maximum form nesting depth exceeded");
        for (var field : fields(form)) {
            Object value = MapUtil.getValue(values, field.getName());
            if (value == null) continue;
            String fieldPath = location + "." + field.getName();
            if (field instanceof ListField && value instanceof List<?> items) {
                var itemForm = forms.get(field.getName());
                if (itemForm == null || fields(itemForm).isEmpty()) {
                    itemForm = types.getListItemTypes().stream().filter(type -> type.name().equals(field.getName()))
                            .map(ListItemType::form).findFirst().orElse(null);
                }
                if (itemForm == null) {
                    problems.add(new UsageProblem(site.id(), source.path(), "Missing list form: " + fieldPath));
                    continue;
                }
                for (int i = 0; i < items.size(); i++) {
                    if (items.get(i) instanceof Map<?, ?> item) {
                        @SuppressWarnings("unchecked") var map = (Map<String, Object>) item;
                        extractForm(source, itemForm, forms, map, fieldPath + "[" + i + "]", result, depth + 1);
                    }
                }
            } else if (value instanceof String text && !text.isBlank()) {
                if (field instanceof MediaField) {
                    result.add(new UsageReference(text, fieldPath, Usage.Origin.CONTENT_TYPE,
                            UsageResource.Kind.MEDIA, site.id(), null, false));
                } else if (field instanceof ReferenceField reference) {
                    result.add(new UsageReference(text, fieldPath, Usage.Origin.CONTENT_TYPE,
                            UsageResource.Kind.CONTENT, reference.getOptions().siteid(), null, false));
                } else if (field instanceof CollectionField collection) {
                    if (collection.getOptions().collection() == null || collection.getOptions().collection().isBlank()) {
                        problems.add(new UsageProblem(site.id(), source.path(), "Missing collection for " + fieldPath));
                    } else {
                        result.add(new UsageReference(text, fieldPath, Usage.Origin.CONTENT_TYPE,
                                UsageResource.Kind.COLLECTION_ITEM, site.id(), collection.getOptions().collection(), false));
                    }
                } else if (field instanceof MarkdownField || field instanceof EasyMdeField) {
                    extractText(text, fieldPath, result);
                }
            }
        }
    }

    private static List<FormField> fields(FormDefinition form) {
        var result = new ArrayList<>(form.fields());
        form.tabs().forEach(tab -> result.addAll(tab.fields()));
        return result;
    }

    private void extractText(String text, String location, List<UsageReference> result) throws IOException {
        var html = Jsoup.parseBodyFragment(maskCode(text));
        html.select("script,style,pre,code").remove();
        int elementIndex = 0;
        for (var element : html.getAllElements()) {
            String at = location + ".html[" + elementIndex++ + "]";
            for (String attribute : List.of("href", "src", "poster")) {
                if (element.hasAttr(attribute)) addPublic(result, element.attr(attribute), at + "." + attribute, Usage.Origin.HTML);
            }
            if (element.hasAttr("srcset")) {
                int candidate = 0;
                // URL tokens may contain commas (notably data URLs); descriptors end at a comma.
                String input = element.attr("srcset");
                int offset = 0;
                while (offset < input.length()) {
                    while (offset < input.length() && (Character.isWhitespace(input.charAt(offset)) || input.charAt(offset) == ',')) offset++;
                    int start = offset;
                    while (offset < input.length() && !Character.isWhitespace(input.charAt(offset))) offset++;
                    String url = input.substring(start, offset);
                    boolean ended = url.endsWith(",");
                    url = url.replaceAll(",+$", "");
                    addPublic(result, url, at + ".srcset[" + candidate++ + "]", Usage.Origin.HTML);
                    if (!ended) {
                        while (offset < input.length() && input.charAt(offset) != ',') offset++;
                    }
                }
            }
        }
        int textIndex = 0;
        for (var node : html.nodeStream().filter(node -> node instanceof TextNode).toList()) {
            String content = ((TextNode) node).getWholeText();
            for (var located : tokenizer.tokenize(content)) {
                String at = location + ".text[" + textIndex + "]@" + located.absoluteStart();
                switch (located.block()) {
                    case LinkInlineRule.LinkBlock link -> addMarkdownLink(result, link.href(), at);
                    case ImageInlineRule.ImageInlineBlock image -> addPublic(result, image.src(), at, Usage.Origin.MARKDOWN);
                    case ImageLinkInlineRule.ImageLinkBlock link -> {
                        addMarkdownLink(result, link.href(), at + ".link");
                        addPublic(result, link.imageSrc(), at + ".image", Usage.Origin.MARKDOWN);
                    }
                    default -> { }
                }
            }
            textIndex++;
        }
    }

    private void addMarkdownLink(List<UsageReference> result, String value, String location) {
        // LinkInlineRule uses HTTPUtil.prependContext. Preserve the original separately in the index.
        result.add(new UsageReference(value, location, Usage.Origin.MARKDOWN,
                UsageResource.Kind.CONTENT, null, null, true));
    }

    private static void addPublic(List<UsageReference> result, String value, String location, Usage.Origin origin) {
        if (!value.isBlank()) result.add(new UsageReference(value, location, origin, null, null, null, true));
    }

    private static String maskCode(String text) {
        var result = new StringBuilder();
        char fence = 0;
        int fenceLength = 0;
        for (String line : text.replace("\r\n", "\n").split("\n", -1)) {
            String trimmed = line.stripLeading();
            int run = 0;
            if (!trimmed.isEmpty() && (trimmed.charAt(0) == '`' || trimmed.charAt(0) == '~')) {
                while (run < trimmed.length() && trimmed.charAt(run) == trimmed.charAt(0)) run++;
            }
            boolean delimiter = run >= 3;
            if (fence != 0) {
                if (delimiter && trimmed.charAt(0) == fence && run >= fenceLength && trimmed.substring(run).isBlank()) fence = 0;
                result.append('\n');
            } else if (delimiter) {
                fence = trimmed.charAt(0);
                fenceLength = run;
                result.append('\n');
            } else if (line.startsWith("    ") || line.startsWith("\t")) {
                result.append('\n');
            } else {
                result.append(line).append('\n');
            }
        }
        return INLINE_CODE.matcher(result).replaceAll("");
    }
}
