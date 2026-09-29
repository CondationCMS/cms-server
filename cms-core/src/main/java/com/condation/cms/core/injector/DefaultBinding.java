package com.condation.cms.core.injector;

/*-
 * #%L
 * CMS Core
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
import com.condation.cms.api.injector.Binding;
import com.condation.cms.api.injector.Injector;
import java.util.function.Function;

/**
 *
 * @author thorstenmarx
 */
public class DefaultBinding<T> implements Binding {

	Function<Injector, T> function;

	boolean singleton = false;
	
	boolean eager = false;

	T singletonInstance;

	T newInstance(Injector injector) {
		if (!singleton) {
			return function.apply(injector);
		}

		synchronized (this) {
			if (singletonInstance == null) {
				singletonInstance = function.apply(injector);
			}
			return singletonInstance;
		}
	}

	@Override
	public void singleton() {
		this.singleton = true;
	}

	@Override
	public void eager() {
		this.singleton = true;
		this.eager = true;
	}
	
	

}
