package net.ogify.engine.secure;

import net.ogify.database.UserController;
import net.ogify.database.entities.SocialNetwork;
import net.ogify.database.entities.User;
import net.ogify.engine.vkapi.exceptions.VkSideError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by melge on 20.10.2015.
 */
@Component
public class LocalAuthController implements AuthController {
    @Autowired
    UserController userController;

    AtomicLong previousId = new AtomicLong(0l);

    @Override
    public boolean isSessionCorrect(Long userId, String sessionSecret) {
        return userController.getUserById(userId) != null;
    }

    @Override
    public Long auth(String code, String redirectUrl, String sessionSecret, String betaKey) throws VkSideError {
        User user = new User("Test User", "http://cs629231.vk.me/v629231001/c541/TaUV7CG7RHg.jpg");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 1);
        user.setVkId(previousId.addAndGet(1));

        user.addSession(sessionSecret, calendar.getTimeInMillis());
        user.addAuthToken("DUMMY", SocialNetwork.Vk, calendar.getTimeInMillis());

        userController.saveOrUpdate(user);
        return 1L;
    }
}
