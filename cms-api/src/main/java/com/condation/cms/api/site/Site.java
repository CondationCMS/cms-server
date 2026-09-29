package com.condation.cms.api.site;

/*-
 * #%L
 * CMS Api
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
import com.condation.cms.api.SiteProperties;
import com.condation.cms.api.injector.Injector;
import java.util.List;

/**
 *
 * @author thmar
 */
public record Site(Injector injector) {

	public SiteDescriptor descriptor() {
		return SiteDescriptor.from(injector.getInstance(SiteProperties.class));
	}

	public String id() {
		return injector.getInstance(SiteProperties.class).id();
	}

	public List<String> modules() {
		return injector.getInstance(SiteProperties.class).activeModules();
	}

	public String baseurl() {
		return (String) injector.getInstance(SiteProperties.class).get("baseurl");
	}

	public boolean manager() {
		return injector.getInstance(SiteProperties.class).ui().managerEnabled();
	}

	public String realUrl() {
		return descriptor().realUrl();
	}

}
