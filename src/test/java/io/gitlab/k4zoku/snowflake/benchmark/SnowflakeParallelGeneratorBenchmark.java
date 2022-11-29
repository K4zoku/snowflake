package io.gitlab.k4zoku.snowflake.benchmark;

import io.gitlab.k4zoku.snowflake.SnowflakeGeneratorFactory;
import io.gitlab.k4zoku.snowflake.parallel.SnowflakeParallelGenerator;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(value = 1, warmups = 1)
@Warmup(iterations = 1)
@Measurement(iterations = 1)
public class SnowflakeParallelGeneratorBenchmark {

    private SnowflakeParallelGenerator generator;

    @Setup
    public void setup() {
        SnowflakeGeneratorFactory factory = SnowflakeGeneratorFactory.builder().dataCenterId(2).build();
        this.generator = new SnowflakeParallelGenerator(factory, 1);
    }

    @Benchmark
    @Threads(Threads.MAX)
    public void snowflakeGenerateBenchmarkMultiThread() {
        generator.generate();
    }

    @Benchmark
    public void snowflakeGenerateBenchmarkSingleThread() {
        generator.generate();
    }
}
