package io.gitlab.k4zoku.snowflake.hibernate.test;

import io.gitlab.k4zoku.snowflake.Snowflake;
import io.gitlab.k4zoku.snowflake.SnowflakeGenerator;
import io.gitlab.k4zoku.snowflake.hibernate.test.entity.TestEntity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.transaction.Transactional;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class SnowflakeHibernateGeneratorTest {

    Session session;

    Set<Class<?>> findAllClassesUsingClassLoader(String packageName) {
        InputStream stream = ClassLoader.getSystemClassLoader()
            .getResourceAsStream(packageName.replace('.', '/'));
        if (stream == null) {
            throw new IllegalArgumentException("Package " + packageName + " not found");
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        return reader.lines()
            .filter(line -> line.endsWith(".class"))
            .map(line -> getClass(line, packageName))
            .collect(Collectors.toSet());
    }

    private Class<?> getClass(String className, String packageName) {
        try {
            return Class.forName(packageName + "."
                + className.substring(0, className.lastIndexOf('.')));
        } catch (ClassNotFoundException ignore) {
            // ignore
        }
        return null;
    }

    @BeforeEach
    void setUp() {
        SnowflakeGenerator.setDefaultEpoch(SnowflakeGenerator.AUTHOR_EPOCH);
        Configuration configuration = new Configuration()
            .configure()
            .addPackage("io.gitlab.k4zoku.snowflake.hibernate.test.entity");
        for (Class<?> clazz : findAllClassesUsingClassLoader("io.gitlab.k4zoku.snowflake.hibernate.test.entity")) {
            configuration.addAnnotatedClass(clazz);
        }
        ServiceRegistry serviceRegistry = configuration.getStandardServiceRegistryBuilder()
            .build();
        SessionFactory sessionFactory = configuration.buildSessionFactory(serviceRegistry);
        session = sessionFactory.getCurrentSession();
        session.beginTransaction();
    }

    @AfterEach
    void tearDown() {
        session.getTransaction().commit();
        session.close();
    }

    @Test
    @Transactional
    void test() {
        TestEntity testEntity = new TestEntity();
        testEntity.setName("test 1");

        TestEntity testEntity2 = new TestEntity();
        testEntity2.setName("test 2");

        session.persist(testEntity);
        session.persist(testEntity2);

        System.out.printf("Persisted entity %s with ID %s%n", testEntity.getName(), testEntity.getId());
        System.out.printf("Persisted entity %s with ID %s%n", testEntity2.getName(), testEntity2.getId());

        assertNotEquals(testEntity.getId(), testEntity2.getId());
        Snowflake id1 = testEntity.getId();
        Snowflake id2 = testEntity2.getId();
        if (id1.getTimestamp() == id2.getTimestamp()) {
            System.out.println("The timestamps of the two IDs are equal");
            assertTrue(id1.getSequence() < id2.getSequence());
            assertEquals(0, id1.getDataCenterId());
            assertEquals(1, id2.getDataCenterId());
        } else {
            System.out.println("The timestamps of the two IDs are not equal");
            assertTrue(id1.getTimestamp() < id2.getTimestamp());
            assertEquals(0, id1.getSequence());
            assertEquals(0, id2.getSequence());
        }
    }

}
