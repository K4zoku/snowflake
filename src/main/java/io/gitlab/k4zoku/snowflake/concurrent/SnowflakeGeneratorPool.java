package io.gitlab.k4zoku.snowflake.concurrent;

import io.gitlab.k4zoku.snowflake.Snowflake;
import io.gitlab.k4zoku.snowflake.SnowflakeGenerator;
import io.gitlab.k4zoku.snowflake.SnowflakeGeneratorFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.concurrent.*;

import static io.gitlab.k4zoku.snowflake.SnowflakeGenerator.MAX_WORKER_ID;

/**
 * Pool of {@link SnowflakeGenerator}. Use this class if you want to generate snowflakes in multiple threads.
 *
 * @author k4zoku
 * @since 1.0
 */
public class SnowflakeGeneratorPool {
    private final ExecutorService executorService;

    /**
     * Create a pool of {@link SnowflakeGenerator}.
     *
     * @param factory        factory to create {@link SnowflakeGenerator} by providing worker ID.
     * @param workers        number of workers (threads) in the pool.
     *                       If this value is 0, the number of workers is equal to the number of processors.
     *                       If this value is greater than {@link SnowflakeGenerator#MAX_WORKER_ID},
     *                       the number of workers will be truncated.
     * @param workerIdOffset offset of worker ID. In other words, the first worker ID is {@code workerIdOffset}.
     */
    public SnowflakeGeneratorPool(
        @NotNull SnowflakeGeneratorFactory factory,
        @Range(from = 1, to = MAX_WORKER_ID + 1) int workers,
        @Range(from = 0, to = MAX_WORKER_ID) int workerIdOffset
    ) {
        this.executorService = new ThreadPoolExecutor(
            workers,
            workers,
            0L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(),
            new SnowflakeWorkerFactory(workerIdOffset, workers, factory)
        );
    }

    public SnowflakeGeneratorPool(
        @NotNull SnowflakeGeneratorFactory factory,
        @Range(from = 1, to = MAX_WORKER_ID + 1) int workers
    ) {
        this(factory, workers, 0);
    }

    public SnowflakeGeneratorPool(
        @NotNull SnowflakeGeneratorFactory factory
    ) {
        this(factory, MAX_WORKER_ID + 1, 0);
    }

    /**
     * Generate a snowflake.
     *
     * @return snowflake
     */
    public Snowflake generate() {
        try {
            return executorService.submit(new SnowflakeWorkerTask()).get();
        } catch (ExecutionException e) {
            throw new IllegalStateException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

}
