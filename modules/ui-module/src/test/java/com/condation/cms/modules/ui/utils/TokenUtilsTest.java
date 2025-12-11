package com.condation.cms.modules.ui.utils;

/*-
 * #%L
 * ui-module
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

import io.jsonwebtoken.Jwts;
import java.time.Duration;
import java.util.Base64;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author thorstenmarx
 */
public class TokenUtilsTest {
	
	private String key () {
		return Base64.getEncoder()
                .encodeToString(Jwts.SIG.HS256.key().build().getEncoded());
	}
	
	@Test
	public void create_and_validate() throws Exception {
		
		var secret = key();
		
		var token = TokenUtils.createToken("condation", secret, Duration.ofHours(1), Duration.ofHours(1));
		
		var payload = TokenUtils.getPayload(token, secret);
		Assertions.assertThat(payload).isPresent();
		Assertions.assertThat(payload.get().username()).isEqualTo("condation");
	}

	@Test
	public void create_and_validate__wrong_secret() throws Exception {
		
		var secret = key();
		
		var token = TokenUtils.createToken("condation", secret, Duration.ofHours(1), Duration.ofHours(1));

		var payload = TokenUtils.getPayload(token, "another secret");
		Assertions.assertThat(payload).isNotPresent();
	}

	@Test
	public void create_and_validate__wrong_token() throws Exception {
		
		var secret = key();
		
		var payload = TokenUtils.getPayload("bliblablub", secret);
		Assertions.assertThat(payload).isNotPresent();
	}
	
}
