package net.ogify.database;

import net.ogify.database.entities.SocialNetwork;
import net.ogify.database.entities.SocialToken;
import net.ogify.database.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Class for work with users records in database
 *
 * @author Morgen Matvey melges.morgen@gmail.com
 */
@Component
public class UserController {
    @Autowired
    private EntityManagerService entityManagerService;

    public List<Long> getAllUsersIds(int maxCount, int pageNumber) {
        EntityManager em = entityManagerService.createEntityManager();
        try {
            TypedQuery<Long> query = em.createNamedQuery("User.getAllIds", Long.class);
            query.setFirstResult(maxCount * pageNumber);
            query.setMaxResults(maxCount);

            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public User getUserById(Long userId) {
        EntityManager em = entityManagerService.createEntityManager();
        try {
            TypedQuery<User> query = em.createNamedQuery("User.getById", User.class);
            query.setParameter("id", userId);
            List<User> resultList = query.getResultList();

            if(resultList.size() == 1)
                return resultList.get(0);
            if(resultList.size() == 0)
                return null;

            throw new NonUniqueResultException("We receive more then one user with specified id, it mustn't happened");
        } finally {
            em.close();
        }
    }

    public List<User> getUsersWithIds(Set<Long> ids) {
        if(ids.isEmpty())
            return new ArrayList<>();

        EntityManager em = entityManagerService.createEntityManager();
        try {
            TypedQuery<User> query = em.createNamedQuery("User.getUsersByIds", User.class);
            query.setParameter("ids", ids);

            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<User> getUserWithVkIds(Set<Long> userIds) {
        if(userIds.isEmpty())
            return new ArrayList<>();

        EntityManager em = entityManagerService.createEntityManager();
        try {
            TypedQuery<User> query = em.createNamedQuery("User.getUsersByVkIds", User.class);
            query.setParameter("vkIds", userIds);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public User getUserByFbId(Long userId) {
        EntityManager em = entityManagerService.createEntityManager();
        try {
            TypedQuery<User> query = em.createNamedQuery("User.getUserByFbId", User.class);
            query.setParameter("fbId", userId);
            List<User> resultList = query.getResultList();

            if(resultList.size() == 1)
                return resultList.get(0);
            if(resultList.size() == 0)
                return null;

            throw new NonUniqueResultException("We receive more then one user with specified id, it mustn't happened");
        } finally {
            em.close();
        }
    }

    public User getUserByVkId(Long userId) {
        EntityManager em = entityManagerService.createEntityManager();
        try {
            TypedQuery<User> query = em.createNamedQuery("User.getUserByVkId", User.class);
            query.setParameter("vkId", userId);
            List<User> resultList = query.getResultList();

            if(resultList.size() == 1)
                return resultList.get(0);
            if(resultList.size() == 0)
                return null;

            throw new NonUniqueResultException("We receive more then one user with specified id, it mustn't happened");
        } finally {
            em.close();
        }
    }

    public User getUserByIdAndSession(Long userId, String sessionSecret) {
        EntityManager em = entityManagerService.createEntityManager();
        try {
            TypedQuery<User> query = em.createNamedQuery("User.getByIdAndSession", User.class);
            query.setParameter("userId", userId);
            query.setParameter("sessionSecret", sessionSecret);
            List<User> resultList = query.getResultList();

            if(resultList.size() == 1)
                return resultList.get(0);
            if(resultList.size() == 0)
                return null;

            throw new NonUniqueResultException("We receive more then one user with specified id, it mustn't happened");
        } finally {
            em.close();
        }
    }

    public SocialToken getUserAuthToken(User owner, SocialNetwork network) {
        EntityManager em = entityManagerService.createEntityManager();
        try {
            TypedQuery<SocialToken> query = em.createNamedQuery("SocialToken.getNewestUsersToken", SocialToken.class);
            query.setParameter("owner", owner);
            query.setParameter("socialNetwork", network);
            query.setMaxResults(1);

            List<SocialToken> resultList = query.getResultList();

            if(resultList.size() == 1)
                return resultList.get(0);
            if(resultList.size() == 0)
                return null;

            throw new NonUniqueResultException("We receive more then one tokens with, it mustn't happened");
        } finally {
            em.close();
        }
    }

    public void saveOrUpdate(User user) {
        EntityManager em = entityManagerService.createEntityManager();
        try {
            em.getTransaction().begin();
            if (user.getId() == null)
                em.persist(user);
            else
                em.merge(user);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}
