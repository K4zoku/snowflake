package io.gitlab.k4zoku.snowflake.concurrent;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;

public class SnowflakeThreadFactory implements ThreadFactory {

    private final int offset;
    private final int maxWorkers;
    private int counter;

    public SnowflakeThreadFactory(int offset, int maxWorkers) {
        this.offset = offset;
        this.maxWorkers = maxWorkers;
        this.counter = 0;
    }

    @Override
    public Thread newThread(@NotNull Runnable r) {
        Thread thread = new Thread(r);
        thread.setName("snowflake-" + (counter + offset));
        counter = (counter + 1) % (maxWorkers - offset);
        thread.setDaemon(true);
        return thread;
    }
}
