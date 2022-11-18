package io.gitlab.k4zoku.snowflake.hibernate;

import io.gitlab.k4zoku.snowflake.Snowflake;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.descriptor.sql.BigIntTypeDescriptor;

public class SnowflakeType extends AbstractSingleColumnStandardBasicType<Snowflake> {

    public static final SnowflakeType INSTANCE = new SnowflakeType();

    public SnowflakeType() {
        super(BigIntTypeDescriptor.INSTANCE, SnowflakeTypeDescriptor.INSTANCE);
    }

    @Override
    public String getName() {
        return "snowflake";
    }
}
