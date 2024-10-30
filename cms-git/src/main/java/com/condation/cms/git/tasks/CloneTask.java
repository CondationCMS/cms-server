package com.condation.cms.git.tasks;

import com.condation.cms.api.utils.ServerUtil;

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


import com.condation.cms.git.Repo;
import com.condation.cms.git.Task;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

/**
 *
 * @author t.marx
 */
@Slf4j
@RequiredArgsConstructor
public class CloneTask implements Task<Boolean> {

	private final Repo repo;
	
	@Override
	public Boolean call() throws Exception {
		
		Path targetFolder = ServerUtil.getPath(repo.getFolder());
		if (Files.exists(targetFolder)) {
			log.trace("repository already cloned");
			return Boolean.TRUE;
		}
		
		UsernamePasswordCredentialsProvider credentialProvider = new UsernamePasswordCredentialsProvider(
				repo.getCredentials().getUsername(), 
				repo.getCredentials().getPassword()
		);
		
		Git result = null;
		try {
			result = Git.cloneRepository()
				.setURI(repo.getUri())
				.setDirectory(targetFolder.toFile())
				.setBranchesToClone(Arrays.asList("refs/heads/" + repo.getBranch()))
				.setBranch("refs/heads/" + repo.getBranch())
				.setCredentialsProvider(credentialProvider)
				.call();
			
			return true;
		} finally {
			if (result != null) {
				result.close();
			}
		}
	}
}
