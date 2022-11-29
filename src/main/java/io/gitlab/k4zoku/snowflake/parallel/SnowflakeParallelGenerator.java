package io.gitlab.k4zoku.snowflake.parallel;

import io.gitlab.k4zoku.snowflake.Snowflake;
import io.gitlab.k4zoku.snowflake.SnowflakeGenerator;
import io.gitlab.k4zoku.snowflake.SnowflakeGeneratorFactory;
import io.gitlab.k4zoku.snowflake.common.Generator;
import io.gitlab.k4zoku.snowflake.parallel.task.SnowflakeConsumeTask;
import io.gitlab.k4zoku.snowflake.parallel.task.SnowflakeGenerateTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static io.gitlab.k4zoku.snowflake.SnowflakeGenerator.MAX_WORKER_ID;

/**
 * Provides a parallel generator of {@link Snowflake}.
 *
 * @author k4zoku
 * @since 1.0
 */
public class SnowflakeParallelGenerator implements Generator<Snowflake>, AutoCloseable {
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
    public SnowflakeParallelGenerator(
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

    public SnowflakeParallelGenerator(
        @NotNull SnowflakeGeneratorFactory factory,
        @Range(from = 1, to = MAX_WORKER_ID + 1) int workers
    ) {
        this(factory, workers, 0);
    }

    public SnowflakeParallelGenerator(
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
            return executorService.submit(new SnowflakeGenerateTask()).get();
        } catch (ExecutionException e) {
            throw new IllegalStateException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void close() {
        executorService.shutdown();
    }

    public boolean isClosed() {
        return executorService.isTerminated();
    }

    @Override
    public Spliterator<Snowflake> spliterator() {
        return Spliterators.spliteratorUnknownSize(iterator(),
            Spliterator.ORDERED | Spliterator.IMMUTABLE | Spliterator.NONNULL |
                Spliterator.DISTINCT | Spliterator.SORTED | Spliterator.CONCURRENT
        );
    }

    @Override
    public void forEach(Consumer<? super Snowflake> action) {
        while (!isClosed()) {
            executorService.submit(new SnowflakeConsumeTask(action));
        }
    }

    public Future<Void> forEachAsync(Consumer<? super Snowflake> action) {
        FutureTask<Void> futureTask = new FutureTask<>(() -> forEach(action), null);
        Thread forEachThread = new Thread(futureTask);
        forEachThread.setDaemon(true);
        forEachThread.start();
        return futureTask;
    }

    @Override
    public Stream<Snowflake> stream() {
        return StreamSupport.stream(spliterator(), true);
    }

    @Override
    public Stream<Snowflake> stream(long maxSize) {
        return StreamSupport.stream(spliterator(), true).limit(maxSize);
    }

}
