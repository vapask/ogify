package net.ogify.engine.walls;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.ogify.database.UserController;
import net.ogify.database.entities.User;
import net.ogify.engine.exceptions.LoadWallPostsException;
import net.ogify.engine.friends.FriendService;
import net.ogify.engine.vkapi.VkWall;
import net.ogify.engine.vkapi.elements.wall.WallPost;
import net.ogify.engine.vkapi.exceptions.VkSideError;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Service for retrieving wall posts from vk
 */
@Service
public class WallPostsService {
    private final static Logger logger = Logger.getLogger(WallPostsService.class);

    private final static long cacheSize = 10000;

    private LoadingCache<GetPostsFromWallRequest, Set<WallPost>> wallPostCache;

    @Autowired
    UserController userController;

    @Autowired
    FriendService friendService;

    public WallPostsService() {
        wallPostCache = CacheBuilder.newBuilder()
                .maximumSize(cacheSize)
                .expireAfterWrite(1, TimeUnit.DAYS)
                .build(new CacheLoader<GetPostsFromWallRequest, Set<WallPost>>() {
                    @Override
                    public Set<WallPost> load(GetPostsFromWallRequest request) throws Exception {
                        return getWallPostsOfVkUser(request);
                    }
                });
    }

    public Set<WallPost> getWallPostsOfVkUser(GetPostsFromWallRequest request) throws VkSideError {
        return VkWall.getPosts(request.getTargetVkId(), request.getRequestOwnerToken());
    }

    public Set<WallPost> getWallPostsOfUser(User user) throws LoadWallPostsException, ExecutionException {
        return wallPostCache.get(new GetPostsFromWallRequest(user.getVkToken().getToken(), user.getVkId()));
    }

    public Set<WallPost> getWallPostsOfUserFriends(User user) {
        Set<Long> friendsVkIds = friendService.getUserVkFriends(user);
        Set<WallPost> resultSet = new HashSet<>();

        for(Long friendVkId : friendsVkIds) {
            try {
                resultSet.addAll(wallPostCache.get(new GetPostsFromWallRequest(user.getVkToken().getToken(), friendVkId)));
            } catch (ExecutionException e) {
                logger.warn(String.format("Can't load wall for user with id %d", friendVkId), e);
            }
        }

        return resultSet;
    }

    public Set<WallPost> getWallPostsRelatedWithUsers(Set<Long> userIds) throws VkSideError {
        List<User> usersList = userController.getUsersWithIds(userIds);

        Set<WallPost> resultSet = new HashSet<>();
        for(User user : usersList) {
            try {
                resultSet.addAll(getWallPostsOfUser(user));
                Thread.sleep(600); // Reduce number of calls to vk in second
                resultSet.addAll(getWallPostsOfUserFriends(user));
                Thread.sleep(600);
            } catch (ExecutionException e) {
                logger.warn(String.format("Error while loading wall posts related with user %d", user.getId()), e);
            } catch (InterruptedException e) {
                logger.warn("Error while wait for 600ms");
            }
        }

        return resultSet;
    }

    public List<WallPost> getAllPosts(int maxCount) throws VkSideError {
        List<WallPost> resultList = new ArrayList<>(maxCount);
        for(int usersPage = 0; resultList.size() < maxCount; usersPage++) {
            List <Long> usersList = userController.getAllUsersIds(200, usersPage);
            if(usersList.isEmpty())
                break;

            resultList.addAll(getWallPostsRelatedWithUsers(new HashSet<>(usersList)));
        }

        return resultList;
    }

    private class GetPostsFromWallRequest {
        private String requestOwnerToken;

        private Long targetVkId;

        public GetPostsFromWallRequest(String requestOwnerToken, Long targetVkId) {
            this.requestOwnerToken = requestOwnerToken;
            this.targetVkId = targetVkId;
        }

        public String getRequestOwnerToken() {
            return requestOwnerToken;
        }

        public Long getTargetVkId() {
            return targetVkId;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            GetPostsFromWallRequest that = (GetPostsFromWallRequest) o;

            return !(targetVkId != null ? !targetVkId.equals(that.targetVkId) : that.targetVkId != null);
        }

        @Override
        public int hashCode() {
            return targetVkId != null ? targetVkId.hashCode() : 0;
        }
    }
}
