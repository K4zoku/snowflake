package io.gitlab.k4zoku.snowflake.concurrent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;

import static io.gitlab.k4zoku.snowflake.SnowflakeGenerator.MAX_WORKER_ID;

public class SnowflakeThreadFactory implements ThreadFactory {
    private final long maxWorkers;
    private long counter;
    private final SnowflakeGeneratorFactory generatorFactory;

    private final Map<Long, SnowflakeWorker> workers = new ConcurrentHashMap<>();

    public SnowflakeThreadFactory(
        @Range(from = 0, to = MAX_WORKER_ID) int offset,
        @Range(from = 1, to = MAX_WORKER_ID + 1) int maxWorkers,
        @NotNull SnowflakeGeneratorFactory generatorFactory
    ) {
        this.maxWorkers = maxWorkers;
        this.counter = offset;
        this.generatorFactory = generatorFactory;
    }

    @Override
    public Thread newThread(@NotNull Runnable r) {
        long id = counter;
        SnowflakeWorker worker = workers.computeIfAbsent(id, i -> new SnowflakeWorker(generatorFactory.create(i)));
        Thread thread = new SnowflakeWorkerThread(worker, r);
        counter = (counter + 1) % maxWorkers;
        return thread;
    }
}
