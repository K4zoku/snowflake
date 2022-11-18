package io.gitlab.k4zoku.snowflake.hibernate;

import io.gitlab.k4zoku.snowflake.Snowflake;
import io.gitlab.k4zoku.snowflake.concurrent.SnowflakeGeneratorPool;
import io.gitlab.k4zoku.snowflake.time.TimestampProvider;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;

import java.io.Serializable;
import java.util.Optional;
import java.util.Properties;

import static io.gitlab.k4zoku.snowflake.SnowflakeGenerator.DEFAULT_EPOCH;

public class SnowflakeHibernateGenerator implements IdentifierGenerator {

    public static final String SNOWFLAKE_EPOCH = "snowflake.epoch";
    public static final String SNOWFLAKE_DATA_CENTER_ID = "snowflake.dataCenterId";
    public static final String SNOWFLAKE_WORKERS = "snowflake.workers";
    public static final String SNOWFLAKE_TIMESTAMP_PROVIDER = "snowflake.timestampProvider";

    private SnowflakeGeneratorPool generator;
    private boolean raw = false;

    @Override
    public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {
        if (long.class.isAssignableFrom(type.getReturnedClass()) || Long.class.isAssignableFrom(type.getReturnedClass())) {
            raw = true;
        } else if (!Snowflake.class.isAssignableFrom(type.getReturnedClass())) {
            throw new MappingException("SnowflakeGenerator only supports Snowflake or Long");
        }
        long epoch = Optional.ofNullable(params.getProperty(SNOWFLAKE_EPOCH))
                .map(Long::parseLong)
                .orElse(DEFAULT_EPOCH);
        int dataCenterId = Optional.ofNullable(params.getProperty(SNOWFLAKE_DATA_CENTER_ID))
                .map(Integer::parseInt)
                .orElseThrow(() ->
                        new MappingException(String.format("Parameter '%s' is required", SNOWFLAKE_DATA_CENTER_ID)));
        int workers = Optional.ofNullable(params.getProperty(SNOWFLAKE_WORKERS))
                .map(Integer::parseInt)
                .orElse(0);
        TimestampProvider timestampProvider = TimestampProvider.getInstance(params.getProperty(SNOWFLAKE_TIMESTAMP_PROVIDER));
        generator = new SnowflakeGeneratorPool(epoch, dataCenterId, workers, timestampProvider);
    }

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) {
        return raw ? generator.generateId() : generator.generate();
    }


}
