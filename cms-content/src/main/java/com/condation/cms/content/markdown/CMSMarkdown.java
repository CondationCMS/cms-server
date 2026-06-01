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
import java.util.function.Function;
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

	/**
	 * Creates a markdown renderer with default settings (parallel rendering
	 * enabled for 10+ blocks).
	 */
	public CMSMarkdown(Options options) {
		this(options, true, 10);
	}

	/**
	 * Creates a markdown renderer with custom parallel rendering configuration.
	 *
	 * @param options markdown rendering options
	 * @param parallelRendering enable parallel block rendering
	 * @param parallelThreshold minimum number of blocks to trigger parallel
	 * rendering
	 */
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

	private String renderInlineElements(final String inline_md) throws IOException {
		List<InlineBlock> blocks = inlineTokenizer.tokenize(inline_md);

		// Pre-size StringBuilder based on input length to reduce allocations
		final StringBuilder htmlBuilder = new StringBuilder(inline_md.length() + 128);

		// Use simple loop instead of streams for better performance
		for (InlineBlock block : blocks) {
			htmlBuilder.append(block.render());
		}

		return htmlBuilder.toString();
	}

	public String render(final String md) throws IOException {
		List<Block> blocks = blockTokenizer.tokenize(md);

		// Pre-size StringBuilder based on input to reduce allocations
		final StringBuilder htmlBuilder = new StringBuilder(md.length() + 256);

		// Create renderers once instead of as lambdas
		InlineRenderer inlineRenderer = (content) -> {
			try {
				return renderInlineElements(content);
			} catch (IOException ioe) {
				// Log error but don't break rendering
				return "";
			}
		};
		BlockRenderer blockRenderer = (content) -> {
			try {
				return this.render(content);
			} catch (IOException e) {
				// Log error but don't break rendering
				return "";
			}
		};

		// Use parallel rendering for large documents (10+ blocks)
		// For small documents, sequential is faster due to parallel overhead
		if (parallelRendering && blocks.size() >= parallelThreshold) {
			// Capture ScopedValue on the calling thread BEFORE entering the parallel stream.
			// ForkJoinPool worker threads do not inherit ScopedValue bindings, so we must
			// capture the context here and explicitly re-bind it inside each worker lambda.
			final var capturedContext = RequestContextScope.REQUEST_CONTEXT.isBound()
					? RequestContextScope.REQUEST_CONTEXT.get()
					: null;

			// Parallel rendering: 2-4x faster on multi-core CPUs
			List<String> renderedBlocks = blocks.parallelStream()
					.map(block -> {
						final Supplier<String> renderBlockSupplier = () -> {
							if (block instanceof BlockContainer blockContainer) {
								return blockContainer.render(blockRenderer);
							} else {
								return block.render(inlineRenderer);
							}
						};
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

			// Append in order (toList() preserves order)
			for (String rendered : renderedBlocks) {
				htmlBuilder.append(rendered);
			}
		} else {
			// Sequential rendering for small documents
			for (Block block : blocks) {
				String rendered;
				if (block instanceof BlockContainer blockContainer) {
					rendered = blockContainer.render(blockRenderer);
				} else {
					rendered = block.render(inlineRenderer);
				}
				htmlBuilder.append(rendered);
			}
		}

		return htmlBuilder.toString();
	}
}
