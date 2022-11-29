package io.gitlab.k4zoku.snowflake.benchmark;

import io.gitlab.k4zoku.snowflake.SnowflakeGenerator;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(value = 1, warmups = 1)
@Warmup(iterations = 1)
@Measurement(iterations = 1)
public class SnowflakeGeneratorBenchmark {
    private SnowflakeGenerator generator;

    @Setup
    public void setup() {
        this.generator = new SnowflakeGenerator(3, 0);
    }

    @Benchmark
    @Threads(Threads.MAX)
    public void snowflakeGenerateBenchmarkMultiThread() {

    }

    @Benchmark
    public void snowflakeGenerateBenchmarkSingleThread() {

    }
}
