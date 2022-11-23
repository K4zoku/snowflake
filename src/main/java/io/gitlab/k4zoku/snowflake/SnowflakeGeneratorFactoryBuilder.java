package io.gitlab.k4zoku.snowflake;

import io.gitlab.k4zoku.snowflake.time.TimestampProvider;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import static io.gitlab.k4zoku.snowflake.SnowflakeGenerator.MAX_DATA_CENTER_ID;

public class SnowflakeGeneratorFactoryBuilder {
    private long epoch = SnowflakeGenerator.DISCORD_EPOCH;
    private int dataCenterId = 0;
    private TimestampProvider timestampProvider = TimestampProvider.system();

    SnowflakeGeneratorFactoryBuilder() {
    }

    @Contract("_ -> this")
    public SnowflakeGeneratorFactoryBuilder epoch(@Range(from = 0, to = Long.MAX_VALUE) long epoch) {
        this.epoch = epoch;
        return this;
    }

    @Contract("_ -> this")
    public SnowflakeGeneratorFactoryBuilder dataCenterId(@Range(from = 0, to = MAX_DATA_CENTER_ID) int dataCenterId) {
        this.dataCenterId = dataCenterId;
        return this;
    }

    @Contract("_ -> this")
    public SnowflakeGeneratorFactoryBuilder timestampProvider(@NotNull TimestampProvider timestampProvider) {
        this.timestampProvider = timestampProvider;
        return this;
    }

    @Contract("-> new")
    public SnowflakeGeneratorFactory build() {
        return new SnowflakeGeneratorFactory(timestampProvider, epoch, dataCenterId);
    }
}
