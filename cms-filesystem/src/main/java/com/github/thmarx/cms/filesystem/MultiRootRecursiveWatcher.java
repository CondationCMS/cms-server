package com.github.thmarx.cms.filesystem;

/*-
 * #%L
 * cms-server
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
import com.github.thmarx.cms.api.utils.PathUtil;
import java.io.File;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.extern.slf4j.Slf4j;

/**
 * The recursive file watcher monitors a folder (and its sub-folders).
 */
@Slf4j
public class MultiRootRecursiveWatcher {

	private final AtomicBoolean running;

	private WatchService watchService;
	private Thread watchThread;
	private final Map<Path, WatchKey> watchPathKeyMap;

	private Timer timer;

	private final Map<Path, Root> roots;

	static record Root(Path path, SubmissionPublisher<FileEvent> publisher) {

	}

	public MultiRootRecursiveWatcher(List<Path> roots) {
		this.running = new AtomicBoolean(false);

		this.watchService = null;
		this.watchThread = null;
		this.watchPathKeyMap = new HashMap<>();

		this.timer = null;

		this.roots = new HashMap<>();
		roots.forEach(path -> {
			this.roots.put(path, new Root(path, new SubmissionPublisher<>()));
		});
	}

	public SubmissionPublisher<FileEvent> getPublisher(Path root) {

		if (this.roots.containsKey(root)) {
			return this.roots.get(root).publisher;
		}

		return null;
	}

	/**
	 * Starts the watcher service and registers watches in all of the sub-folders of the given root folder.
	 *
	 * <p>
	 * <b>Important:</b> This method returns immediately, even though the watches might not be in place yet. For large
	 * file trees, it might take several seconds until all directories are being monitored. For normal cases (1-100
	 * folders), this should not take longer than a few milliseconds.
	 */
	public void start() throws IOException {
		watchService = FileSystems.getDefault().newWatchService();

		watchThread = Thread.ofVirtual().name("Watcher").start(new Runnable() {
			@Override
			public void run() {
				running.set(true);
				walkTreeAndSetWatches();

				while (running.get()) {
					try {
						WatchKey watchKey = watchService.take();
						List<WatchEvent<?>> events = watchKey.pollEvents();

						events.forEach((event) -> {
							Path path = (Path) watchKey.watchable();
							File file = path.resolve((Path) event.context()).toFile();

							final FileEvent fileEvent;
							if (event.kind().equals(ENTRY_CREATE)) {
								fileEvent = new FileEvent(file, FileEvent.Type.CREATED);
							} else if (event.kind().equals(ENTRY_DELETE)) {
								fileEvent = new FileEvent(file, FileEvent.Type.DELETED);
							} else if (event.kind().equals(ENTRY_MODIFY)) {
								fileEvent = new FileEvent(file, FileEvent.Type.MODIFIED);
							} else {
								fileEvent = null;
							}

							if (fileEvent != null) {
								roots.values().forEach((root) -> {
									try {
										if (PathUtil.isChild(root.path, file.toPath())) {
											root.publisher.submit(fileEvent);
										}
									} catch (IOException ex) {
										log.error(null, ex);
									}
								});
							}
						});

						// fire events
						watchKey.reset();
						resetWaitSettlementTimer();
					} catch (InterruptedException | ClosedWatchServiceException e) {
						running.set(false);
					}
				}
			}
		});

		//watchThread.start();
	}

	public synchronized void stop() {
		if (watchThread != null) {
			try {
				roots.values().forEach(root -> root.publisher.close());
				watchService.close();
				running.set(false);
				watchThread.interrupt();
			} catch (IOException e) {
				// Don't care
			}
		}
	}

	private synchronized void resetWaitSettlementTimer() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}

		timer = new Timer("WatchTimer");
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				log.debug("File system actions (on watched folders) settled. Updating watches ...");
				walkTreeAndSetWatches();
				unregisterStaleWatches();
			}
		}, 10000);
	}

	private synchronized void walkTreeAndSetWatches() {
		log.debug("Registering new folders at watch service ...");

		roots.values().forEach(root -> {
			try {
				Files.walkFileTree(root.path, new FileVisitor<Path>() {
					@Override
					public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
						registerWatch(dir);
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
						return FileVisitResult.CONTINUE;
					}
				});
			} catch (IOException e) {
				// Don't care
			}
		});

	}

	private synchronized void unregisterStaleWatches() {
		Set<Path> paths = new HashSet<>(watchPathKeyMap.keySet());
		Set<Path> stalePaths = new HashSet<>();

		for (Path path : paths) {
			if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
				stalePaths.add(path);
			}
		}

		if (!stalePaths.isEmpty()) {
			log.debug("Cancelling stale path watches ...");

			for (Path stalePath : stalePaths) {
				unregisterWatch(stalePath);
			}
		}
	}

	private synchronized void registerWatch(Path dir) {
		if (!watchPathKeyMap.containsKey(dir)) {
			log.debug("- Registering " + dir);

			try {
				WatchKey watchKey = dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY, OVERFLOW);
				watchPathKeyMap.put(dir, watchKey);
			} catch (IOException e) {
				// Don't care!
			}
		}
	}

	private synchronized void unregisterWatch(Path dir) {
		WatchKey watchKey = watchPathKeyMap.get(dir);

		if (watchKey != null) {
			log.debug("- Cancelling " + dir);

			watchKey.cancel();
			watchPathKeyMap.remove(dir);
		}
	}

	public interface WatchListener {

		public void watchEventsOccurred();
	}

	public abstract static class AbstractFileEventSubscriber implements Flow.Subscriber<FileEvent> {

		protected Flow.Subscription subscription;

		@Override
		public void onSubscribe(Flow.Subscription subscription) {
			this.subscription = subscription;
			this.subscription.request(1);
		}

		@Override
		public void onError(Throwable throwable) {
		}

		@Override
		public void onComplete() {
		}
	}
}
