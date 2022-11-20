package io.gitlab.k4zoku.snowflake;

import io.gitlab.k4zoku.snowflake.concurrent.SnowflakeGeneratorPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class SnowflakeGeneratorPoolTest {

    SnowflakeGeneratorPool pool;

    @BeforeEach
    void setUp() {
        SnowflakeGeneratorFactory factory = SnowflakeGeneratorFactory.builder().dataCenterId(26).build();
        pool = new SnowflakeGeneratorPool(factory, 32);
    }

    @Execution(ExecutionMode.CONCURRENT)
    @ParameterizedTest
    @ValueSource(ints = {65536, 65536 * 2, 65536 * 4, 65536 * 8, 65536 * 16, 65536 * 32, 65536 * 64, 65536 * 128})
    void multiThreadGenerateTest(int n) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        for (int i = 0; i < n; i++) {
            executor.submit(() -> {
                Snowflake snowflake = pool.generate();
                assertNotEquals(0, snowflake.getAsLong());
            });
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS));
        assertEquals(n, pool.getGeneratedCount());
    }
}
