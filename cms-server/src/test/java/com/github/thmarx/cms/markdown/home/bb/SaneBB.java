package com.github.thmarx.cms.markdown.home.bb;

/*-
 * #%L
 * cms-server
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

// SaneBB - public domain
// Niels Martignène <niels.martignene@protonmail.com>
//
// This software is in the public domain. Where that dedication is not
// recognized, you are granted a perpetual, irrevocable license to copy,
// distribute, and modify this file as you see fit.
//
// See the LICENSE file for more details.

// Fast BBCode to HTML converter
//
// Specific choices:
//
//     Error-tolerant, malformed tags are written untransformed
//     Auto-close tags: [i][b]Allow[/i] is turned into <i><b>Bold Italic</b></i>
//     Short form close tags ([/]) are supported
//     White spaces and casing in tags (e.g. [ CoLoR=green ]) is ignored
//
// Simple API (no options):
//
//     SaneBB.Result result = SaneBB.parse("[b]Hello World[/b]");
//     System.out.printf("HTML : %s (errors: %b)", result.html, result.errors);
//
// Complex API (supports options):
//
//     SaneBB parser = new SaneBB();
//     SaneBB.Result result = parser.parseAll("[b]Hello World[/b]");
//     System.out.printf("HTML : %s (errors: %b)", result.html, result.errors);
//
// Options:
//
//     parser.url_transformer: Set to a UnaryOperator<String> before parsing, return final URL as string
//                             or null to refuse. By default, http: and https: URLs are returned as-is and
//                             everything else is refused.
//
// Supported tags:
//
//     [b]Bold[/b]
//     [i]Italic[/i]
//     [u]Underline[/u]
//     [s]Strike through[/s]
//     [left]Align left[/left]
//     [center]Align center[/center]
//     [right]Align right[/right]
//     [code]Code[/code]
//     [quote]Quote[/quote]
//     [font=FONT]Change font family[/font]
//     [size=SIZE]Change font size[/size] (SIZE must be a number)
//     [color=COLOR]Change text color[/color]
//     [img]Image URL[/img]
//     [img=WIDTHxHEIGHT]Image URL[/img] (WIDTH and HEIGHT must be numbers)
//     [url]Link URL[/url]
//     [url=URL]Text[/url]
//     [ul][li]Unordered item 1 [*] Unordered item 2[/ul] ([li] and [*] are equivalent, closing them is optional)
//     [list][/list] (same as [ul])
//     [ol][li]Ordered item 1 [*] Ordered item 2[/ol]
//     [table][tr][th]Header cell 1[/th][th]Header cell 2[/th][/tr][tr][td]Cell 1[/td][td]Cell 2[/td][/tr][/table]

import java.util.ArrayList;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.net.URL;

public class SaneBB {
    String code;
    StringBuilder html;
    boolean errors;
    int len;
    int offset;
    Token token;
    ArrayList<OpenTag> tags;

    // Options
    public UnaryOperator<String> url_transformer = SaneBB::transformUrlDefault;

    public static Result parse(String code) {
        SaneBB parser = new SaneBB();
        return parser.parseAll(code);
    }

    public Result parseAll(String code) {
        this.code = code;
        this.html = new StringBuilder();
        this.errors = false;
        this.len = this.code.length();
        this.offset = 0;
        this.token = new Token();
        this.tags = new ArrayList<OpenTag>();

        parseScope((String ident) -> parseDefaultScope(ident));

        for (int i = tags.size() - 1; i >= 0; i--) {
            OpenTag tag = tags.get(i);
            html.append(tag.html);
        }

        return new Result(html.toString(), errors);
    }

    void parseScope(Predicate<String> func) {
        int first_tag = tags.size();

        while (copyUntilToken()) {
            boolean success = false;

            int start = offset++;
            readToken();
            int next = offset;

            if (token.type == Token.Type.STRING) {
                // Limit global nesting depth
                if (tags.size() < 16) {
                    String ident = token.str.toLowerCase();
                    success = func.test(ident);
                }
            } else if (token.type == Token.Type.SLASH) {
                readToken();

                int idx;
                if (token.type == Token.Type.STRING) {
                    String match = token.str;

                    if (expectToken(Token.Type.RIGHT_BRACKET)) {
                        idx = findMatchingTag(match);
                    } else {
                        idx = -1;
                    }
                } else if (token.type == Token.Type.RIGHT_BRACKET) {
                    idx = tags.size() - 1;
                } else {
                    idx = -1;
                }

                if (idx >= 0) {
                    if (idx >= first_tag) {
                        while (tags.size() > idx) {
                            OpenTag tag = tags.remove(tags.size() - 1);
                            html.append(tag.html);
                        }

                        success = true;
                    } else {
                        // This tag belongs to a parent scope, we need to walk back there
                        // and let him deal with the closing tag.

                        offset = start;
                        return;
                    }
                }
            }

            if (!success) {
                html.append(code, start, next);
                offset = next;

                errors = true;
            }
        }
    }

    int findMatchingTag(String match) {
        match = match.toLowerCase();

        for (int i = tags.size() - 1; i >= 0; i--) {
            OpenTag tag = tags.get(i);

            if (tag.tag.equals(match))
                return i;
        }

        return -1;
    }

    boolean parseDefaultScope(String ident) {
        switch (ident) {
            case "b": return parseBasicTag("b", "<b>", "</b>", null);
            case "i": return parseBasicTag("i", "<i>", "</i>", null);
            case "u": return parseBasicTag("u", "<u>", "</u>", null);
            case "s": return parseBasicTag("s", "<s>", "</s>", null);
            case "left": return parseBasicTag("left", "<div style=\"text-align: left;\">", "</div>", null);
            case "center": return parseBasicTag("center", "<div style=\"text-align: center;\">", "</div>", null);
            case "right": return parseBasicTag("right", "<div style=\"text-align: right;\">", "</div>", null);
            case "code": return parseBasicTag("code", "<pre>", "</pre>", null);
            case "quote": return parseBasicTag("quote", "<blockquote>", "</blockquote>", null);

            case "font": return parseStyleTag("font", "font-family", Token.Type.STRING);
            case "size": return parseStyleTag("size", "font-size", Token.Type.NUMBER);
            case "color": return parseStyleTag("color", "color", Token.Type.STRING);

            case "url": {
                readToken();

                if (token.type == Token.Type.EQUAL) {
                    boolean success = true;
                    String href;

                    success &= expectToken(Token.Type.STRING); href = token.str;
                    success &= expectToken(Token.Type.RIGHT_BRACKET);
                    href = url_transformer.apply(href); success &= (href != null);

                    if (success) {
                        tags.add(new OpenTag("url", "</a>"));
                        html.append("<a href=\""); appendSafe(href);
                        html.append("\">");

                        return true;
                    }
                } else if (token.type == Token.Type.RIGHT_BRACKET) {
                    boolean success = true;
                    String href;

                    success &= expectToken(Token.Type.STRING); href = token.str;
                    success &= parseCloseTag("url");
                    href = url_transformer.apply(href); success &= (href != null);

                    if (success) {
                        html.append("<a href=\""); appendSafe(href);
                        html.append("\">"); appendSafe(href);
                        html.append("</a>");

                        return true;
                    }
                }
            } break;

            case "img": {
                readToken();

                if (token.type == Token.Type.EQUAL) {
                    boolean success = true;
                    String width;
                    String height;
                    String src;

                    success &= expectToken(Token.Type.NUMBER); width = token.str;
                    success &= offset < len && code.charAt(offset++) == 'x';
                    success &= expectToken(Token.Type.NUMBER); height = token.str;
                    success &= expectToken(Token.Type.RIGHT_BRACKET);
                    success &= expectToken(Token.Type.STRING); src = token.str;
                    success &= parseCloseTag("img");
                    src = url_transformer.apply(src); success &= (src != null);

                    if (success) {
                        html.append("<img src=\""); appendSafe(src);
                        html.append("\" width=\""); html.append(width);
                        html.append("\" height=\""); html.append(height);
                        html.append("\" alt=\"\"/>");

                        return true;
                    }
                } else if (token.type == Token.Type.RIGHT_BRACKET) {
                    boolean success = true;
                    String src;

                    success &= expectToken(Token.Type.STRING); src = token.str;
                    success &= parseCloseTag("img");
                    src = url_transformer.apply(src); success &= (src != null);

                    if (success) {
                        html.append("<img src=\""); appendSafe(src);
                        html.append("\" alt=\"\"/>");

                        return true;
                    }
                }
            } break;

            case "ul": return parseBasicTag("ul", "<ul>", "</ul>", (String id) -> parseListScope(id, false));
            case "list": return parseBasicTag("list", "<ul>", "</ul>", (String id) -> parseListScope(id, false));
            case "ol": return parseBasicTag("ol", "<ol>", "</ol>", (String id) -> parseListScope(id, false));
            case "table": return parseBasicTag("table", "<table>", "</table>", (String id) -> parseTableScope(id));
        }

        return false;
    }

    boolean parseListScope(String ident, boolean inception) {
        if (inception) {
            if (ident.equals("li") || ident.equals("*")) {
                if (expectToken(Token.Type.RIGHT_BRACKET)) {
                    OpenTag tag = tags.get(tags.size() - 1);
                    assert tag.tag == "li" || tag.tag == "*";

                    tag.tag = ident;
                    html.append("</li><li>");

                    return true;
                }
            } else {
                return parseDefaultScope(ident);
            }
        } else if (ident.equals("li") || ident.equals("*")) {
            return parseBasicTag(ident, "<li>", "</li>", (String id) -> parseListScope(id, true));
        }

        return false;
    }

    boolean parseTableScope(String ident) {
        switch (ident) {
            case "tr": return parseBasicTag("tr", "<tr>", "</tr>", null);
            case "th": return parseBasicTag("th", "<th>", "</th>", (String id) -> parseDefaultScope(id));
            case "td": return parseBasicTag("td", "<td>", "</td>", (String id) -> parseDefaultScope(id));
        }

        return false;
    }

    boolean parseBasicTag(String tag, String open, String close, Predicate<String> scope) {
        if (expectToken(Token.Type.RIGHT_BRACKET)) {
            tags.add(new OpenTag(tag, close));
            html.append(open);

            if (scope != null) {
                parseScope(scope);
            }

            return true;
        } else {
            return false;
        }
    }

    boolean parseStyleTag(String tag, String attr, Token.Type type) {
        boolean success = true;
        String value;

        success &= expectToken(Token.Type.EQUAL);
        success &= expectToken(type); value = token.str;
        success &= expectToken(Token.Type.RIGHT_BRACKET);

        if (success) {
            tags.add(new OpenTag(tag, "</span>"));
            html.append("<span style=\""); html.append(attr);
            html.append(": "); appendSafe(value);
            html.append(";\">");

            return true;
        } else {
            return false;
        }
    }

    boolean parseCloseTag(String tag) {
        boolean success = true;

        success &= expectToken(Token.Type.LEFT_BRACKET);
        success &= expectToken(Token.Type.SLASH);
        readToken();
        if (token.type == Token.Type.STRING) {
            success &= token.str.equalsIgnoreCase(tag);
            readToken();
        }
        success &= (token.type == Token.Type.RIGHT_BRACKET);

        return success;
    }

    boolean copyUntilToken() {
        for (;;) {
            if (offset >= len)
                return false;

            char c = code.charAt(offset);

            if (c == '[') {
                break;
            } else if (c == '\n') {
                html.append("<br/>\n");
            } else {
                appendSafe(c);
            }

            offset++;
        }

        return true;
    }

    void readToken() {
        // Skip white spaces
        while (offset < len && isWhiteChar(code.charAt(offset))) {
            offset++;
        }

        // Parse token
        if (offset < len) {
            char c = code.charAt(offset);
            int next = offset + 1;

            switch (c) {
                case '/': { token.type = Token.Type.SLASH; } break;
                case '=': { token.type = Token.Type.EQUAL; } break;
                case '[': { token.type = Token.Type.LEFT_BRACKET; } break;
                case ']': { token.type = Token.Type.RIGHT_BRACKET; } break;

                default: {
                    if (isNumberChar(c)) {
                        while (next < len && isNumberChar(code.charAt(next))) {
                            next++;
                        }

                        token.type = Token.Type.NUMBER;
                        token.str = code.substring(offset, next);
                    } else if (isStringChar(c)) {
                        while (next < len && isStringChar(code.charAt(next))) {
                            next++;
                        }

                        token.type = Token.Type.STRING;
                        token.str = code.substring(offset, next);
                    } else {
                        token.type = Token.Type.MALFORMED;
                    }
                } break;
            }

            offset = next;
        } else {
            token.type = Token.Type.MALFORMED;
        }
    }

    boolean expectToken(Token.Type type) {
        readToken();
        return token.type == type;
    }

    boolean isWhiteChar(char c) { return c == ' ' || c == '\t'; }
    boolean isStringChar(char c) { return !isWhiteChar(c) && c >= 32 && c != '[' && c != ']' && c != '='; }
    boolean isNumberChar(char c) { return (c >= '0' && c <= '9'); }

    void appendSafe(String str) {
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            appendSafe(c);
        }
    }

    void appendSafe(char c) {
        switch (c) {
            case '<': { html.append("&lt;"); } break;
            case '>': { html.append("&gt;"); } break;
            case '&': { html.append("&amp;"); } break;
            case '"': { html.append("&quot;"); } break;
            case '\t': { html.append('\t'); } break;
            case ';': { html.append("&#59;"); } break;

            default: {
                if (c >= 32) {
                    html.append(c);
                }
            } break;
        }
    }

    public static String transformUrlDefault(String str) {
        try {
            URL url = new URL(str);
            String protocol = url.getProtocol();

            if (protocol.equals("http") || protocol.equals("https")) {
                return str;
            } else {
                return null;
            }
        } catch (java.net.MalformedURLException e) {
            return null;
        }
    }
}
