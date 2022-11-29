package io.gitlab.k4zoku.snowflake;

import org.jetbrains.annotations.Range;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Formattable;
import java.util.FormattableFlags;
import java.util.Formatter;

import static io.gitlab.k4zoku.snowflake.SnowflakeGenerator.*;

public class Snowflake extends Number implements Comparable<Snowflake>, Formattable {

    private static final long serialVersionUID = 0L;

    private final long value;

    public Snowflake() {
        this.value = 0L;
    }

    public Snowflake(long value) {
        this.value = value;
    }

    public static Snowflake of(long value) {
        return new Snowflake(value);
    }

    public static Snowflake of(String value) {
        return new Snowflake(Long.parseLong(value));
    }

    @Range(from = 0, to = Long.MAX_VALUE)
    public long getTimestamp(@Range(from = 0, to = Long.MAX_VALUE) long epoch) {
        return ((value & TIMESTAMP_MASK) >>> TIMESTAMP_SHIFT) + epoch;
    }

    @Range(from = 0, to = Long.MAX_VALUE)
    public long getTimestamp() {
        return getTimestamp(getDefaultEpoch());
    }

    @Range(from = 0, to = MAX_DATA_CENTER_ID)
    public byte getDataCenterId() {
        return (byte) ((value & DATA_CENTER_ID_MASK) >> DATA_CENTER_ID_SHIFT);
    }

    @Range(from = 0, to = MAX_WORKER_ID)
    public byte getWorkerId() {
        return (byte) ((value & WORKER_ID_MASK) >> WORKER_ID_SHIFT);
    }

    @Range(from = 0, to = MAX_SEQUENCE)
    public short getSequence() {
        return (short) (value & SEQUENCE_MASK);
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

    @Override
    public int compareTo(Snowflake o) {
        return Long.compare(value, o.value);
    }

    @Override
    public int intValue() {
        return (int) value;
    }

    @Override
    public long longValue() {
        return value;
    }

    @Override
    public float floatValue() {
        return value;
    }

    @Override
    public double doubleValue() {
        return value;
    }

    @Override
    public void formatTo(Formatter formatter, int flags, int width, int precision) {
        boolean alternate = (flags ^ FormattableFlags.ALTERNATE) == 0;
        boolean leftJustify = (flags ^ FormattableFlags.LEFT_JUSTIFY) == 0;
        int multiple = leftJustify ? -1 : 1;
        if (alternate) {
            boolean upperCase = (flags ^ FormattableFlags.UPPERCASE) == 0;
            String format = MessageFormat.format("[%{0}{1}] @%{2}{3}d^%{4}{5}d #%{6}{7}d", multiple * 24, upperCase ? 'S' : 's', precision < 0 ? '0' : '\0', multiple * 2, precision < 0 ? '0' : '\0', multiple * 2, precision < 0 ? '0' : '\0', multiple * 4);
            OffsetDateTime timestamp = Instant.ofEpochMilli(getTimestamp()).atOffset(ZoneOffset.UTC);
            formatter.format(format, timestamp, getDataCenterId(), getWorkerId(), getSequence());
        } else {
            formatter.format(MessageFormat.format("%{0}s", multiple * width), Long.toUnsignedString(value));
        }
    }
}
