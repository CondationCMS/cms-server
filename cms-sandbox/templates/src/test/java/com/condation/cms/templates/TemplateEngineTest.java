package com.condation.cms.templates;

import com.condation.cms.templates.loaders.StringTemplateLoader;
import com.condation.cms.templates.tags.ElseIfTag;
import com.condation.cms.templates.tags.ElseTag;
import com.condation.cms.templates.tags.EndIfTag;
import com.condation.cms.templates.tags.IfTag;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author thmar
 */
public class TemplateEngineTest {

	TemplateEngine templateEngine;

	@BeforeEach
	void setupTemplateEngine() {
		TemplateConfiguration config = new TemplateConfiguration();
		config
				.registerTag(new IfTag())
				.registerTag(new ElseIfTag())
				.registerTag(new ElseTag())
				.registerTag(new EndIfTag());
		
		config.setTemplateLoader(new StringTemplateLoader().add("simple", "Hallo {{ name }}"));
		
		this.templateEngine = new TemplateEngine(config);
	}

	@Test
	public void testSomeMethod() {
		Template simpleTemplate = templateEngine.getTemplate("simple");
		
		Assertions.assertThat(simpleTemplate).isNotNull();
		System.out.println(simpleTemplate.execute());
	}

}
