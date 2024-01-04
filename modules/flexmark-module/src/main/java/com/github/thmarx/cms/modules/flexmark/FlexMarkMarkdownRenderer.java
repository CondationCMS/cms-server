package com.github.thmarx.cms.modules.flexmark;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 Marx-Software
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
import com.github.thmarx.cms.api.feature.features.IsPreviewFeature;
import com.github.thmarx.cms.api.feature.features.SitePropertiesFeature;
import com.github.thmarx.cms.api.markdown.MarkdownRenderer;
import com.github.thmarx.cms.api.request.ThreadLocalRequestContext;
import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.AttributeProvider;
import com.vladsch.flexmark.html.AttributeProviderFactory;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.IndependentAttributeProviderFactory;
import com.vladsch.flexmark.html.renderer.AttributablePart;
import com.vladsch.flexmark.html.renderer.LinkResolverContext;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.NodeRendererFactory;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.TextCollectingVisitor;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.html.MutableAttributes;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author t.marx
 */
public class FlexMarkMarkdownRenderer implements MarkdownRenderer {
	
	final MutableDataSet options = new MutableDataSet();
	private final Parser parser;
	private final HtmlRenderer renderer;
	
	public FlexMarkMarkdownRenderer() {
		options.set(Parser.EXTENSIONS, List.of(
				TablesExtension.create(),
				AnchorLinkExtension.create(),
				UrlHandlingExtension.create()
		));
		options.set(HtmlRenderer.GENERATE_HEADER_ID, true);
		parser = Parser.builder(options).build();
		renderer = HtmlRenderer.builder(options).build();
	}
	
	@Override
	public String render(final String markdown) {
		Node document = parser.parse(markdown);
		return renderer.render(document);
	}
	
	@Override
	public String excerpt(final String markdown, final int length) {
		Node document = parser.parse(markdown);
		TextCollectingVisitor textCollectingVisitor = new TextCollectingVisitor();
		String text = textCollectingVisitor.collectAndGetText(document);
		
		if (text.length() <= length) {
			return text;
		} else {
			return text.substring(0, length);
		}
	}
	
	static class UrlHandlingExtension implements HtmlRenderer.HtmlRendererExtension {
		
		@Override
		public void rendererOptions(@NotNull MutableDataHolder options) {
			// add any configuration settings to options you want to apply to everything, here
		}
		
		@Override
		public void extend(@NotNull HtmlRenderer.Builder htmlRendererBuilder, @NotNull String rendererType) {
//			htmlRendererBuilder.nodeRendererFactory(new SampleNodeRenderer.Factory());
			htmlRendererBuilder.attributeProviderFactory(UrlAttributeProvider.Factory());
		}
		
		static UrlHandlingExtension create() {
			return new UrlHandlingExtension();
		}
	}
	
	static class UrlAttributeProvider implements AttributeProvider {
		
		@Override
		public void setAttributes(@NotNull Node node, @NotNull AttributablePart part, @NotNull MutableAttributes attributes) {
			if (node instanceof Link && part == AttributablePart.LINK) {
				Link link = (Link) node;
				
				var requestContext = ThreadLocalRequestContext.REQUEST_CONTEXT.get();
				
				var href = link.getUrl().toString();
				if (requestContext != null
						&& !href.startsWith("http") && !href.startsWith("https")) {
					
					if (requestContext.has(SitePropertiesFeature.class)) {
						var contextPath = requestContext.get(SitePropertiesFeature.class).siteProperties().contextPath();
						if (!"/".equals(contextPath) && !href.startsWith(contextPath) && href.startsWith("/")) {
							href = contextPath + href;
						}
					}
					if (requestContext.has(IsPreviewFeature.class)) {
						if (href.contains("?")) {
							href += "&preview";
						} else {
							href += "?preview";
						}
					}
					
					attributes.replaceValue("href", href);
				}
			}
		}
		
		public static AttributeProviderFactory Factory() {
			return new IndependentAttributeProviderFactory() {
				@NotNull
				@Override
				public AttributeProvider apply(@NotNull LinkResolverContext context) {
					return new UrlAttributeProvider();
				}
			};
		}
	}
	
	static class SampleNodeRenderer implements NodeRenderer {
		
		public SampleNodeRenderer(DataHolder options) {
			
		}
		
		@Override
		public Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
			return new HashSet<>(Arrays.asList(
					new NodeRenderingHandler<>(Link.class, this::render)
			));
		}
		
		private void render(Link node, NodeRendererContext context, HtmlWriter html) {
			var requestContext = ThreadLocalRequestContext.REQUEST_CONTEXT.get();
			var href = node.getUrl().toString();
			var text = node.getText();
			if (requestContext != null
					&& !href.startsWith("http") && !href.startsWith("https")) {
				
				var contextPath = requestContext.get(SitePropertiesFeature.class).siteProperties().contextPath();
				if (!"/".equals(contextPath) && !href.startsWith(contextPath) && href.startsWith("/")) {
					href = contextPath + href;
				}
				if (requestContext.has(IsPreviewFeature.class)) {
					if (href.contains("?")) {
						href += "&preview";
					} else {
						href += "?preview";
					}
				}
			}
			
			html.withAttr().tag("a").attr("href", href);
			html.text(text);
			html.tag("/a");
		}
		
		public static class Factory implements NodeRendererFactory {
			
			@NotNull
			@Override
			public NodeRenderer apply(@NotNull DataHolder options) {
				return new SampleNodeRenderer(options);
			}
		}
	}
}
