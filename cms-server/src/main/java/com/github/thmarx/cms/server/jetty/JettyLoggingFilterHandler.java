package com.github.thmarx.cms.server.jetty;

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

import com.github.thmarx.cms.api.SiteProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.ThreadContext;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

/**
 *
 * @author t.marx
 */
@Slf4j
public class JettyLoggingFilterHandler extends Handler.Wrapper {

	private final SiteProperties siteProperties;
	
	public JettyLoggingFilterHandler (final Handler handler, final SiteProperties siteProperties) {
		super(handler);
		this.siteProperties = siteProperties;
	}
	
	@Override
	public boolean handle(Request rqst, Response rspns, Callback clbck) throws Exception {
		try {
			
			ThreadContext.put("site", siteProperties.id());
			log.debug("test log");
			return super.handle(rqst, rspns, clbck);
		}finally {
			ThreadContext.clearAll();
		}
	}
	
}
