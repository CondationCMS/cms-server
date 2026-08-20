package com.condation.cms.templates;

/*-
 * #%L
 * CMS Templates
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

import com.condation.cms.content.template.functions.geo.GeoFunction;
import com.condation.cms.templates.loaders.StringTemplateLoader;
import java.io.IOException;
import java.util.HashMap;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class TemplateEngineGeoFunctionTest extends AbstractTemplateEngineTest {

	@Override
	public TemplateLoader getLoader() {
		return new StringTemplateLoader().add(
				"distance",
				"{{ cms.geo.distance(51.4818, 7.2162, 51.4882, 7.2160, 'km', 2) }} km");
	}

	@Test
	void rendersRoundedDistanceFromTheCmsGeoNamespace() throws IOException {
		var cms = new HashMap<String, Object>();
		cms.put("geo", new GeoFunction());
		var context = new HashMap<String, Object>();
		context.put("cms", cms);
		var result = SUT.getTemplate("distance").evaluate(context);

		Assertions.assertThat(result).isEqualToIgnoringWhitespace("0.71 km");
	}
}
