package net.ogify.engine.exceptions;

import net.ogify.engine.vkapi.exceptions.VkSideError;

/**
 * Created by melge on 27.09.2015.
 */
public class LoadWallPostsException extends VkSideError {
    public LoadWallPostsException(String s) {
        super(s);
    }

    public LoadWallPostsException(String s, Throwable cause) {
        super(s, cause);
    }
}
