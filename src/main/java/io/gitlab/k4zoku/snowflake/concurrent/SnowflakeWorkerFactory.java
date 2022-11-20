package io.gitlab.k4zoku.snowflake.concurrent;

import io.gitlab.k4zoku.snowflake.SnowflakeGenerator;
import io.gitlab.k4zoku.snowflake.SnowflakeGeneratorFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

import static io.gitlab.k4zoku.snowflake.SnowflakeGenerator.MAX_WORKER_ID;

public class SnowflakeWorkerFactory implements ThreadFactory {
    private final long maxWorkers;
    private final AtomicLong counter;
    private final int offset;
    private final SnowflakeGeneratorFactory generatorFactory;

    public SnowflakeWorkerFactory(
        @Range(from = 0, to = MAX_WORKER_ID) int offset,
        @Range(from = 1, to = MAX_WORKER_ID + 1) int maxWorkers,
        @NotNull SnowflakeGeneratorFactory generatorFactory
    ) {
        this.maxWorkers = maxWorkers;
        this.counter = new AtomicLong(0);
        this.offset = offset;
        this.generatorFactory = generatorFactory;
    }

    @Override
    public Thread newThread(@NotNull Runnable r) {
        long id = nextId();
        SnowflakeGenerator generator = generatorFactory.create(id);
        return new SnowflakeWorker(generator, r);
    }

    protected long nextId() {
        // counter must be less than maxWorkers and less than MAX_WORKER_ID - offset,
        // so nextId will be greater than or equal to offset and less than or equal to MAX_WORKER_ID.
        return offset + counter.getAndUpdate(c -> (c + 1) % maxWorkers) % (MAX_WORKER_ID - offset);
    }
}
