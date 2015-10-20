package net.ogify.engine.vkapi;

import net.ogify.engine.vkapi.elements.VkAccessResponse;
import net.ogify.engine.vkapi.elements.VkAuthUri;
import net.ogify.engine.vkapi.exceptions.VkSideError;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by melges on 16.12.14.
 */
@Service
@PropertySource("classpath:ogify.properties")
public class VkAuth {
    private final static String VK_CLIENT_ACCESS_URI = "https://oauth.vk.com/authorize";
    private final static String VK_API_VERSION = "5.37";

    @Value("${vk.app.id}")
    private long APP_ID;
    @Value("${vk.app.password}")
    private String APP_SECRET;

    private final static Logger logger = Logger.getLogger(VkAuth.class);

    public VkAuthUri getClientAuthUri(URI redirectUri, String betaKey) {
        String scope = "notify,offline,friends";
        return new VkAuthUri(String.format("%s?client_id=%s&scope=%s&redirect_uri=%s&response_type=code&v=%s&state=%s",
                VK_CLIENT_ACCESS_URI, APP_ID, scope, redirectUri.toString(), VK_API_VERSION, betaKey));
    }

    /**
     * Get user information for provided code from vk. Associate vk user with out user, create session and return
     * session secret string, which should be sended to client.
     * @param code special string from client provided by vk.
     * @param redirectUri uri on which user will be redirected back by vk after auth procedure.
     * @return parsed to object vk response.
     * @throws VkSideError on any errors with communicating with vk.
     */
    public VkAccessResponse auth(String code, String redirectUri) throws VkSideError {
        final Map<String, Object> parametersMap = new HashMap<String, Object>();
        parametersMap.put("client_id", APP_ID);
        parametersMap.put("client_secret", APP_SECRET);
        parametersMap.put("code", code);
        parametersMap.put("redirect_uri", redirectUri);

        return VkClient.call(VkClient.VK_ACCESS_URI, parametersMap, VkAccessResponse.class);
    }
}
