package io.gitlab.k4zoku.snowflake.concurrent;

import io.gitlab.k4zoku.snowflake.Snowflake;

import java.util.concurrent.Callable;

public class SnowflakeGeneratorTask implements Callable<Snowflake> {
    @Override
    public Snowflake call() {
        return SnowflakeWorker.currentWorker().work();
    }
}
