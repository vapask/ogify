package net.ogify.engine.secure;

import net.ogify.engine.vkapi.exceptions.VkSideError;

import javax.xml.bind.DatatypeConverter;
import java.security.SecureRandom;
import java.util.Random;

/**
 * Created by melge on 20.10.2015.
 */
public interface AuthController {
    String SESSION_COOKIE_NAME = "ogifySessionSecret";

    String USER_ID_COOKIE_NAME = "sId";

    /**
     * Method generate secure random number which should be used as session secret
     *
     * @return random generated string
     */
    static String generateSessionSecret() {
        Random randomGenerator = new SecureRandom();
        byte[] sessionSecretAsBytes = new byte[16];
        randomGenerator.nextBytes(sessionSecretAsBytes);

        return DatatypeConverter.printHexBinary(sessionSecretAsBytes);
    }

    /**
     * Method check data provided by client for correctness and session validity.
     *
     * @param userId        vkId of user, provided by client.
     * @param sessionSecret session id provided by client (session secret).
     * @return true if clint give correct data, or false if client is liar or his session expired.
     */
    boolean isSessionCorrect(Long userId, String sessionSecret);

    /**
     * Method authorize client by provided data using vk oauth protocol. If user client authenticate at first time,
     * new user will be created. In other cases we find who is authenticate and create new session.
     *
     * @param code          code provided by client (client must request it from vk first).
     * @param redirectUrl   redirect uri which client provide to vk client and which client call for invoke auth.
     * @param sessionSecret client session secret.
     * @param betaKey       special key used for beta programs.
     * @return vk id returned from
     * @throws VkSideError if vk say about error, or we have a trouble when connecting to vk.
     */
    Long auth(String code, String redirectUrl, String sessionSecret, String betaKey)
            throws VkSideError;

}
