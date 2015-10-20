package net.ogify.engine.friends;

import com.google.common.cache.CacheLoader;
import net.ogify.database.entities.SocialToken;
import net.ogify.database.entities.User;
import net.ogify.engine.exceptions.SocialNetworkTokenMissedException;
import net.ogify.engine.vkapi.VkFriends;
import net.ogify.engine.vkapi.exceptions.VkSideError;

import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Created by melge on 27.09.2015.
 */
public class VkFriendsCacheLoader extends CacheLoader<User, Set<Long>> {
    private FriendService friendService;

    public VkFriendsCacheLoader(FriendService friendService) {
        this.friendService = friendService;
    }

    @Override
    public Set<Long> load(User user) throws VkSideError, ExecutionException {
        SocialToken token = friendService.getSocialToken(user);
        if(token == null)
            throw new ExecutionException(new SocialNetworkTokenMissedException("Can't get social token for user"));

        return VkFriends.getFriends(user.getVkId(), token.getToken());
    }
}