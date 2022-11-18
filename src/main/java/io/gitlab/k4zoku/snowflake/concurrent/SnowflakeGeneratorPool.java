package io.gitlab.k4zoku.snowflake.concurrent;

import io.gitlab.k4zoku.snowflake.Snowflake;
import io.gitlab.k4zoku.snowflake.SnowflakeGenerator;
import io.gitlab.k4zoku.snowflake.time.TimestampProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.gitlab.k4zoku.snowflake.SnowflakeGenerator.DEFAULT_EPOCH;
import static io.gitlab.k4zoku.snowflake.SnowflakeGenerator.DEFAULT_TIMESTAMP_PROVIDER;

/**
 * Pool of {@link SnowflakeGenerator}. Use this class if you want to generate snowflakes in multiple threads.
 * @author k4zoku
 * @since 1.0.0
 */
public class SnowflakeGeneratorPool {
    private final Collection<Callable<Snowflake>> snowflakeGenerators;
    private final Collection<Callable<Long>> snowflakeIdGenerators;
    private final ExecutorService executorService;

    /**
     * Create a pool of {@link SnowflakeGenerator}.
     * @param epoch epoch
     * @param dataCenterId data center ID
     * @param workers number of workers
     * @param timestampProvider timestamp provider
     */
    public SnowflakeGeneratorPool(long epoch, long dataCenterId, int workers, TimestampProvider timestampProvider) {
        this.snowflakeGenerators = new ArrayList<>(workers);
        this.snowflakeIdGenerators = new ArrayList<>(workers);
        for (int i = 0; i < workers; i++) {
            SnowflakeGenerator generator = new SnowflakeGenerator(epoch, dataCenterId, i, timestampProvider);
            snowflakeGenerators.add(generator::generate);
            snowflakeIdGenerators.add(generator::generateId);
        }
        this.executorService = Executors.newFixedThreadPool(workers);
    }

    /**
     * Create a pool of {@link SnowflakeGenerator}.
     * @param dataCenterId data center ID
     * @param workers number of workers
     */
    public SnowflakeGeneratorPool(long dataCenterId, int workers) {
        this(DEFAULT_EPOCH, dataCenterId, workers, DEFAULT_TIMESTAMP_PROVIDER);
    }

    /**
     * Create a pool of {@link SnowflakeGenerator}. This constructor will get number of processors and use it as number of workers.
     * @param dataCenterId data center ID
     */
    public SnowflakeGeneratorPool(long dataCenterId) {
        this(dataCenterId, Runtime.getRuntime().availableProcessors());
    }

    private <T> T execute(Collection<Callable<T>> tasks) {
        try {
            return executorService.invokeAny(tasks);
        } catch (ExecutionException e) {
            throw new IllegalStateException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    public Snowflake generate() {
        return execute(snowflakeGenerators);
    }

    public long generateId() {
        return execute(snowflakeIdGenerators);
    }

}
