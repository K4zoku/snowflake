@TypeDef(name = "snowflake", typeClass = SnowflakeType.class, defaultForType = Snowflake.class)
@GenericGenerator(
        name = "TestEntitySnowflake",
        strategy = "io.gitlab.k4zoku.snowflake.hibernate.SnowflakeHibernateGenerator",
        parameters = {
                @Parameter(name = SNOWFLAKE_DATA_CENTER_ID, value = "1")
        }
)
package io.gitlab.k4zoku.snowflake.hibernate.test.entity;

import io.gitlab.k4zoku.snowflake.Snowflake;
import io.gitlab.k4zoku.snowflake.hibernate.SnowflakeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.TypeDef;

import static io.gitlab.k4zoku.snowflake.hibernate.SnowflakeHibernateGenerator.SNOWFLAKE_DATA_CENTER_ID;