package io.gitlab.k4zoku.snowflake.concurrent;

import io.gitlab.k4zoku.snowflake.Snowflake;
import io.gitlab.k4zoku.snowflake.SnowflakeGenerator;
import io.gitlab.k4zoku.snowflake.time.TimestampProvider;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.gitlab.k4zoku.snowflake.SnowflakeGenerator.MAX_DATA_CENTER_ID;
import static io.gitlab.k4zoku.snowflake.SnowflakeGenerator.MAX_WORKER_ID;

/**
 * Pool of {@link SnowflakeGenerator}. Use this class if you want to generate snowflakes in multiple threads.
 *
 * @author k4zoku
 * @since 1.0
 */
public class SnowflakeGeneratorPool {
    private final Collection<Callable<Snowflake>> snowflakeGenerators;
    private final ExecutorService executorService;

    /**
     * Create a pool of {@link SnowflakeGenerator}.
     *
     * @param epoch             epoch
     * @param dataCenterId      data center ID
     * @param workers           number of workers (threads) in the pool.
     *                          If this value is 0, the number of workers is equal to the number of processors.
     *                          If this value is greater than {@link SnowflakeGenerator#MAX_WORKER_ID},
     *                          the number of workers will be truncated.
     * @param timestampProvider timestamp provider
     */
    public SnowflakeGeneratorPool(
        @Range(from = 0, to = Long.MAX_VALUE) long epoch,
        @Range(from = 0, to = MAX_DATA_CENTER_ID) long dataCenterId,
        @Range(from = 0, to = MAX_WORKER_ID) int workers,
        @Nullable TimestampProvider timestampProvider) {
        workers = Math.toIntExact((workers < 1 ? Runtime.getRuntime().availableProcessors() : workers) & MAX_WORKER_ID);
        this.snowflakeGenerators = new ArrayList<>(workers);
        for (int i = 0; i <= workers; i++) {
            // NOTE: this may cause memory leak, if you have a better idea, please let me know.
            SnowflakeGenerator generator = new SnowflakeGenerator(epoch, dataCenterId, i, timestampProvider);
            snowflakeGenerators.add(generator::generate);
        }
        this.executorService = Executors.newFixedThreadPool(workers);
    }

    /**
     * Generate a snowflake.
     *
     * @return snowflake
     */
    public Snowflake generate() {
        try {
            return executorService.invokeAny(snowflakeGenerators);
        } catch (ExecutionException e) {
            throw new IllegalStateException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    public ExecutorService getExecutorService() {
        return this.executorService;
    }

}
