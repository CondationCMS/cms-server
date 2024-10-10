package com.condation.cms.content.shortcodes;

/*-
 * #%L
 * tests
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

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.apache.commons.jexl3.*;
import java.util.*;
import java.util.function.Function;

public class ShortCodes {

    private static final JexlEngine jexl = new JexlBuilder().create(); // Initialisierung des JEXL-Engines

    public static String parseShortcodes(String text, Codes codes) {
        // ANTLR Setup
        CharStream input = CharStreams.fromString(text);
        ShortCodeLexer lexer = new ShortCodeLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ShortCodeParser parser = new ShortCodeParser(tokens);
        ParseTree tree = parser.shortcodes(); // Parse the input

        // Listener Setup
        ShortCodeListenerImpl listener = new ShortCodeListenerImpl(codes);
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(listener, tree); // Walk the tree using the listener

        // Return the modified text after processing all shortcodes
        return listener.getResult();
    }

    // Listener class to process shortcodes
    public static class ShortCodeListenerImpl extends ShortCodeParserBaseListener {
        private final Codes codes;
        private final StringBuilder result = new StringBuilder(); // Stores the final output

        public ShortCodeListenerImpl(Codes codes) {
            this.codes = codes;
        }

		@Override
		public void enterText(ShortCodeParser.TextContext ctx) {
			result.append(ctx.getText());
		}

//		@Override
//		public void enterSelfClosingTag(ShortCodeParser.SelfClosingTagContext ctx) {
//			String name = ctx.TAG_NAME().getText();
//            Map<String, Object> parameters = parseParams(ctx.params());
//
//            // Apply shortcode function if exists
//            if (codes.hasCode(name)) {
//                result.append(codes.get(name).apply(parameters));
//            } else {
//                result.append(ctx.getText()); // No shortcode function found, append raw text
//            }
//		}
		
        @Override
        public void enterShortcodeWithContent(ShortCodeParser.ShortcodeWithContentContext ctx) {
			System.out.println("enterShortcodeWithContent: " + ctx.openingTag().TAG_NAME());
			
            String name = ctx.openingTag().TAG_NAME().getText();
            Map<String, Object> parameters = parseParams(ctx.openingTag().params());
            String content = ctx.content() != null ? ctx.content().getText() : "";

            // Apply shortcode function if exists
            if (codes.hasCode(name)) {
                parameters.put("content", content); // Pass content as parameter
                result.append(codes.get(name).apply(parameters));
            } else {
                result.append(ctx.getText()); // No shortcode function found, append raw text
            }
        }

        @Override
        public void enterSelfClosingShortcode(ShortCodeParser.SelfClosingShortcodeContext ctx) {
            String name = ctx.selfClosingTag().TAG_NAME().getText();
            Map<String, Object> parameters = parseParams(ctx.selfClosingTag().params());

            // Apply shortcode function if exists
            if (codes.hasCode(name)) {
                result.append(codes.get(name).apply(parameters));
            } else {
                result.append(ctx.getText()); // No shortcode function found, append raw text
            }
        }

        // Methode, um Parameter zu extrahieren
        private Map<String, Object> parseParams(ShortCodeParser.ParamsContext ctx) {
            Map<String, Object> params = new HashMap<>();
            if (ctx != null) {
                for (ShortCodeParser.ParamContext paramCtx : ctx.param()) {
                    String key = paramCtx.TAG_NAME().getText();
                    String rawValue = paramCtx.value().getText();
                    Object value = evaluateIfExpression(rawValue);
                    params.put(key, value);
                }
            }
            return params;
        }

        // Methode, um Ausdrücke innerhalb von ${} zu erkennen und mit JEXL auszuwerten
        private Object evaluateIfExpression(String rawValue) {
            // Prüfen, ob der Wert im Format ${expression} vorliegt
			var testValue = rawValue.replace("\"", "");
            if (testValue.startsWith("${") && testValue.endsWith("}")) {
				String expression = testValue.substring(2, testValue.length() - 1);
                return evaluateExpression(expression);
            } else {
                // Normaler Text
                return testValue;// Entfernt eventuell vorhandene Anführungszeichen
            }
        }

        // Methode, um einen JEXL-Ausdruck auszuwerten
        private Object evaluateExpression(String expression) {
            try {
                JexlExpression jexlExpression = jexl.createExpression(expression);
                JexlContext context = new MapContext(); // Leerer Kontext für einfache Auswertung
                return jexlExpression.evaluate(context); // Ausdruck auswerten
            } catch (Exception e) {
                e.printStackTrace();
                return null; // Wenn die Auswertung fehlschlägt, gib null zurück
            }
        }

        public String getResult() {
            return result.toString(); // Gibt das finale Ergebnis nach dem Parsen zurück
        }
    }

    // Codes class to store shortcode functions
    public static class Codes {
        private final Map<String, Function<Map<String, Object>, String>> codes = new HashMap<>();

        public void add(String codeName, Function<Map<String, Object>, String> function) {
            codes.put(codeName, function);
        }

        public boolean hasCode(String codeName) {
            return codes.containsKey(codeName);
        }

        public Function<Map<String, Object>, String> get(String codeName) {
            return codes.getOrDefault(codeName, (params) -> "");
        }
    }
}
