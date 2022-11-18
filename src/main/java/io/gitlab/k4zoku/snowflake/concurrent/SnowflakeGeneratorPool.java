package io.gitlab.k4zoku.snowflake.concurrent;

import io.gitlab.k4zoku.snowflake.Snowflake;
import io.gitlab.k4zoku.snowflake.SnowflakeGenerator;
import io.gitlab.k4zoku.snowflake.time.TimestampProvider;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
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

    public static final int DEFAULT_MAX_WORKERS = 0;
    private final Map<String, SnowflakeWorker> workers;
    private final Collection<Callable<Snowflake>> tasks;
    private final ExecutorService executorService;

    private final int maxWorkers;
    private final int initialWorkers;

    private final SnowflakeGeneratorFactory snowflakeGeneratorFactory;

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
        @Range(from = 0, to = MAX_WORKER_ID) int maxWorkers,
        @Range(from = 0, to = MAX_WORKER_ID) int initialWorkers,
        @Range(from = 0, to = MAX_WORKER_ID) int workerIdOffset,
        @Nullable TimestampProvider timestampProvider
    ) {
        this.workers = new ConcurrentHashMap<>(initialWorkers);
        this.maxWorkers = maxWorkers = Math.toIntExact((maxWorkers == DEFAULT_MAX_WORKERS ? Runtime.getRuntime().availableProcessors() : maxWorkers) & MAX_WORKER_ID);
        if (initialWorkers < 0 || initialWorkers > maxWorkers) {
            throw new IllegalArgumentException("initialWorkers must be between 0 and maxWorkers");
        }
        this.tasks = new ArrayList<>(initialWorkers);
        this.executorService = new ThreadPoolExecutor(
            initialWorkers,
            maxWorkers,
            60L,
            TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            new SnowflakeThreadFactory(workerIdOffset, this.maxWorkers)
        );
        for (int i = 0; i < initialWorkers; i++) {
            tasks.add(() -> workers.computeIfAbsent(Thread.currentThread().getName(), this::createWorker).call());
        }
        this.initialWorkers = Math.toIntExact(initialWorkers & MAX_WORKER_ID);
        this.snowflakeGeneratorFactory = new SnowflakeGeneratorFactory(epoch, dataCenterId, timestampProvider);
    }

    public SnowflakeGeneratorPool(
        @Range(from = 0, to = Long.MAX_VALUE) long epoch,
        @Range(from = 0, to = MAX_DATA_CENTER_ID) long dataCenterId,
        @Range(from = 0, to = MAX_WORKER_ID) int maxWorkers,
        @Nullable TimestampProvider timestampProvider
    ) {
        this(epoch, dataCenterId, maxWorkers, 3, 0, timestampProvider);
    }
    /**
     * Generate a snowflake.
     *
     * @return snowflake
     */
    public Snowflake generate() {
        try {
            return executorService.invokeAny(tasks);
        } catch (ExecutionException e) {
            throw new IllegalStateException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    private SnowflakeWorker createWorker(long id) {
        SnowflakeGenerator generator = snowflakeGeneratorFactory.create(id);
        return new SnowflakeWorker(generator);
    }

    private SnowflakeWorker createWorker(String id) {
        return createWorker(Long.parseLong(id));
    }

}
