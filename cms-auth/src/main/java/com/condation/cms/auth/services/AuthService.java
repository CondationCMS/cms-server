package com.condation.cms.auth.services;

/*-
 * #%L
 * cms-auth
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

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;


/**
 *
 * @author t.marx
 */
@Slf4j
@RequiredArgsConstructor
public class AuthService {
	
	private static final String FILENAME = "auth.yaml";
	
	private final Path hostsBase;
	
	public Optional<Auth> load () {
		var authFile = hostsBase.resolve("config/auth.yaml");
		if (!Files.exists(authFile)) {
			return Optional.empty();
		}
		try (InputStream in = Files.newInputStream(authFile)) {
			return Optional.ofNullable(new Yaml().loadAs(in, Auth.class));
		} catch (Exception e) {
			log.error("error loading auth file", e);
			return Optional.empty();
		}
	}
	
	
	@AllArgsConstructor
	@NoArgsConstructor
	@Data
	public static class Auth {
	
		private List<AuthPath> paths;
		
		public Optional<AuthPath> find (final String path) {
			return paths.stream().filter(secPath -> path.startsWith(secPath.path)).findFirst();
		}
	}
	
	@AllArgsConstructor
	@NoArgsConstructor
	@Data
	public static class AuthPath {
		private String path;
		private String realm;
		private List<String> roles;
		
		public boolean allowed (User user) {
			if (user.roles() == null || user.roles().length == 0) {
				return false;
			}
			if (roles == null || roles.isEmpty()) {
				return false;
			}
			
			for (String role : user.roles()) {
				if (roles.contains(role)) {
					return true;
				}
			}
			
			return false;
		}
	}
}
