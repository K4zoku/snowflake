package io.gitlab.k4zoku.snowflake.parallel.task;

import io.gitlab.k4zoku.snowflake.Snowflake;
import io.gitlab.k4zoku.snowflake.parallel.SnowflakeWorker;

import java.util.function.Consumer;

public class SnowflakeConsumeTask implements Runnable {

    private final Consumer<? super Snowflake> action;

    public SnowflakeConsumeTask(Consumer<? super Snowflake> action) {
        this.action = action;
    }

    @Override
    public void run() {
        action.accept(SnowflakeWorker.currentWorker().work());
    }

}
