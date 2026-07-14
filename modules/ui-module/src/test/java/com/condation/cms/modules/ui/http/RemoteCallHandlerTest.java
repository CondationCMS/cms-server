package com.condation.cms.modules.ui.http;

/*-
 * #%L
 * UI Module
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

import com.condation.cms.api.module.SiteModuleContext;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.ui.rpc.RPCError;
import com.condation.cms.api.ui.rpc.RPCException;
import com.condation.cms.api.ui.rpc.RPCResult;
import com.condation.cms.modules.ui.services.RemoteMethodService;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RemoteCallHandlerTest {

	@Mock
	private RemoteMethodService remoteMethodService;

	@Mock
	private SiteModuleContext moduleContext;

	@Mock
	private RequestContext requestContext;

	@Test
	void buildErrorResult_preservesCodeFromRPCException() throws Exception {
		var handler = new RemoteCallHandler(remoteMethodService, moduleContext, requestContext);

		Method buildErrorResult = RemoteCallHandler.class.getDeclaredMethod("buildErrorResult", Exception.class, String.class);
		buildErrorResult.setAccessible(true);

		RPCException exception = new RPCException(404, "content node not found");

		RPCResult result = (RPCResult) buildErrorResult.invoke(handler, exception, "workflow.transit");

		assertThat(result.error()).isEqualTo(new RPCError(404, "content node not found"));
	}

	@Test
	void buildErrorResult_fallsBackToCodeMinusOne_forGenericException() throws Exception {
		var handler = new RemoteCallHandler(remoteMethodService, moduleContext, requestContext);

		Method buildErrorResult = RemoteCallHandler.class.getDeclaredMethod("buildErrorResult", Exception.class, String.class);
		buildErrorResult.setAccessible(true);

		Exception exception = new IllegalStateException("boom");

		RPCResult result = (RPCResult) buildErrorResult.invoke(handler, exception, "some.method");

		assertThat(result.error()).isEqualTo(new RPCError("boom"));
		assertThat(result.error().code()).isEqualTo(-1);
	}
}
