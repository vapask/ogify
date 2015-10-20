package net.ogify.engine.secure;

import net.ogify.database.UserController;
import net.ogify.database.entities.SocialNetwork;
import net.ogify.database.entities.User;
import net.ogify.engine.vkapi.VkAuth;
import net.ogify.engine.vkapi.VkUsers;
import net.ogify.engine.vkapi.elements.VkAccessResponse;
import net.ogify.engine.vkapi.elements.VkUserInfo;
import net.ogify.engine.vkapi.exceptions.VkSideError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class contain methods for work with user authentication.
 */
@Component
public class ProductionAuthController implements AuthController {
    @Autowired
    VkAuth vkAuth;

    @Autowired
    UserController userController;

    /**
     * Method check data provided by client for correctness and session validity.
     * @param userId vkId of user, provided by client.
     * @param sessionSecret session id provided by client (session secret).
     * @return true if clint give correct data, or false if client is liar or his session expired.
     */
    public boolean isSessionCorrect(Long userId, String sessionSecret) {
        return userController.getUserByIdAndSession(userId, sessionSecret) != null;
    }

    /**
     * Method authorize client by provided data using vk oauth protocol. If user client authenticate at first time,
     * new user will be created. In other cases we find who is authenticate and create new session.
     *
     * @param code code provided by client (client must request it from vk first).
     * @param redirectUrl redirect uri which client provide to vk client and which client call for invoke auth.
     * @param sessionSecret client session secret.
     * @param betaKey special key used for beta programs.
     * @return id of authenticated user.
     * @throws VkSideError if vk say about error, or we have a trouble when connecting to vk.
     */
    public Long auth(String code, String redirectUrl, String sessionSecret, String betaKey)
            throws VkSideError {
        return authVk(code, redirectUrl, sessionSecret);
    }

    public Long authVk(String code, String redirectUrl, String sessionSecret) throws VkSideError {
        VkAccessResponse response = vkAuth.auth(code, redirectUrl);
        User user = userController.getUserByVkId(response.getUserId());
        if(user == null) {
            VkUserInfo vkUserInfo = VkUsers.get(response.getUserId(), response.getAccessToken());
            user = new User(vkUserInfo.getFullName(), vkUserInfo.getPhotoUri());
            user.setVkId(vkUserInfo.getId());
        }

        user.addSession(sessionSecret, response.getExpiresIn());
        user.addAuthToken(response.getAccessToken(), SocialNetwork.Vk, response.getExpiresIn());

        userController.saveOrUpdate(user);

        return user.getId();
    }
}
