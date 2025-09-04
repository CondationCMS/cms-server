package com.condation.cms.tests.template.xml;

/*-
 * #%L
 * tests
 * %%
 * Copyright (C) 2023 - 2025 CondationCMS
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
import com.condation.cms.tests.template.xml.ast.AstNode;
import com.condation.cms.tests.template.xml.ast.HtmlElementNode;
import com.condation.cms.tests.template.xml.ast.IfNode;
import com.condation.cms.tests.template.xml.ast.IncludeNode;
import com.condation.cms.tests.template.xml.ast.LoopNode;
import com.condation.cms.tests.template.xml.ast.TextNode;
import com.condation.cms.tests.template.xml.ast.ViewNode;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author thorstenmarx
 */
public class StAXTemplateRenderer implements TemplateLoader {

    private final List<AstNodeFactory> factories = new ArrayList<>();

    public StAXTemplateRenderer() {
        registerBuiltInFactories();
    }

    public void registerFactory(AstNodeFactory factory) {
        factories.add(0, factory); // first match wins
    }

    private void registerBuiltInFactories() {
        registerFactory(new AstNodeFactory() {
            @Override
            public boolean supports(String ns, String local) {
                return "view".equals(ns);
            }
            @Override
            public AstNode create(XMLStreamReader reader, StAXTemplateRenderer parser, HtmlElementNode current) throws XMLStreamException {
                String key = reader.getLocalName();
                parser.skipToEndElement(reader, reader.getPrefix(), key);
                return new ViewNode(key);
            }
        });

        registerFactory(new AstNodeFactory() {
            @Override
            public boolean supports(String ns, String local) {
                return "cms".equals(ns) && "if".equals(local);
            }
            @Override
            public AstNode create(XMLStreamReader reader, StAXTemplateRenderer parser, HtmlElementNode current) throws XMLStreamException {
                String var = reader.getAttributeValue(null, "var");
                IfNode node = new IfNode(var);
                parser.parseChildrenInto(reader, node);
                return node;
            }
        });

        registerFactory(new AstNodeFactory() {
            @Override
            public boolean supports(String ns, String local) {
                return "cms".equals(ns) && "loop".equals(local);
            }
            @Override
            public AstNode create(XMLStreamReader reader, StAXTemplateRenderer parser, HtmlElementNode current) throws XMLStreamException {
                String var = reader.getAttributeValue(null, "var");
                LoopNode node = new LoopNode(var);
                parser.parseChildrenInto(reader, node);
                return node;
            }
        });

        registerFactory(new AstNodeFactory() {
            @Override
            public boolean supports(String ns, String local) {
                return "cms".equals(ns) && "include".equals(local);
            }
            @Override
            public AstNode create(XMLStreamReader reader, StAXTemplateRenderer parser, HtmlElementNode current) throws XMLStreamException {
                String name = reader.getAttributeValue(null, "template");
                parser.skipToEndElement(reader, reader.getPrefix(), reader.getLocalName());
                return new IncludeNode(name, parser);
            }
        });

        registerFactory(new DefaultAstNodeFactory());
    }

    public AstNode parse(InputStream templateStream) throws XMLStreamException {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        XMLStreamReader reader = factory.createXMLStreamReader(templateStream);
        return parseNodes(reader);
    }

    private AstNode parseNodes(XMLStreamReader reader) throws XMLStreamException {
        HtmlElementNode root = new HtmlElementNode("root", Map.of());
        Deque<HtmlElementNode> stack = new ArrayDeque<>();
        stack.push(root);

        while (reader.hasNext()) {
            int event = reader.next();
            switch (event) {
                case XMLStreamConstants.START_ELEMENT -> {
                    String ns = reader.getPrefix();
                    String local = reader.getLocalName();

                    AstNode node = createAstNode(reader, ns, local, stack.peek());
                    if (node instanceof HtmlElementNode htmlNode) {
                        stack.peek().addChild(htmlNode);
                        stack.push(htmlNode);
                    } else {
                        stack.peek().addChild(node);
                    }
                }
                case XMLStreamConstants.CHARACTERS -> {
                    String text = reader.getText();
                    if (!text.trim().isEmpty()) {
                        stack.peek().addChild(new TextNode(text));
                    }
                }
                case XMLStreamConstants.END_ELEMENT -> {
                    String ns = reader.getPrefix();
                    if (!"view".equals(ns) && !"cms".equals(ns)) {
                        stack.pop();
                    }
                }
            }
        }

        return root;
    }

    private AstNode createAstNode(XMLStreamReader reader, String ns, String local, HtmlElementNode current) throws XMLStreamException {
        for (AstNodeFactory factory : factories) {
            if (factory.supports(ns, local)) {
                return factory.create(reader, this, current);
            }
        }
        throw new XMLStreamException("No suitable factory for " + ns + ":" + local);
    }

    public void parseChildrenInto(XMLStreamReader reader, AstNode parent) throws XMLStreamException {
        Deque<HtmlElementNode> stack = new ArrayDeque<>();
        HtmlElementNode dummy = new HtmlElementNode("dummy", Map.of());
        stack.push(dummy);

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String ns = reader.getPrefix();
                String local = reader.getLocalName();
                AstNode node = createAstNode(reader, ns, local, stack.peek());

                if (parent instanceof IfNode ifNode) ifNode.addChild(node);
                if (parent instanceof LoopNode loopNode) loopNode.addChild(node);
                if (node instanceof HtmlElementNode htmlNode) stack.push(htmlNode);
            } else if (event == XMLStreamConstants.CHARACTERS) {
                String text = reader.getText();
                if (!text.trim().isEmpty()) {
                    if (parent instanceof IfNode ifNode) ifNode.addChild(new TextNode(text));
                    if (parent instanceof LoopNode loopNode) loopNode.addChild(new TextNode(text));
                }
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                String prefix = reader.getPrefix();
                String local = reader.getLocalName();
                if ("cms".equals(prefix) && ("if".equals(local) || "loop".equals(local))) return;
                if (!stack.isEmpty()) stack.pop();
            }
        }
    }

    public void skipToEndElement(XMLStreamReader reader, String prefix, String localName) throws XMLStreamException {
        int depth = 1;
        while (reader.hasNext() && depth > 0) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT && prefix.equals(reader.getPrefix()) && localName.equals(reader.getLocalName())) depth++;
            if (event == XMLStreamConstants.END_ELEMENT && prefix.equals(reader.getPrefix()) && localName.equals(reader.getLocalName())) depth--;
        }
    }

    @Override
    public AstNode load(String name) throws Exception {
        String included = "<p>Included Template: " + name + "</p>";
        InputStream stream = new java.io.ByteArrayInputStream(included.getBytes());
        return parse(stream);
    }

    public static void main(String[] args) throws Exception {
        String template = """
            <page xmlns:view=\"https://cms/view\" xmlns:cms=\"https://cms\">
                <h1><view:title /></h1>
                <p>This is static HTML content.</p>
                <cms:if var=\"isLoggedIn\">
                    <p>Hello, <view:username /></p>
                </cms:if>
                <cms:loop var=\"items\">
                    <li><view:item /></li>
                </cms:loop>
                <cms:include template=\"footer\" />
            </page>
        """;

        Map<String, Object> context = new HashMap<>();
        context.put("title", "Welcome!");
        context.put("isLoggedIn", true);
        context.put("username", "Thorsten");
        context.put("items", List.of("One", "Two", "Three"));

        InputStream stream = new java.io.ByteArrayInputStream(template.getBytes());
        StAXTemplateRenderer renderer = new StAXTemplateRenderer();
        AstNode ast = renderer.parse(stream);

        System.out.println(ast.render(context));
    }
}