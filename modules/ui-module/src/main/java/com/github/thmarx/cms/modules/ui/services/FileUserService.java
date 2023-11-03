package com.github.thmarx.cms.modules.ui.services;

import com.github.thmarx.cms.modules.ui.model.User;
import com.github.thmarx.cms.modules.ui.utils.Helper;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author marx
 */
@Slf4j
public class FileUserService implements UserService {
	
	public static final String FILENAME = "users.realm";
	
	String path;

	Map<String, User> userByName = new HashMap<>();

	final static Splitter userSplitter = Splitter.on(":").trimResults();
	final static Splitter groupSplitter = Splitter.on(",").trimResults();

	public FileUserService(final String path) {
		this.path = path;
		if (!path.endsWith("/")) {
			this.path += "/";
		}

		try {
			loadUsers();
		} catch (IOException ex) {
			log.error("", ex);
			throw new RuntimeException(ex);
		}
	}

	/**
	 * users are stored:
	 * <id>:<username>:<password.hash>:group,group
	 */
	private void loadUsers() throws IOException {
		File users = new File(path + FILENAME);
		if (users.exists()) {
			List<String> lines = Files.readLines(users, Charsets.UTF_8);

			for (String line : lines) {
				if (!line.startsWith("#")) {
					User user = User.fromString(line);
					userByName.put(user.username(), user);
				}
			}
		}
	}

	/**
	 * users are stored:
	 * <id>:<username>:<password.hash>:group,group
	 */
	private void saveUsers() throws IOException {
		File usersFile = new File(path + FILENAME);
		if (usersFile.exists()) {
			usersFile.createNewFile();
		}
		StringBuilder sb = new StringBuilder();
		userByName.values().stream().forEach((user) -> {
			sb.append(user.toString()).append("\r\n");
		});
		
		Files.write(sb.toString().getBytes(StandardCharsets.UTF_8), usersFile);
	}

	@Override
	public void add(User user) {
		if (user.id() == null) {
			
			if (userByName.containsKey(user.username())) {
				throw new RuntimeException("username already exists.");
			}
			
			user.id(UUID.randomUUID().toString());
		}
		
		try {
			
			final String hashedPassword = Helper.hash(user.password());
			user.password(hashedPassword);
			userByName.put(user.username(), user);
			
			saveUsers();
		} catch (Exception ex) {
			log.error("", ex);
			throw new RuntimeException(ex);
		}
	}

	@Override
	public User get(String username) {
		return userByName.get(username);
	}
	

	@Override
	public User login(final String username, final String password) {
		try {
			final String hashedPassword = Helper.hash(password);
			if (userByName.containsKey(username)) {
				if (userByName.get(username).password().equals(hashedPassword)) {
					return userByName.get(username);
				}
			}
			return null;
		} catch (Exception ex) {
			log.error("", ex);
		}
		return null;
	}

	@Override
	public void delete(User user) {
		userByName.remove(user.username());
		try {
			saveUsers();
		} catch (IOException ex) {
			log.error("", ex);
			throw new RuntimeException(ex);
		}
	}

	@Override
	public Collection<User> all() {
		return userByName.values();
	}
	
	
}