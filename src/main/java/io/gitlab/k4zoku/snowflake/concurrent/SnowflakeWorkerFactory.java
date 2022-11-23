package io.gitlab.k4zoku.snowflake.concurrent;

import io.gitlab.k4zoku.snowflake.SnowflakeGenerator;
import io.gitlab.k4zoku.snowflake.SnowflakeGeneratorFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static io.gitlab.k4zoku.snowflake.SnowflakeGenerator.MAX_WORKER_ID;

public class SnowflakeWorkerFactory implements ThreadFactory {
    private final int maxWorkers;
    private final AtomicInteger counter;
    private final int offset;
    private final int delta;
    private final SnowflakeGeneratorFactory generatorFactory;
    private final Map<Integer, WeakReference<SnowflakeGenerator>> generators;

    public SnowflakeWorkerFactory(
        @Range(from = 0, to = MAX_WORKER_ID) int offset,
        @Range(from = 1, to = MAX_WORKER_ID + 1) int maxWorkers,
        @NotNull SnowflakeGeneratorFactory generatorFactory
    ) {
        this.maxWorkers = maxWorkers;
        this.counter = new AtomicInteger(0);
        this.offset = (offset % (MAX_WORKER_ID + 1));
        this.delta = (MAX_WORKER_ID - this.offset);
        this.generatorFactory = generatorFactory;
        this.generators = new ConcurrentHashMap<>();
    }

    @Override
    public Thread newThread(@NotNull Runnable r) {
        int id = nextId();
        SnowflakeGenerator generator = Optional.ofNullable(generators.get(id))
            .map(WeakReference::get)
            .orElseGet(() -> {
                SnowflakeGenerator g = generatorFactory.create(id);
                generators.put(id, new WeakReference<>(g));
                return g;
            });
        return new SnowflakeWorker(generator, r);
    }

    /**
     * Returns the next worker ID in the interval of inclusive range from {@link #offset} to
     * ({@link #offset} + {@link #maxWorkers}) or {@link SnowflakeGenerator#MAX_WORKER_ID}
     * whichever is smaller.
     *
     * @return the next worker ID
     */
    protected int nextId() {
        return offset + counter.getAndUpdate(c -> (c + 1) % maxWorkers) % delta;
    }
}
