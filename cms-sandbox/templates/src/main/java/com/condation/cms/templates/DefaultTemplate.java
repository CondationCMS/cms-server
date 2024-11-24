package com.condation.cms.templates;

import com.condation.cms.templates.parser.ASTNode;
import java.util.Map;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author thmar
 */
@RequiredArgsConstructor
public class DefaultTemplate implements Template {

	private final ASTNode rootNode;
	
	@Override
	public String execute(Map<String, Object> context) {
		return rootNode.toString();
	}
	
}
