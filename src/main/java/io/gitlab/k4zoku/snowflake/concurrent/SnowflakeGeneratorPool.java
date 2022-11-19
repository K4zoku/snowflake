package io.gitlab.k4zoku.snowflake.concurrent;

import io.gitlab.k4zoku.snowflake.Snowflake;
import io.gitlab.k4zoku.snowflake.SnowflakeGenerator;
import io.gitlab.k4zoku.snowflake.SnowflakeGeneratorFactory;
import io.gitlab.k4zoku.snowflake.time.TimestampProvider;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.concurrent.*;

import static io.gitlab.k4zoku.snowflake.SnowflakeGenerator.MAX_DATA_CENTER_ID;
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
     * @param epoch             epoch
     * @param dataCenterId      data center ID
     * @param maxWorkers        number of workers (threads) in the pool.
     *                          If this value is 0, the number of workers is equal to the number of processors.
     *                          If this value is greater than {@link SnowflakeGenerator#MAX_WORKER_ID},
     *                          the number of workers will be truncated.
     * @param timestampProvider timestamp provider
     */
    public SnowflakeGeneratorPool(
        @Range(from = 0, to = Long.MAX_VALUE) long epoch,
        @Range(from = 0, to = MAX_DATA_CENTER_ID) long dataCenterId,
        @Range(from = 1, to = MAX_WORKER_ID + 1) int maxWorkers,
        @Range(from = 0, to = MAX_WORKER_ID) int initialWorkers,
        @Range(from = 0, to = MAX_WORKER_ID) int workerIdOffset,
        @Nullable TimestampProvider timestampProvider
    ) {
        if (initialWorkers > maxWorkers) {
            throw new IllegalArgumentException("initialWorkers must be between 0 and maxWorkers");
        }
        SnowflakeGeneratorFactory snowflakeGeneratorFactory = new SnowflakeGeneratorFactory(epoch, dataCenterId, timestampProvider);
        this.executorService = new ThreadPoolExecutor(
            initialWorkers,
            maxWorkers,
            3L,
            TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(1),
            new SnowflakeThreadFactory(workerIdOffset, maxWorkers, snowflakeGeneratorFactory)
        );
    }

    public SnowflakeGeneratorPool(
        @Range(from = 0, to = Long.MAX_VALUE) long epoch,
        @Range(from = 0, to = MAX_DATA_CENTER_ID) long dataCenterId,
        @Range(from = 1, to = MAX_WORKER_ID + 1) int maxWorkers,
        @Nullable TimestampProvider timestampProvider
    ) {
        this(epoch, dataCenterId, maxWorkers, 0, 0, timestampProvider);
    }

    /**
     * Generate a snowflake.
     *
     * @return snowflake
     */
    public Snowflake generate() {
        try {
            return generateAsync().get();
        } catch (ExecutionException e) {
            throw new IllegalStateException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    public Future<Snowflake> generateAsync() {
        return executorService.submit(() -> SnowflakeWorkerThread.getCurrentThread().getWorker().work());
    }

}
