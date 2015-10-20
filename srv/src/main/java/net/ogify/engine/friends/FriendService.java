package net.ogify.engine.friends;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import net.ogify.database.UserController;
import net.ogify.database.entities.SocialNetwork;
import net.ogify.database.entities.SocialToken;
import net.ogify.database.entities.User;
import net.ogify.engine.exceptions.SocialNetworkTokenMissedException;
import net.ogify.engine.vkapi.exceptions.VkSideError;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by melges on 24.02.2015.
 */
@Service
public class FriendService {
    private final static Logger logger = Logger.getLogger(FriendService.class);

    private final static long cacheSize = 10000;

    protected LoadingCache<Long, Set<Long>> friendsCache;

    protected LoadingCache<Long, Set<Long>> extendedFriendsCache;

    protected LoadingCache<User, Set<Long>> vkFriendsCache;

    @Autowired
    UserController userController;

    public FriendService() {
        friendsCache = CacheBuilder.newBuilder()
                .maximumSize(cacheSize)
                .expireAfterWrite(7, TimeUnit.DAYS)
                .build(new FriendCacheLoader(this));

        extendedFriendsCache = CacheBuilder.newBuilder()
                .maximumSize(cacheSize)
                .expireAfterWrite(7, TimeUnit.DAYS)
                .build(new ExtendedFriendCacheLoader(this));

        vkFriendsCache = CacheBuilder.newBuilder()
                .maximumSize(cacheSize)
                .expireAfterWrite(7, TimeUnit.DAYS)
                .build(new VkFriendsCacheLoader(this));

    }

    SocialToken getSocialToken(User user) {
        return userController.getUserAuthToken(user, SocialNetwork.Vk);
    }

    protected Set<Long> loadFriendList(Long userId)
            throws VkSideError, IllegalArgumentException, SocialNetworkTokenMissedException, ExecutionException {
        User user = userController.getUserById(userId);
        if(user == null)
            throw new IllegalArgumentException(String.format("Invalid user with id: %d", userId));

        Set<Long> vkFriendsIds = vkFriendsCache.get(user);
        List<User> filteredFriends = userController.getUserWithVkIds(vkFriendsIds);
        HashSet<Long> resultSet = new HashSet<>(filteredFriends.size());
        for(User filteredUser : filteredFriends)
            resultSet.add(filteredUser.getId());

        return resultSet;
    }

    protected Set<Long> loadExtendedFriendList(Long userId) throws ExecutionException {
        Set<Long> friends = getUserFriendsIds(userId);

        Set<Long> extendedFriends = new HashSet<>();
        for(Long friendId : friends)
            try {
                extendedFriends.addAll(getUserFriendsIds(friendId));
            } catch (ExecutionException loadException) {
                logger.warn(String.format("Error on loading friends for user with id %d: no valid social tokens",
                        friendId));
            }

        return extendedFriends;
    }

    public Set<Long> getUserFriendsIds(Long userId) throws ExecutionException {
        return friendsCache.get(userId);
    }

    public Set<Long> getUserExtendedFriendsIds(Long userId) throws ExecutionException {
        return extendedFriendsCache.get(userId);
    }

    public Set<Long> getUserVkFriends(User user) {
        try {
            return vkFriendsCache.get(user);
        } catch (ExecutionException e) {
            logger.warn(String.format("Error on retrieving friends from vk for user %d", user.getId()), e);
            return ImmutableSet.of();
        }
    }

    public boolean isUsersFriends(Long userId, Long possibleFriend) {
        Set<Long> usersFriends = friendsCache.getUnchecked(userId);
        return usersFriends != null && usersFriends.contains(possibleFriend);
    }
}
