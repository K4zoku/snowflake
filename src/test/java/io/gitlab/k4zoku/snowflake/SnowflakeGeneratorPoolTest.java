package io.gitlab.k4zoku.snowflake;

import io.gitlab.k4zoku.snowflake.concurrent.SnowflakeGeneratorPool;
import io.gitlab.k4zoku.snowflake.time.TimestampProvider;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SnowflakeGeneratorPoolTest {

    @Test
    void test() throws InterruptedException {
        SnowflakeGeneratorPool pool = new SnowflakeGeneratorPool(SnowflakeGenerator.DISCORD_EPOCH, 26, 32, 3, 0, TimestampProvider.system());
        ExecutorService executor = Executors.newFixedThreadPool(1000);
        for (int i = 0; i < 1000; i++) {
            executor.submit(() -> {
                for (int j = 0; j < 100; j++) {
                    System.out.println(pool.generate().toFormattedString());
                }
            });
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(1, java.util.concurrent.TimeUnit.MINUTES));
    }
}
