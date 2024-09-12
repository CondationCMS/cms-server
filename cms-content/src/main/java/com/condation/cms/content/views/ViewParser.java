package com.condation.cms.content.views;

/*-
 * #%L
 * cms-content
 * %%
 * Copyright (C) 2023 - 2024 CondationCMS
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

import com.condation.cms.content.views.model.View;
import java.io.IOException;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;
import com.condation.cms.api.db.cms.ReadOnlyFile;
/**
 *
 * @author t.marx
 */
public class ViewParser {

	public static View parse(final ReadOnlyFile viewFile) throws IOException {
		Representer representer = new Representer(new DumperOptions());
		representer.getPropertyUtils().setSkipMissingProperties(true);
		LoaderOptions loaderOptions = new LoaderOptions();
		Constructor constructor = new Constructor(View.class, loaderOptions);
		Yaml yaml = new Yaml(constructor, representer);

		return yaml.loadAs(viewFile.getContent(), View.class);
	}
}
