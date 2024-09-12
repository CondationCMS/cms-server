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


import com.condation.cms.git.TaskRunner;
import com.condation.cms.git.Config;
import com.condation.cms.git.tasks.CloneTask;
import com.condation.cms.git.tasks.FetchTask;
import com.condation.cms.git.tasks.MergeTask;
import com.condation.cms.git.tasks.ResetTask;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class FlowTest {
	
	TaskRunner runner = new TaskRunner();
	
	@Test
	void flow_test () throws IOException, InterruptedException, ExecutionException {
		var config = Config.load(Path.of("git.yaml"));
		
		Future<Boolean> clone = runner.execute(new CloneTask(config.getRepos().get(0)));
		
		if (clone.get()) {
			System.out.println("clone done");
			
			var fetch = runner.execute(new FetchTask(config.getRepos().get(0)));
			
			if (fetch.get()) {
				System.out.println("fetch done");
				var merge = runner.execute(new MergeTask(config.getRepos().get(0)));
				if (merge.get()) {
					System.out.println("merged");
				} else {
					System.out.println("merge error");
					var reset = runner.execute(new ResetTask(config.getRepos().get(0)));
					System.out.println("reset " + reset.get());
				}
			} else {
				System.out.println("fetch error");
			}
			
		} else {
			System.out.println("clone error");
		}
	}
}
