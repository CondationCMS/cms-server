package com.condation.cms.content.markdown;

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
import com.condation.cms.api.request.RequestContextScope;
import com.condation.cms.content.markdown.rules.block.ParagraphBlockRule;
import com.condation.cms.content.markdown.rules.inline.TextInlineRule;
import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

/**
 * CMS Markdown renderer with optional parallel block rendering. For documents
 * with 10+ blocks, parallel rendering provides 2-4x speedup on multi-core CPUs.
 *
 * @author t.marx
 */
public class CMSMarkdown {

	private final BlockTokenizer blockTokenizer;
	private final InlineElementTokenizer inlineTokenizer;

	private final List<BlockElementRule> blockRules;
	private final List<InlineElementRule> inlineRules;

	private final boolean parallelRendering;
	private final int parallelThreshold;

	public CMSMarkdown(Options options) {
		this(options, true, 10);
	}

	public CMSMarkdown(Options options, boolean parallelRendering, int parallelThreshold) {
		this.blockTokenizer = new BlockTokenizer(options);
		this.inlineTokenizer = new InlineElementTokenizer(options);
		this.parallelRendering = parallelRendering;
		this.parallelThreshold = parallelThreshold;
		blockRules = options.blockElementRules;
		blockRules.addLast(new ParagraphBlockRule());
		inlineRules = options.inlineElementRules;
		inlineRules.addLast(new TextInlineRule());
	}

	private String renderInlineElements(final String inline_md, int documentOffset) throws IOException {
		List<LocatedInlineBlock> blocks = inlineTokenizer.tokenize(inline_md, documentOffset);

		final StringBuilder htmlBuilder = new StringBuilder(inline_md.length() + 128);
		for (LocatedInlineBlock located : blocks) {
			htmlBuilder.append(located.block().render(located.absoluteStart(), located.absoluteEnd()));
		}
		return htmlBuilder.toString();
	}

	private String renderBlock(LocatedBlock located, InlineRenderer inlineRenderer, BlockRenderer blockRenderer) {
		Block block = located.block();
		if (block instanceof BlockContainer blockContainer) {
			return blockContainer.render(blockRenderer);
		}
		return block.render(inlineRenderer, located.absoluteStart());
	}

	public String render(final String md) throws IOException {
		List<LocatedBlock> blocks = blockTokenizer.tokenize(md);

		final StringBuilder htmlBuilder = new StringBuilder(md.length() + 256);

		InlineRenderer inlineRenderer = (content, documentOffset) -> {
			try {
				return renderInlineElements(content, documentOffset);
			} catch (IOException ioe) {
				return "";
			}
		};
		BlockRenderer blockRenderer = (content) -> {
			try {
				return this.render(content);
			} catch (IOException e) {
				return "";
			}
		};

		if (parallelRendering && blocks.size() >= parallelThreshold) {
			final var capturedContext = RequestContextScope.REQUEST_CONTEXT.isBound()
					? RequestContextScope.REQUEST_CONTEXT.get()
					: null;

			List<String> renderedBlocks = blocks.parallelStream()
					.map(located -> {
						final Supplier<String> renderBlockSupplier = () ->
								renderBlock(located, inlineRenderer, blockRenderer);
						try {
							if (capturedContext != null) {
								return ScopedValue.where(RequestContextScope.REQUEST_CONTEXT, capturedContext)
										.call(renderBlockSupplier::get);
							} else {
								return renderBlockSupplier.get();
							}
						} catch (Exception e) {
							throw new RuntimeException("error rendering blocks", e);
						}
					})
					.toList();

			for (String rendered : renderedBlocks) {
				htmlBuilder.append(rendered);
			}
		} else {
			for (LocatedBlock located : blocks) {
				htmlBuilder.append(renderBlock(located, inlineRenderer, blockRenderer));
			}
		}

		return htmlBuilder.toString();
	}
}
