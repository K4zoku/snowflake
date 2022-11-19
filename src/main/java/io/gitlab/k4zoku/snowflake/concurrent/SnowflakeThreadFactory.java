package io.gitlab.k4zoku.snowflake.concurrent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

import static io.gitlab.k4zoku.snowflake.SnowflakeGenerator.MAX_WORKER_ID;

public class SnowflakeThreadFactory implements ThreadFactory {
    private final long maxWorkers;
    private final AtomicLong counter;
    private final SnowflakeGeneratorFactory generatorFactory;

    public SnowflakeThreadFactory(
        @Range(from = 0, to = MAX_WORKER_ID) int offset,
        @Range(from = 1, to = MAX_WORKER_ID + 1) int maxWorkers,
        @NotNull SnowflakeGeneratorFactory generatorFactory
    ) {
        this.maxWorkers = maxWorkers;
        this.counter = new AtomicLong(offset);
        this.generatorFactory = generatorFactory;
    }

    @Override
    public Thread newThread(@NotNull Runnable r) {
        long id = counter.getAndUpdate(c -> (c + 1) % maxWorkers);
        System.out.println("Creating thread with id " + id);
        SnowflakeWorker worker = new SnowflakeWorker(generatorFactory.create(id));
        return new SnowflakeWorkerThread(worker, r);
    }
}
