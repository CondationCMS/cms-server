package com.github.thmarx.cms.filesystem.datafilter.dimension;

/*-
 * #%L
 * cms-filesystem
 * %%
 * Copyright (C) 2023 Marx-Software
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;

import com.github.thmarx.cms.filesystem.datafilter.DataFilter;

public class SynchedDimension<K, V>
		extends
		AbstractIndex<K, V, ConcurrentSkipListMap<K, List<V>>> {

	public SynchedDimension(DataFilter<V> dataFilter) {
		super(new ConcurrentSkipListMap<K, List<V>>(), dataFilter);
	}

	@Override
	protected List<V> createList() {
		return Collections.synchronizedList(new ArrayList<V>());
	}
}
