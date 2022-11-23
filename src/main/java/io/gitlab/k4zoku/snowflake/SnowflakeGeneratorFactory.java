package io.gitlab.k4zoku.snowflake;

import io.gitlab.k4zoku.snowflake.time.TimestampProvider;

public class SnowflakeGeneratorFactory {
    private final long epoch;
    private final int dataCenterId;
    private final TimestampProvider timestampProvider;

    SnowflakeGeneratorFactory(
        TimestampProvider timestampProvider,
        long epoch,
        int dataCenterId
    ) {
        this.epoch = epoch;
        this.dataCenterId = dataCenterId;
        this.timestampProvider = timestampProvider;
    }

    public static SnowflakeGeneratorFactoryBuilder builder() {
        return new SnowflakeGeneratorFactoryBuilder();
    }

    public SnowflakeGenerator create(int workerId) {
        return new SnowflakeGenerator(epoch, dataCenterId, workerId, timestampProvider);
    }
}
