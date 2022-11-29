package io.gitlab.k4zoku.snowflake.hibernate;

import io.gitlab.k4zoku.snowflake.Snowflake;
import io.gitlab.k4zoku.snowflake.SnowflakeGenerator;
import io.gitlab.k4zoku.snowflake.SnowflakeGeneratorFactory;
import io.gitlab.k4zoku.snowflake.common.Generator;
import io.gitlab.k4zoku.snowflake.common.util.ProxyGenerator;
import io.gitlab.k4zoku.snowflake.parallel.SnowflakeParallelGenerator;
import io.gitlab.k4zoku.snowflake.time.TimestampProvider;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;

import java.io.Serializable;
import java.util.Optional;
import java.util.Properties;

public class SnowflakeHibernateGenerator implements IdentifierGenerator {

    public static final String SNOWFLAKE_EPOCH = "snowflake.epoch";
    public static final String SNOWFLAKE_DATA_CENTER_ID = "snowflake.dataCenterId";
    public static final String SNOWFLAKE_WORKER_ID = "snowflake.workerId";
    public static final String SNOWFLAKE_PARALLEL = "snowflake.parallel";
    public static final String SNOWFLAKE_WORKERS = "snowflake.workers";
    public static final String SNOWFLAKE_TIMESTAMP_PROVIDER = "snowflake.timestampProvider";

    private volatile Generator<Serializable> generator;

    @Override
    public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {
        // type checking
        boolean longValue;
        if (long.class.isAssignableFrom(type.getReturnedClass()) || Long.class.isAssignableFrom(type.getReturnedClass())) {
            longValue = true;
        } else if (Snowflake.class.isAssignableFrom(type.getReturnedClass())) {
            longValue = false;
        } else {
            throw new MappingException("SnowflakeGenerator only supports Snowflake or Long");
        }

        // configure generator
        try {
            configureGenerator(params, longValue);
        } catch (IllegalArgumentException e) {
            throw new MappingException("Invalid configuration", e);
        } catch (Exception e) {
            throw new MappingException("Failed to configure SnowflakeGenerator", e);
        }
    }

    protected void configureGenerator(Properties params, boolean longValue) {
        long epoch = Optional.ofNullable(params.getProperty(SNOWFLAKE_EPOCH))
            .map(Long::parseLong)
            .orElse(SnowflakeGenerator.getDefaultEpoch());
        byte dataCenterId = Optional.ofNullable(params.getProperty(SNOWFLAKE_DATA_CENTER_ID))
            .map(Byte::parseByte)
            .orElseThrow(() ->
                new MappingException(String.format("Parameter '%s' is required", SNOWFLAKE_DATA_CENTER_ID)));
        boolean parallel = Optional.ofNullable(params.getProperty(SNOWFLAKE_PARALLEL))
            .map(Boolean::parseBoolean)
            .orElse(false);
        TimestampProvider timestampProvider = TimestampProvider.getInstance(params.getProperty(SNOWFLAKE_TIMESTAMP_PROVIDER));
        Generator<Snowflake> snowflakeGenerator;
        if (parallel) {
            byte workers = Optional.ofNullable(params.getProperty(SNOWFLAKE_WORKERS))
                .map(Byte::parseByte)
                .orElseGet(() -> (byte) (SnowflakeGenerator.MAX_WORKER_ID + 1));
            byte offset = Optional.ofNullable(params.getProperty(SNOWFLAKE_WORKER_ID))
                .map(Byte::parseByte)
                .orElse((byte) 0);
            SnowflakeGeneratorFactory factory = SnowflakeGeneratorFactory.builder()
                .epoch(epoch)
                .dataCenterId(dataCenterId)
                .timestampProvider(timestampProvider)
                .build();
            snowflakeGenerator = new SnowflakeParallelGenerator(factory, workers, offset);
        } else {
            byte workerId = Optional.ofNullable(params.getProperty(SNOWFLAKE_WORKER_ID))
                .map(Byte::parseByte)
                .orElseThrow(() ->
                    new MappingException(String.format("Parameter '%s' is required", SNOWFLAKE_WORKER_ID)));
            snowflakeGenerator = new SnowflakeGenerator(epoch, dataCenterId, workerId, timestampProvider);
        }
        if (longValue) {
            this.generator = ProxyGenerator.create(snowflakeGenerator, Snowflake::longValue);
        } else {
            this.generator = ProxyGenerator.create(snowflakeGenerator, Serializable.class);
        }
    }

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) {
        try {
            return generator.generate();
        } catch (Exception e) {
            throw new HibernateException(e);
        }
    }


}
