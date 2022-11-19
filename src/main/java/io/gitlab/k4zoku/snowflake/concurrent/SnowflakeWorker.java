package io.gitlab.k4zoku.snowflake.concurrent;

import io.gitlab.k4zoku.snowflake.Snowflake;
import io.gitlab.k4zoku.snowflake.SnowflakeGenerator;

import java.util.concurrent.Callable;

public class SnowflakeWorker implements Callable<Snowflake> {

    public static boolean test = false;

    private final SnowflakeGenerator generator;
    private final long id;

    public SnowflakeWorker(SnowflakeGenerator generator) {
        this.generator = generator;
        this.id = generator.getWorkerId();
    }

    @Override
    public Snowflake call() {
        return work();
    }

    public Snowflake work() {
        return generator.generate();
    }

    public long getId() {
        return id;
    }
}
