package io.gitlab.k4zoku.snowflake.parallel.task;

import io.gitlab.k4zoku.snowflake.Snowflake;
import io.gitlab.k4zoku.snowflake.parallel.SnowflakeWorker;

import java.util.concurrent.Callable;

public class SnowflakeGenerateTask implements Callable<Snowflake> {

    @Override
    public Snowflake call() {
        return SnowflakeWorker.currentWorker().work();
    }

}
