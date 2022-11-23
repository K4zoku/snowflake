package io.gitlab.k4zoku.snowflake.hibernate.test;

import io.gitlab.k4zoku.snowflake.Snowflake;
import io.gitlab.k4zoku.snowflake.SnowflakeGenerator;
import io.gitlab.k4zoku.snowflake.hibernate.test.entity.SnowflakeEntity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.*;

class SnowflakeHibernateGeneratorTest {

    static SessionFactory sessionFactory;
    static Session session;

    @BeforeAll
    static void setUp() {
        SnowflakeGenerator.setDefaultEpoch(SnowflakeGenerator.AUTHOR_EPOCH);
        Configuration configuration = new Configuration().configure();
        ServiceRegistry serviceRegistry = configuration.getStandardServiceRegistryBuilder()
            .build();
        MetadataSources metadataSources = new MetadataSources(serviceRegistry);
        metadataSources.addAnnotatedClass(SnowflakeEntity.class);
        Metadata metadata = metadataSources.buildMetadata();
        sessionFactory = metadata.buildSessionFactory();
        session = sessionFactory.openSession();
        session.beginTransaction();
    }

    @AfterAll
    static void tearDown() {
        session.getTransaction().commit();
        session.close();
        sessionFactory.close();
    }

    @Test
    @Transactional
    void test() {
        SnowflakeEntity snowflakeEntity = new SnowflakeEntity();

        SnowflakeEntity snowflakeEntity2 = new SnowflakeEntity();
        session.persist(snowflakeEntity);
        session.persist(snowflakeEntity2);

        System.out.printf("Persisted entity %s: %s%n", snowflakeEntity.getId(), snowflakeEntity.getId().toFormattedString());
        System.out.printf("Persisted entity %s: %s%n", snowflakeEntity2.getId(), snowflakeEntity2.getId().toFormattedString());

        assertNotEquals(snowflakeEntity.getId(), snowflakeEntity2.getId());

        System.out.println("Entity 1 worker ID: " + snowflakeEntity.getId().getWorkerId());
        System.out.println("Entity 2 worker ID: " + snowflakeEntity2.getId().getWorkerId());

        Snowflake id1 = snowflakeEntity.getId();
        Snowflake id2 = snowflakeEntity2.getId();
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

}
