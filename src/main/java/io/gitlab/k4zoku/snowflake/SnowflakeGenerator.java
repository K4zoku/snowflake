package io.gitlab.k4zoku.snowflake;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

public class SnowflakeGenerator implements Serializable, Comparable<SnowflakeGenerator> {

    // 2022-01-01T00:00:00+00:00
    public static final long DEFAULT_EPOCH = 1640995200000L;
    public static final long TIMESTAMP_BITS = 41L;
    public static final long DATA_CENTER_ID_BITS = 5L;
    public static final long WORKER_ID_BITS = 5L;
    public static final long SEQUENCE_BITS = 12L;
    public static final long MAX_TIMESTAMP = ~(-1L << TIMESTAMP_BITS);
    public static final long MAX_DATA_CENTER_ID = ~(-1L << DATA_CENTER_ID_BITS);
    public static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    public static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);
    public static final long SEQUENCE_MASK = MAX_SEQUENCE;
    public static final long TIMESTAMP_SHIFT = DATA_CENTER_ID_BITS + WORKER_ID_BITS + SEQUENCE_BITS;
    public static final long TIMESTAMP_MASK = MAX_TIMESTAMP << TIMESTAMP_SHIFT;
    public static final long DATA_CENTER_ID_SHIFT = WORKER_ID_BITS + SEQUENCE_BITS;
    public static final long DATA_CENTER_ID_MASK = MAX_DATA_CENTER_ID << DATA_CENTER_ID_SHIFT;
    public static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    public static final long WORKER_ID_MASK = MAX_WORKER_ID << WORKER_ID_SHIFT;
    private static final long serialVersionUID = 0L;
    private final long epoch;
    private final long dataCenterId;
    private final long workerId;
    private volatile long sequence = 0L;

    private transient volatile long lastTimestamp = -1L;
    private transient TimestampProvider timestampProvider = SystemTimestampProvider.getInstance();

    public SnowflakeGenerator(long epoch, int dataCenterId, int workerId) {
        this.epoch = epoch;
        this.dataCenterId = dataCenterId;
        this.workerId = workerId;
    }

    public SnowflakeGenerator(int dataCenterId, int workerId) {
        this(DEFAULT_EPOCH, dataCenterId, workerId);
    }

    public SnowflakeGenerator() {
        this(0, 0);
    }

    public long getEpoch() {
        return epoch;
    }

    public long getDataCenterId() {
        return dataCenterId;
    }

    public long getWorkerId() {
        return workerId;
    }

    public TimestampProvider getTimestampProvider() {
        return timestampProvider;
    }

    public void setTimestampProvider(TimestampProvider timestampProvider) {
        this.timestampProvider = timestampProvider;
    }

    public synchronized long nextId() {
        long timestamp = timestampProvider.getTimestamp();
        if (timestamp < lastTimestamp) {
            throw new IllegalStateException("Clock moved backwards.");
        }
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                timestamp = timestampProvider.waitUntilNextTimestamp(timestamp);
            }
        } else {
            sequence = 0;
        }
        lastTimestamp = timestamp;
        return (timestamp - epoch) << TIMESTAMP_SHIFT
                | dataCenterId << DATA_CENTER_ID_SHIFT
                | workerId << WORKER_ID_SHIFT
                | sequence;
    }

    public synchronized Snowflake next() {
        return new Snowflake(nextId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(epoch, dataCenterId, workerId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        SnowflakeGenerator other = (SnowflakeGenerator) obj;
        return this.epoch == other.epoch
                && this.dataCenterId == other.dataCenterId
                && this.workerId == other.workerId;
    }

    @Override
    public int compareTo(@NotNull SnowflakeGenerator o) {
        int result = Long.compare(epoch, o.epoch);
        if (result != 0) {
            return result;
        }
        result = Long.compare(dataCenterId, o.dataCenterId);
        if (result != 0) {
            return result;
        }
        return Long.compare(workerId, o.workerId);
    }
}
