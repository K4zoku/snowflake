package io.gitlab.k4zoku.snowflake;

import io.gitlab.k4zoku.snowflake.time.TimestampProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.io.Serializable;
import java.util.Objects;

/**
 * <p>
 * <q>Snowflake is a network service for generating unique ID numbers at high scale with some simple guarantees.</q>
 * </p>
 * <p>This is an implementation of <b>Twitter Snowflake</b> ID generator on Java. This class is thread-safe.</p>
 * <p>
 * Structure of a snowflake:
 * <pre>
 *          0 00000000000000000000000000000000000000000 00000 00000 000000000000
 *          1 2                                         3     4     5
 *      </pre>
 *     <ol>
 *         <li>
 *             Sign bit
 *             <dl>
 *                 <dt>Length</dt>
 *                 <dd>1 bit</dd>
 *                 <dt>Value</dt>
 *                 <dd>Always 0</dd>
 *                 <dt>Meaning</dt>
 *                 <dd>Keep the sign bit of the snowflake ID positive. Or reserved for future use.</dd>
 *             </dl>
 *         </li>
 *         <li>
 *             Timestamp
 *             <dl>
 *                 <dt>Length</dt>
 *                 <dd>41 bits</dd>
 *                 <dt>Value</dt>
 *                 <dd>Number of milliseconds since the epoch (Default epoch: <time datetime="2022-01-01T00:00:00+00:00">January 1, 2022, 00:00:00 GMT</time>).</dd>
 *                 <dt>Meaning</dt>
 *                 <dd>Used to identify the time when the snowflake is generated.</dd>
 *                 <dt>Limitation</dt>
 *                 <dd>69.7 years since the epoch, ids can be repeated after 69.7 years.</dd>
 *             </dl>
 *         </li>
 *         <li>
 *             Data center ID
 *             <dl>
 *                 <dt>Length</dt>
 *                 <dd>5 bits</dd>
 *                 <dt>Value</dt>
 *                 <dd>Non-negative integer that is used to identify the data center where the snowflake is generated.</dd>
 *                 <dt>Meaning</dt>
 *                 <dd>Decentralize the snowflake ID generation. Different data centers can generate snowflake IDs independently.</dd>
 *                 <dt>Limitation</dt>
 *                 <dd>Maximum value: 31</dd>
 *             </dl>
 *         </li>
 *         <li>
 *             Worker ID
 *             <dl>
 *                 <dt>Length</dt>
 *                 <dd>5 bits</dd>
 *                 <dt>Value</dt>
 *                 <dd>Non-negative integer that is used to identify the worker that generates the snowflake.</dd>
 *                 <dt>Meaning</dt>
 *                 <dd>Decentralize the snowflake ID generation. Same data center can generate snowflake IDs independently.</dd>
 *                 <dt>Limitation</dt>
 *                 <dd>Maximum value: 31</dd>
 *                 <dt>Recommendation</dt>
 *                 <dd>Use the number of CPU cores as the worker ID.</dd>
 *                 <dd>For example, if the number of CPU cores is 8, the worker ID can be 0, 1, 2, 3, 4, 5, 6, 7.</dd>
 *              </dl>
 *          </li>
 *          <li>
 *              Sequence
 *              <dl>
 *                  <dt>Length</dt>
 *                  <dd>12 bits</dd>
 *                  <dt>Value</dt>
 *                  <dd>Non-negative integer that is used to identify the order of snowflakes generated by the same worker in the same millisecond.</dd>
 *                  <dt>Meaning</dt>
 *                  <dd>Keep the snowflake ID unique in the same millisecond.</dd>
 *                  <dt>Limitation</dt>
 *                  <dd>Maximum value: 4095</dd>
 *              </dl>
 *          </li>
 *     </ol>
 * </p>
 *
 * @see <a href="https://github.com/twitter/snowflake">snowflake</a>
 * @see <a href="https://twitter.com">Twitter</a>
 */
public class SnowflakeGenerator implements Serializable, Comparable<SnowflakeGenerator> {

    private static final long serialVersionUID = 0L;

    // LENGTHS
    public static final long TIMESTAMP_BITS = 41L;
    public static final long DATA_CENTER_ID_BITS = 5L;
    public static final long WORKER_ID_BITS = 5L;
    public static final long SEQUENCE_BITS = 12L;

    // MAX VALUES
    public static final long MAX_TIMESTAMP = ~(-1L << TIMESTAMP_BITS);
    public static final long MAX_DATA_CENTER_ID = ~(-1L << DATA_CENTER_ID_BITS);
    public static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    public static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    // OFFSETS
    public static final long TIMESTAMP_SHIFT = DATA_CENTER_ID_BITS + WORKER_ID_BITS + SEQUENCE_BITS;
    public static final long DATA_CENTER_ID_SHIFT = WORKER_ID_BITS + SEQUENCE_BITS;
    public static final long WORKER_ID_SHIFT = SEQUENCE_BITS;

    // MASKS
    public static final long TIMESTAMP_MASK = MAX_TIMESTAMP << TIMESTAMP_SHIFT;
    public static final long DATA_CENTER_ID_MASK = MAX_DATA_CENTER_ID << DATA_CENTER_ID_SHIFT;
    public static final long WORKER_ID_MASK = MAX_WORKER_ID << WORKER_ID_SHIFT;
    public static final long SEQUENCE_MASK = MAX_SEQUENCE;

    // DEFAULT VALUES
    public static final long DISCORD_EPOCH = 1420070400000L; // Equivalent to 2015-01-01T00:00:00+00:00, the epoch of Discord (https://discord.com/developers/docs/reference#snowflakes)
    public static final long AUTHOR_EPOCH = 1640995200000L; // Equivalent to 2022-01-01T00:00:00+00:00, the epoch of the author of this library
    private static long defaultEpoch = DISCORD_EPOCH; // Default epoch is the epoch of Discord
    public static final TimestampProvider DEFAULT_TIMESTAMP_PROVIDER = TimestampProvider.system(); // Using System.currentTimeMillis()

    public static void setDefaultEpoch(long epoch) {
        if (epoch < 0) {
            throw new IllegalArgumentException("Epoch cannot be negative.");
        }
        defaultEpoch = epoch;
    }

    public static long getDefaultEpoch() {
        return defaultEpoch;
    }

    // INSTANCE FIELDS
    private final long epoch;
    private final long dataCenterId;
    private final long workerId;
    private volatile long sequence = 0L;

    private final long template; // pre-computed snowflake template with initialized data center ID and worker ID

    private transient volatile long lastTimestamp = -1L;
    private transient TimestampProvider timestampProvider;

    /**
     * Create a snowflake generator with custom epoch and timestamp provider.
     * @param epoch The epoch of the snowflake generator. (milliseconds since unix epoch)
     * @param dataCenterId The data center ID of the snowflake generator. Out of range value will be truncated.
     * @param workerId The worker ID of the snowflake generator. Out of range value will be truncated.
     * @param timestampProvider The timestamp provider of the snowflake generator.
     */
    public SnowflakeGenerator(long epoch, @Range(from = 0, to = MAX_DATA_CENTER_ID) long dataCenterId,  @Range(from = 0, to = MAX_WORKER_ID) long workerId, @NotNull TimestampProvider timestampProvider) {
        this.epoch = epoch;
        this.dataCenterId = dataCenterId & MAX_DATA_CENTER_ID;
        this.workerId = workerId & MAX_WORKER_ID;
        this.timestampProvider = timestampProvider;
        this.template = (dataCenterId << DATA_CENTER_ID_SHIFT) | (workerId << WORKER_ID_SHIFT);
    }

    /**
     * Create a snowflake generator with custom epoch.
     * @param epoch The epoch of the snowflake generator. (milliseconds since unix epoch)
     * @param dataCenterId The data center ID of the snowflake generator. Out of range value will be truncated.
     * @param workerId The worker ID of the snowflake generator. Out of range value will be truncated.
     */
    public SnowflakeGenerator(long epoch, @Range(from = 0, to = MAX_DATA_CENTER_ID) long dataCenterId,  @Range(from = 0, to = MAX_WORKER_ID) long workerId) {
        this(epoch, dataCenterId, workerId, DEFAULT_TIMESTAMP_PROVIDER);
    }

    /**
     * Create a snowflake generator with default epoch and timestamp provider.
     * @param dataCenterId The data center ID of the snowflake generator. Out of range value will be truncated.
     * @param workerId The worker ID of the snowflake generator. Out of range value will be truncated.
     */
    public SnowflakeGenerator(@Range(from = 0, to = MAX_DATA_CENTER_ID) long dataCenterId,  @Range(from = 0, to = MAX_WORKER_ID) long workerId) {
        this(defaultEpoch, dataCenterId, workerId);
    }

    /**
     * Gets the epoch of the snowflake generator.
     * @return The epoch of the snowflake generator.
     */
    public long getEpoch() {
        return epoch;
    }

    /**
     * Gets the data center ID of the snowflake generator.
     * @return The data center ID of the snowflake generator.
     */
    public long getDataCenterId() {
        return dataCenterId;
    }

    /**
     * Gets the worker ID of the snowflake generator.
     * @return The worker ID of the snowflake generator.
     */
    public long getWorkerId() {
        return workerId;
    }

    /**
     * Gets the timestamp provider of the snowflake generator.
     * @return The timestamp provider of the snowflake generator.
     */
    public TimestampProvider getTimestampProvider() {
        return timestampProvider;
    }

    /**
     * Sets the timestamp provider of the snowflake generator.
     * @param timestampProvider The timestamp provider of the snowflake generator.
     */
    public void setTimestampProvider(TimestampProvider timestampProvider) {
        this.timestampProvider = timestampProvider;
    }

    /**
     * Generates a Snowflake.
     * @return The generated Snowflake.
     */
    public synchronized Snowflake generate() {
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
        long value = (timestamp - epoch) << TIMESTAMP_SHIFT | template | sequence;
        return new Snowflake(value);
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
