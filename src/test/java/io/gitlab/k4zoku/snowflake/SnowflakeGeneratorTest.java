package io.gitlab.k4zoku.snowflake;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SnowflakeGeneratorTest {

    private SnowflakeGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new SnowflakeGenerator(0, 31);
    }

    @Test
    void testGeneratorSetting() {
        Snowflake snowflake = generator.generate();
        assertNotEquals(0, snowflake.getAsLong());
        assertEquals(snowflake.getTimestamp(generator.getEpoch()), snowflake.getTimestamp());
        assertEquals(generator.getDataCenterId(), snowflake.getDataCenterId());
        assertEquals(generator.getWorkerId(), snowflake.getWorkerId());
        assertEquals(0, snowflake.getSequence());
    }

    @Test
    void testCompare() {
        Snowflake snowflake1 = generator.generate();
        Snowflake snowflake2 = generator.generate();
        assertTrue(snowflake1.compareTo(snowflake2) < 0);
    }
}
