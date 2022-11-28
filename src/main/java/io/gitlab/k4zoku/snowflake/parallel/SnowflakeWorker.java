package io.gitlab.k4zoku.snowflake.parallel;

import io.gitlab.k4zoku.snowflake.Snowflake;
import io.gitlab.k4zoku.snowflake.SnowflakeGenerator;

public class SnowflakeWorker extends Thread {

    private final SnowflakeGenerator generator;

    public SnowflakeWorker(SnowflakeGenerator generator, Runnable target) {
        super(target, String.format("SnowflakeWorker[%02d/%02d]", generator.getDataCenterId(), generator.getWorkerId()));
        this.setDaemon(true);
        this.generator = generator;
    }

    public Snowflake work() {
        return generator.generate();
    }

    public static SnowflakeWorker currentWorker() {
        Thread thread = Thread.currentThread();
        if (thread instanceof SnowflakeWorker) {
            return (SnowflakeWorker) thread;
        }
        throw new IllegalStateException("Current thread is not a SnowflakeWorker");
    }
}
