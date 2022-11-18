package io.gitlab.k4zoku.snowflake.hibernate.test;

import io.gitlab.k4zoku.snowflake.time.TimestampProvider;

public class CounterBlockingTimestampProvider implements TimestampProvider {

    private final TimestampProvider delegate;

    private int counter = 0;
    private final int blockUntil;

    private long lastTimestamp = -1;

    public CounterBlockingTimestampProvider(TimestampProvider delegate, int blockUntil) {
        this.delegate = delegate;
        this.blockUntil = blockUntil;
    }

    public CounterBlockingTimestampProvider() {
        this(TimestampProvider.system(), 10);
    }

    @Override
    public long getTimestamp() {
        counter = (counter + 1) % blockUntil;
        if (counter != 0) {
            lastTimestamp = lastTimestamp == -1 ? delegate.getTimestamp() : lastTimestamp;
            return lastTimestamp;
        }
        return lastTimestamp = delegate.getTimestamp();
    }
}
