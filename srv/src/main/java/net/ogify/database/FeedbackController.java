package net.ogify.database;

import net.ogify.database.entities.Feedback;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Created by melges on 15.03.2015.
 */
@Component
public class FeedbackController {
    @Autowired
    EntityManagerService entityManagerService;

    public void save(Feedback feedback) throws EntityExistsException {
        EntityManager em = entityManagerService.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(feedback);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}
