package com.condation.cms.modules.ui.http.auth;

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
import com.condation.cms.api.configuration.configs.ServerConfiguration;
import com.condation.cms.api.feature.features.ConfigurationFeature;
import com.condation.cms.api.module.SiteModuleContext;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.modules.ui.http.JettyHandler;
import com.condation.cms.modules.ui.utils.AuthUtil;
import com.condation.cms.modules.ui.utils.TokenUtils;
import com.condation.cms.modules.ui.utils.json.UIGsonProvider;
import java.time.Duration;
import java.util.Map;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Handler für den expliziten Refresh-Endpunkt.
 *
 * Die Route /manager/refresh verwendet hier das Refresh-Cookie, um bei Bedarf
 * neue Auth- und Refresh-Tokens zu setzen.
 */
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenHandler extends JettyHandler {

    private final SiteModuleContext moduleContext;
    private final RequestContext requestContext;

    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
        if (!request.getMethod().equalsIgnoreCase("POST")) {
            return false;
        }

        boolean refreshed = AuthUtil.refreshTokens(request, response, moduleContext, requestContext);
        if (refreshed) {
            var secret = moduleContext.get(ConfigurationFeature.class).configuration().get(ServerConfiguration.class).serverProperties().secret();
            response.setStatus(200);
            Content.Sink.write(response, true, UIGsonProvider.INSTANCE.toJson(Map.of(
                    "status", "ok",
                    // TODO: we can not access the name in this way
                    "previewToken", TokenUtils.createToken(getUsername(request, moduleContext, requestContext), secret, Duration.ofHours(1), Duration.ofDays(7))
            )), callback);
        } else {
            response.setStatus(401);
            Content.Sink.write(response, true, UIGsonProvider.INSTANCE.toJson(Map.of("status", "error", "reason", "unauthorized")), callback);
        }

        return true;
    }
}
