package io.gitlab.k4zoku.snowflake;

import io.gitlab.k4zoku.snowflake.time.TimestampProvider;

public class SnowflakeGeneratorFactory {
    private final long epoch;
    private final long dataCenterId;
    private final TimestampProvider timestampProvider;

    public SnowflakeGeneratorFactory(
        long epoch,
        long dataCenterId,
        TimestampProvider timestampProvider
    ) {
        this.epoch = epoch;
        this.dataCenterId = dataCenterId;
        this.timestampProvider = timestampProvider;
    }

    public SnowflakeGenerator create(long workerId) {
        return new SnowflakeGenerator(epoch, dataCenterId, workerId, timestampProvider);
    }
}
