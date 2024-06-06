package com.github.thmarx.cms.cli.commands.extensions;

import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class InfoCommandTest {
	
	@Test
	public void testSomeMethod() {
		
		var cmd = new InfoCommand();
		cmd.setExtension("test-extension");
		cmd.run();
		
	}
	
}
