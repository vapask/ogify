package net.ogify.engine.vkapi;

import com.google.common.collect.ImmutableMap;
import net.ogify.engine.vkapi.elements.wall.WallGetResponse;
import net.ogify.engine.vkapi.elements.wall.WallPost;
import net.ogify.engine.vkapi.exceptions.VkSideError;

import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by melge on 27.09.2015.
 */
public class VkWall {
    public static Set<WallPost> getPosts(Long ownerId, String accessToken) throws VkSideError {
        final String methodName = "wall.get";
        final Map<String, Object> parametersMap = ImmutableMap.<String, Object>of(
                "owner_id", ownerId,
                "filter", "owner",
                "access_token", accessToken);

        WallGetResponse response = VkClient.call(VkClient.VK_API_URI + methodName, parametersMap,
                WallGetResponse.class);

        return response.getPostSet().stream().filter(new Predicate<WallPost>() {
            @Override
            public boolean test(WallPost wallPost) {
                return !wallPost.getText().isEmpty();
            }
        }).collect(Collectors.<WallPost>toSet());
    }
}
