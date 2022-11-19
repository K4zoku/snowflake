package io.gitlab.k4zoku.snowflake.concurrent;

import io.gitlab.k4zoku.snowflake.Snowflake;
import io.gitlab.k4zoku.snowflake.SnowflakeGenerator;

public class SnowflakeWorker {
    private final SnowflakeGenerator generator;
    private final long id;

    public SnowflakeWorker(SnowflakeGenerator generator) {
        this.generator = generator;
        this.id = generator.getWorkerId();
    }

    public long getId() {
        return id;
    }

    public Snowflake work() {
        return generator.generate();
    }
}
