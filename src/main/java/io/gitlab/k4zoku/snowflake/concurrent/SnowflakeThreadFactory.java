package io.gitlab.k4zoku.snowflake.concurrent;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;

public class SnowflakeThreadFactory implements ThreadFactory {

    private int counter;
    private int offset;
    private int maxWorkers;

    public SnowflakeThreadFactory() {
        counter = 0;
    }

    public SnowflakeThreadFactory(int offset, int maxWorkers) {
        this.offset = offset;
        this.maxWorkers = maxWorkers;
        this.counter = 0;
    }

    @Override
    public Thread newThread(@NotNull Runnable r) {
        Thread thread = new Thread(r);
        thread.setName(String.valueOf(counter + offset));
        counter = (counter + 1) % (maxWorkers - offset);
        thread.setDaemon(true);
        return thread;
    }
}
