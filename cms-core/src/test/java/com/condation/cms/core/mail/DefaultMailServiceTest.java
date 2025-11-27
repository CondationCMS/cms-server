package com.condation.cms.core.mail;

/*-
 * #%L
 * cms-core
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

import com.condation.cms.api.db.DB;
import com.condation.cms.api.db.DBFileSystem;
import com.condation.cms.api.mail.Message;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

/**
 *
 * @author thorstenmarx
 */
public class DefaultMailServiceTest {
	
	@TempDir
	Path tempDir;
	
	@RegisterExtension
	static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP);


	
	@Test
	public void test_mailer() throws IOException {
		
		greenMail.setUser("user1", "pass1");
		
		String yaml = """
            accounts:
              - name: default
                host: localhost
                fromMail: from@example.com
                port: %s
                username: user1
                password: pass1
			""".formatted(greenMail.getSmtp().getPort());
		Path configFile = createTempFile(yaml);
		
		var db = Mockito.mock(DB.class);
		var fileSystem = Mockito.mock(DBFileSystem.class);
		
		Mockito.when(db.getFileSystem()).thenReturn(fileSystem);
		Mockito.when(fileSystem.resolve("config/mail.yaml")).thenReturn(configFile);
		
		var mailService = new DefaultMailService(db);
		
		var message = new Message("noreply", new com.condation.cms.api.mail.Message.Recipient("to", "to@example.com"), "Hello", "Hello World!");
		
		mailService.sendText(message);
		
		Assertions.assertThat(greenMail.getReceivedMessagesForDomain("to@example.com")).hasSize(1);
	}
	
	
	// Helper Methode
	private Path createTempFile(String yamlContent) throws IOException {
		Path file = tempDir.resolve("mail.yaml");
		Files.write(file, yamlContent.getBytes(StandardCharsets.UTF_8));
		return file;
	}
}
