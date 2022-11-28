package io.gitlab.k4zoku.snowflake.parallel;

import io.gitlab.k4zoku.snowflake.Snowflake;

import java.util.concurrent.Callable;

class SnowflakeWorkerTask implements Callable<Snowflake> {
    @Override
    public Snowflake call() {
        return SnowflakeWorker.currentWorker().work();
    }
}
