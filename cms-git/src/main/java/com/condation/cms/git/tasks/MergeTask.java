package com.condation.cms.git.tasks;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
@Slf4j
public class MergeTask implements Task<Boolean> {

	private final Repo repo;

	@Override
	public Boolean call() throws Exception {

		Path targetFolder = Path.of(repo.getFolder());
		if (!Files.exists(targetFolder)) {
			log.trace("target folder does not exists");
			return Boolean.FALSE;
		}

		UsernamePasswordCredentialsProvider credentialProvider = new UsernamePasswordCredentialsProvider(
				repo.getCredentials().getUsername(),
				repo.getCredentials().getPassword()
		);

		Git git_repo = Git.open(new File(repo.getFolder()));
		try {

			var result = git_repo.merge()
					.include(git_repo.getRepository().findRef("origin/" + repo.getBranch()))
					.call();
			return result.getConflicts() == null || result.getConflicts().isEmpty();
		} finally {
			if (git_repo != null) {
				git_repo.close();
			}
		}
	}

}
