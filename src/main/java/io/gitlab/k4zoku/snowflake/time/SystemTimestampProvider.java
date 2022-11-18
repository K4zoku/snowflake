package io.gitlab.k4zoku.snowflake.time;

public class SystemTimestampProvider implements TimestampProvider {

    private static final SystemTimestampProvider INSTANCE = new SystemTimestampProvider();

    private SystemTimestampProvider() {
    }

    public static SystemTimestampProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public long getTimestamp() {
        return System.currentTimeMillis();
    }

    @Override
    public synchronized long waitUntilNextTimestamp(long lastTimestamp) {
        long timestamp = getTimestamp();
        while (timestamp <= lastTimestamp) {
            try {
                wait(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            timestamp = getTimestamp();
        }
        return timestamp;
    }
}