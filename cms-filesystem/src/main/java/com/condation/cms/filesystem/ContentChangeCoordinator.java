package com.condation.cms.filesystem;

/*-
 * #%L
 * CMS FileSystem
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class ContentChangeCoordinator implements AutoCloseable {

	private final Object lock = new Object();
	private final Duration quietPeriod;
	private final BiConsumer<Boolean, Set<Path>> batchProcessor;
	private final ScheduledExecutorService executor;
	private final Set<Path> pendingPaths = new LinkedHashSet<>();

	private ScheduledFuture<?> scheduledFlush;
	private long generation;
	private boolean fullResync;
	private boolean closed;

	ContentChangeCoordinator(Duration quietPeriod, BiConsumer<Boolean, Set<Path>> batchProcessor) {
		this.quietPeriod = quietPeriod;
		this.batchProcessor = batchProcessor;
		this.executor = Executors.newSingleThreadScheduledExecutor(
				Thread.ofVirtual().name("content-change-coordinator", 0).factory());
	}

	void submit(Path path) {
		synchronized (lock) {
			if (closed || fullResync) {
				return;
			}
			pendingPaths.add(path.toAbsolutePath().normalize());
			scheduleFlush(quietPeriod.toMillis(), ++generation);
		}
	}

	void requestFullResync() {
		synchronized (lock) {
			if (closed) {
				return;
			}
			fullResync = true;
			pendingPaths.clear();
			scheduleFlush(0, ++generation);
		}
	}

	void flushNow() {
		try {
			var future = executor.submit(this::forceFlush);
			future.get();
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		} catch (Exception ex) {
			log.error("error while flushing content changes", ex);
		}
	}

	private void scheduleFlush(long delayMillis, long scheduledGeneration) {
		if (scheduledFlush != null) {
			scheduledFlush.cancel(false);
		}
		scheduledFlush = executor.schedule(
				() -> flush(scheduledGeneration), delayMillis, TimeUnit.MILLISECONDS);
	}

	private void flush(long scheduledGeneration) {
		final boolean resync;
		final Set<Path> paths;
		synchronized (lock) {
			if (scheduledGeneration != generation) {
				return;
			}
			resync = fullResync;
			paths = Collections.unmodifiableSet(new LinkedHashSet<>(pendingPaths));
			fullResync = false;
			pendingPaths.clear();
			scheduledFlush = null;
		}

		process(resync, paths);
	}

	private void forceFlush() {
		final boolean resync;
		final Set<Path> paths;
		synchronized (lock) {
			generation++;
			if (scheduledFlush != null) {
				scheduledFlush.cancel(false);
			}
			resync = fullResync;
			paths = Collections.unmodifiableSet(new LinkedHashSet<>(pendingPaths));
			fullResync = false;
			pendingPaths.clear();
			scheduledFlush = null;
		}
		process(resync, paths);
	}

	private void process(boolean resync, Set<Path> paths) {
		if (!resync && paths.isEmpty()) {
			return;
		}

		try {
			batchProcessor.accept(resync, paths);
		} catch (Exception ex) {
			log.error("error while processing content changes", ex);
		}
	}

	@Override
	public void close() {
		synchronized (lock) {
			if (closed) {
				return;
			}
			closed = true;
			if (scheduledFlush != null) {
				scheduledFlush.cancel(false);
			}
		}
		flushNow();
		executor.shutdown();
		try {
			if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
				executor.shutdownNow();
			}
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			executor.shutdownNow();
		}
	}
}
