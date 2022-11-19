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

import static org.junit.jupiter.api.Assertions.*;

class SnowflakeHibernateGeneratorTest {

    Session session;

    @BeforeEach
    void setUp() {
        SnowflakeGenerator.setDefaultEpoch(SnowflakeGenerator.AUTHOR_EPOCH);
        Configuration configuration = new Configuration()
            .configure()
            .addAnnotatedClass(TestEntity.class)
            .addPackage("io.gitlab.k4zoku.snowflake.hibernate.test.entity");
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

        System.out.printf("Persisted entity %s: %s%n", testEntity.getId(), testEntity.getId().toFormattedString());
        System.out.printf("Persisted entity %s: %s%n", testEntity2.getId(), testEntity2.getId().toFormattedString());

        assertNotEquals(testEntity.getId(), testEntity2.getId());

        System.out.println("Entity 1 worker ID: " + testEntity.getId().getWorkerId());
        System.out.println("Entity 2 worker ID: " + testEntity2.getId().getWorkerId());

        Snowflake id1 = testEntity.getId();
        Snowflake id2 = testEntity2.getId();
        if (id1.getTimestamp() == id2.getTimestamp()) {
            System.out.println("The timestamps of the two IDs are equal");
            if (id1.getWorkerId() == id2.getWorkerId()) {
                System.out.println("The worker IDs of the two IDs are equal");
                assertTrue(id1.getSequence() < id2.getSequence());
                assertEquals(0, id1.getSequence());
                assertEquals(1, id2.getSequence());
            } else {
                System.out.println("The worker IDs of the two IDs are not equal");
                assertEquals(0, id1.getSequence());
                assertEquals(0, id2.getSequence());
            }
        } else {
            System.out.println("The timestamps of the two IDs are not equal");
            assertTrue(id1.getTimestamp() < id2.getTimestamp());
            assertEquals(0, id1.getSequence());
            assertEquals(0, id2.getSequence());
        }
    }

    @Test
    @Transactional
    void testMultiple() {
        for (int i = 0; i < 100; i++) {
            TestEntity testEntity = new TestEntity();
            testEntity.setName("test " + i);
            session.persist(testEntity);
            System.out.printf("Persisted entity %s: %s%n", testEntity.getId(), testEntity.getId().toFormattedString());
        }
    }

}
