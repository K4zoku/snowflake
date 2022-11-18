package io.gitlab.k4zoku.snowflake.hibernate.test.entity;

import io.gitlab.k4zoku.snowflake.Snowflake;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Objects;

@Entity
@Getter
@Setter
public class TestEntity {
    @Id
    @GeneratedValue(generator = "TestEntitySnowflake")
    @Setter(AccessLevel.NONE)
    private Snowflake id;

    private String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        TestEntity that = (TestEntity) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
