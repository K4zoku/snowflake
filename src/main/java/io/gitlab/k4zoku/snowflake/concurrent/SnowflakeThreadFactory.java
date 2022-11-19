package io.gitlab.k4zoku.snowflake.concurrent;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;

import static io.gitlab.k4zoku.snowflake.SnowflakeGenerator.MAX_WORKER_ID;

public class SnowflakeThreadFactory implements ThreadFactory {
    private final long maxWorkers;
    private long counter;
    private final SnowflakeGeneratorFactory generatorFactory;

    private final Map<Long, SnowflakeWorker> workers = new ConcurrentHashMap<>();

    public SnowflakeThreadFactory(int offset, int maxWorkers, @NotNull SnowflakeGeneratorFactory generatorFactory) {
        this.maxWorkers = maxWorkers & MAX_WORKER_ID;
        this.counter = offset & MAX_WORKER_ID;
        this.generatorFactory = generatorFactory;
    }

    @Override
    public Thread newThread(@NotNull Runnable r) {
        long id = counter;
        SnowflakeWorker worker = workers.computeIfAbsent(id, i -> new SnowflakeWorker(generatorFactory.create(i)));
        Thread thread = new SnowflakeWorkerThread(worker, r);
        counter = (counter + 1) & maxWorkers;
        return thread;
    }
}
