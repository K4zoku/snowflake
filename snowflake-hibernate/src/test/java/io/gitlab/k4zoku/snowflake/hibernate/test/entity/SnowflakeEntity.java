package io.gitlab.k4zoku.snowflake.hibernate.test.entity;

import io.gitlab.k4zoku.snowflake.Snowflake;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Objects;

import static io.gitlab.k4zoku.snowflake.hibernate.SnowflakeHibernateGenerator.SNOWFLAKE_DATA_CENTER_ID;

@Entity
@Getter
@Setter
@GenericGenerator(
    name = "TestEntitySnowflake",
    strategy = "io.gitlab.k4zoku.snowflake.hibernate.SnowflakeHibernateGenerator",
    parameters = {
        @Parameter(name = SNOWFLAKE_DATA_CENTER_ID, value = "1")
    }
)
@TypeDef(
    name = "snowflake",
    typeClass = io.gitlab.k4zoku.snowflake.hibernate.SnowflakeType.class,
    defaultForType = Snowflake.class
)
public class SnowflakeEntity {
    @Id
    @GeneratedValue(strategy = javax.persistence.GenerationType.AUTO, generator = "TestEntitySnowflake")
    @Column(nullable = false)
    @Setter(AccessLevel.NONE)
    private Snowflake id;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        SnowflakeEntity that = (SnowflakeEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
