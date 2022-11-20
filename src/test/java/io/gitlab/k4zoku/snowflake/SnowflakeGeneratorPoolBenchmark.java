package io.gitlab.k4zoku.snowflake;

import io.gitlab.k4zoku.snowflake.concurrent.SnowflakeGeneratorPool;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class SnowflakeGeneratorPoolBenchmark {

    private SnowflakeGeneratorPool pool;

    @Setup
    public void setup() {
        SnowflakeGeneratorFactory factory = SnowflakeGeneratorFactory.builder().dataCenterId(2).build();
        pool = new SnowflakeGeneratorPool(factory);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Fork(value = 1, warmups = 1)
    @Warmup(iterations = 1)
    @Measurement(iterations = 1)
    @Threads(4)
    public void snowflakeGenerateBenchmarkMultiThread() {
        pool.generate();
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Fork(value = 1, warmups = 1)
    @Warmup(iterations = 1)
    @Measurement(iterations = 1)
    public void snowflakeGenerateBenchmarkSingleThread() {
        pool.generate();
    }

    @TearDown
    public void tearDown() {
        System.out.println();
        System.out.println("Total snowflake generated: " + pool.getGeneratedCount());
    }
}
