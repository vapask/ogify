package net.ogify.database;

import net.ogify.database.entities.Feedback;
import net.ogify.database.entities.Order;
import net.ogify.database.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by melges.morgen on 15.02.15.
 */
@Component
public class OrderController {
    @Autowired
    private EntityManagerService entityManagerService;

    public Order getOrderById(Long id) {
        EntityManager em = entityManagerService.createEntityManager();
        try {
            return em.find(Order.class, id);
        } finally {
            em.close();
        }
    }

    public List<Order> getNearestOrders(Double latitude, Double longitude) {
        EntityManager em = entityManagerService.createEntityManager();
        try {
            TypedQuery<Order> query = em.createNamedQuery("Order.getNearestOrder", Order.class);
            query.setParameter("latitude", latitude);
            query.setParameter("longitude", longitude);

            query.setParameter("enumOrderAll", Order.OrderNamespace.All);
            query.setParameter("enumOrderNew", Order.OrderStatus.New);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Method return all users orders (where user is owner or executor)
     *
     * @param userId id of user which orders should be returned.
     * @param firstResult position of the first result, numbered from 0.
     * @param maxResults maximum number of results.
     * @return users orders.
     */
    public List<Order> getUsersOrders(Long userId, int firstResult, int maxResults) {
        EntityManager em = entityManagerService.createEntityManager();
        try {
            User user = em.find(User.class, userId);

            TypedQuery<Order> query = em.createNamedQuery("Order.getUsersOrders", Order.class);
            query.setParameter("user", user);

            query.setFirstResult(firstResult);
            query.setMaxResults(maxResults);

            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Order getUsersOrder(Long userId, Long orderId) {
        EntityManager em = entityManagerService.createEntityManager();
        try {
            User user = em.find(User.class, userId);

            TypedQuery<Order> query = em.createNamedQuery("Order.getUsersOrderById", Order.class);
            query.setParameter("user", user);
            query.setParameter("orderId", orderId);

            List<Order> resultList = query.getResultList();
            if(resultList.size() == 1)
                return resultList.get(0);
            if(resultList.size() == 0)
                return null;

            throw new NonUniqueResultException("We receive more then one order with specified id, it mustn't happened");
        } finally {
            em.close();
        }
    }

    public List<Order> getNearestOrdersFiltered(Long userId, Set<Long> userFriends, Set<Long> extendedFriends,
                                                Double neLatitude, Double neLongitude,
                                                Double swLatitude, Double swLongitude) {
        EntityManager em = entityManagerService.createEntityManager();
        try {
            User user = em.find(User.class, userId);

            // If friendsIds or extended friendsIds set is empty add dummy id for correct syntax in query
            Set<Long> searchFriendsIdsSet;
            if(userFriends.isEmpty()) {
                searchFriendsIdsSet = new HashSet<>();
                searchFriendsIdsSet.add(-1L);
            } else
                searchFriendsIdsSet = userFriends;

            Set<Long> searchExtendedFriendsIdsSet;
            if(extendedFriends.isEmpty()) {
                searchExtendedFriendsIdsSet = new HashSet<>();
                searchExtendedFriendsIdsSet.add(-1L);
            } else
                searchExtendedFriendsIdsSet = extendedFriends;

            TypedQuery<Order> query = em.createNamedQuery("Order.getNearestOrdersFiltered", Order.class);
            query.setParameter("neLatitude", neLatitude);
            query.setParameter("neLongitude", neLongitude);
            query.setParameter("swLatitude", swLatitude);
            query.setParameter("swLongitude", swLongitude);
            query.setParameter("userExtendedFriendsIds", searchExtendedFriendsIdsSet);
            query.setParameter("userFriendsIds", searchFriendsIdsSet);
            query.setParameter("user", user);

            query.setParameter("enumOrderNew", Order.OrderStatus.New);
            query.setParameter("enumOrderAll", Order.OrderNamespace.All);
            query.setParameter("enumOrderFriends", Order.OrderNamespace.Friends);
            query.setParameter("enumOrderFriendsOfFriends", Order.OrderNamespace.FriendsOfFriends);

            return query.getResultList();
        } finally {
            em.close();
        }
    }


    public Order getOrderByIdFiltered(Long userId, Long orderId, Set<Long> friends, Set<Long> extendedFriends) {
        EntityManager em = entityManagerService.createEntityManager();
        try {
            User user = em.find(User.class, userId);

            // If friendsIds or extended friendsIds set is empty add dummy id for correct syntax in query
            Set<Long> searchFriendsSet;
            if(friends.isEmpty()) {
                searchFriendsSet = new HashSet<>();
                searchFriendsSet.add(-1L);
            } else
                searchFriendsSet = friends;

            Set<Long> searchExtendedFriendsSet;
            if(extendedFriends.isEmpty()) {
                searchExtendedFriendsSet = new HashSet<>();
                searchExtendedFriendsSet.add(-1L);
            } else
                searchExtendedFriendsSet = extendedFriends;

            TypedQuery<Order> query = em.createNamedQuery("Order.getOrderByIdFiltered", Order.class);
            query.setParameter("orderId", orderId);
            query.setParameter("user", user);
            query.setParameter("friends", searchFriendsSet);
            query.setParameter("extendedFriends", searchExtendedFriendsSet);

            query.setParameter("enumOrderAll", Order.OrderNamespace.All);
            query.setParameter("enumOrderFriends", Order.OrderNamespace.Friends);
            query.setParameter("enumOrderFriendsOfFriends", Order.OrderNamespace.FriendsOfFriends);

            List<Order> resultList = query.getResultList();
            if(resultList.size() == 1)
                return resultList.get(0);
            if(resultList.size() == 0)
                return null;

            throw new NonUniqueResultException("We receive more then one user with specified id, it mustn't happened");
        } finally {
            em.close();
        }
    }

    public void saveOrUpdate(Order order) {
        EntityManager em = entityManagerService.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(order);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void save(Order order) {
        EntityManager em = entityManagerService.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(order);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void getRelatedFeedback(Order order, User who, User whom) {

    }

    /**
     * Function check order on is specified user already rate other member of order.
     *
     * @param order order which should be rated.
     * @param user user who should rate.
     * @return true if rated.
     */
    public boolean isOrderRatedBy(Order order, User user) {
        EntityManager em = entityManagerService.createEntityManager();
        try {
            TypedQuery<Feedback> query = em.createNamedQuery("Feedback.getFeedback", Feedback.class);
            query.setParameter("whichOrder", order);
            query.setParameter("whoRate", user);

            return query.getResultList().size() == 1;
        } finally {
            em.close();
        }
    }

    public List<Order> getOrdersForSocialLinkWithOwner(Long executorId, Set<Long> ordersIds,
                                                       Set<Long> friendsIds, Set<Long> extendedFriendsIds) {
        EntityManager em = entityManagerService.createEntityManager();
        try {
            TypedQuery<Order> query = em.createNamedQuery("Order.getOrdersByIdsForLinkWithOwner", Order.class);
            query.setParameter("ordersIds", ordersIds);
            query.setParameter("friendsIds", friendsIds);
            query.setParameter("extendedFriendsIds", extendedFriendsIds);

            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Order> getRunningByUser(Long userId, Set<Order.OrderNamespace> namespaces) {
        EntityManager em = entityManagerService.createEntityManager();
        try {
            TypedQuery<Order> query = em.createNamedQuery("Order.getRunningByUser", Order.class);
            query.setParameter("executorId", userId);
            query.setParameter("namespaces", namespaces);
            query.setParameter("runningStatus", Order.OrderStatus.Running);

            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Order> getCreatedByUser(Long userId, Set<Order.OrderNamespace> namespaces,
                                        int firstResult, int maxResults) {
        EntityManager em = entityManagerService.createEntityManager();
        try {
            TypedQuery<Order> query = em.createNamedQuery("Order.getCreatedByUser", Order.class);
            query.setParameter("ownerId", userId);
            query.setParameter("namespaces", namespaces);
            query.setFirstResult(firstResult);
            query.setMaxResults(maxResults);

            return query.getResultList();
        } finally {
            em.close();
        }
    }
}
