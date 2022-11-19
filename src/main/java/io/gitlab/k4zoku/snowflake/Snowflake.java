package io.gitlab.k4zoku.snowflake;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneOffset;

import static io.gitlab.k4zoku.snowflake.SnowflakeGenerator.*;

public class Snowflake implements Comparable<Snowflake>, Serializable {

    private static final long serialVersionUID = 0L;

    private final long value;

    public Snowflake(long value) {
        if (value < 4194304) {
            throw new IllegalArgumentException(String.format("'%d' doesn't look like a snowflake. Snowflakes are much larger numbers.", value));
        }
        this.value = value;
    }

    public static Snowflake of(long id) {
        return new Snowflake(id);
    }

    public static Snowflake of(String id) {
        return new Snowflake(Long.parseLong(id));
    }

    public long getAsLong() {
        return value;
    }

    public long getTimestamp(long epoch) {
        return ((value & TIMESTAMP_MASK) >> TIMESTAMP_SHIFT) + epoch;
    }

    public long getTimestamp() {
        return getTimestamp(getDefaultEpoch());
    }

    public long getDataCenterId() {
        return (value & DATA_CENTER_ID_MASK) >> DATA_CENTER_ID_SHIFT;
    }

    public long getWorkerId() {
        return (value & WORKER_ID_MASK) >> WORKER_ID_SHIFT;
    }

    public long getSequence() {
        return value & SEQUENCE_MASK;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        Snowflake other = (Snowflake) obj;
        return this.value == other.value;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(value);
    }

    @Override
    public String toString() {
        return Long.toString(value);
    }

    public String toFormattedString() {
        return String.format("[%-24s] [%02d/%02d] #%04d",
            Instant.ofEpochMilli(getTimestamp()).atOffset(ZoneOffset.UTC),
            getDataCenterId(),
            getWorkerId(),
            getSequence()
        );
    }

    @Override
    public int compareTo(Snowflake o) {
        return Long.compare(value, o.value);
    }
}
