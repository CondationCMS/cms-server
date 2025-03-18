package com.condation.cms.git;

/*-
 * #%L
 * cms-git
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


import com.condation.cms.git.tasks.CloneTask;
import com.condation.cms.git.tasks.FetchTask;
import com.condation.cms.git.tasks.MergeTask;
import com.condation.cms.git.tasks.ResetTask;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class FlowTest {
	
	@Test
	void flow_test () throws Exception {
		var config = Config.load(Path.of("git.yaml"));
		
		Boolean clone = new CloneTask(config.getRepos().get(0)).call();
		
		if (clone) {
			System.out.println("clone done");
			
			var fetch = new FetchTask(config.getRepos().get(0)).call();
			
			if (fetch) {
				System.out.println("fetch done");
				var merge = new MergeTask(config.getRepos().get(0)).call();
				if (merge) {
					System.out.println("merged");
				} else {
					System.out.println("merge error");
					var reset = new ResetTask(config.getRepos().get(0)).call();
					System.out.println("reset " + reset);
				}
			} else {
				System.out.println("fetch error");
			}
			
		} else {
			System.out.println("clone error");
		}
	}
}
