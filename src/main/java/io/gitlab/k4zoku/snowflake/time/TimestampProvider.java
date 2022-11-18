package io.gitlab.k4zoku.snowflake.time;

import java.io.Serializable;

public interface TimestampProvider extends Serializable {

        long getTimestamp();

        long waitUntilNextTimestamp(long lastTimestamp);

}
