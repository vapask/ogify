package net.ogify.database;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Service for creating entity managers associated with database, configured via properties.
 */
@Service
public class EntityManagerService {
    @Value("${db.type:LocalDB}")
    String persistenceUnitName;

    private EntityManagerFactory entityManagerFactory;

    @PostConstruct
    private void init() {
        entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnitName);
    }

    public EntityManager createEntityManager() {
        return entityManagerFactory.createEntityManager();
    }
}
