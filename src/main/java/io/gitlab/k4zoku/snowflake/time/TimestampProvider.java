package io.gitlab.k4zoku.snowflake.time;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public interface TimestampProvider extends Serializable {

        long getTimestamp();

        default long waitUntilNextTimestamp(long lastTimestamp) {
            long timestamp;
            do {
                timestamp = getTimestamp();
            } while (timestamp == lastTimestamp);
            return timestamp;
        }

        static TimestampProvider system() {
            return System::currentTimeMillis;
        }

        static TimestampProvider getInstance(String className) {
            if (className == null || className.isEmpty()) { // Default to system clock
                return system();
            }
            try {
                Class<? extends TimestampProvider> clazz = Class.forName(className).asSubclass(TimestampProvider.class);
                Constructor<? extends TimestampProvider> constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true);
                return constructor.newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException |
                     InvocationTargetException e) {
                throw new IllegalArgumentException("Unable to instantiate custom TimestampProvider: " + className, e);
            }
        }

}
