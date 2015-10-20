package net.ogify.database;

import net.ogify.database.entities.BetaKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Created by melge on 18.10.2015.
 */
@Component
public class BetaKeyController {
    @Autowired
    EntityManagerService entityManagerService;

    public BetaKey getByKey(String key) {
        EntityManager em = entityManagerService.createEntityManager();
        try {
            TypedQuery<BetaKey> query = em.createNamedQuery("BetaKey.getByKey", BetaKey.class);
            query.setParameter("betaKey", key);

            List<BetaKey> result = query.getResultList();
            if(result.size() != 1)
                return null;

            return result.get(0);
        } finally {
            em.close();
        }
    }

    public void saveOrUpdate(BetaKey betaKey) {
        EntityManager em = entityManagerService.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(betaKey);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}
