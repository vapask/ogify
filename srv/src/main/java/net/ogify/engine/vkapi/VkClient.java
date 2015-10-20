package net.ogify.engine.vkapi;

import net.ogify.engine.vkapi.elements.VkErrorResponse;
import net.ogify.engine.vkapi.exceptions.VkSideError;
import org.apache.log4j.Logger;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.jackson.JacksonFeature;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * Created by melges on 13.01.15.
 */
public class VkClient {
    private final static Logger logger = Logger.getLogger(VkClient.class);

    public final static String VK_API_URI = "https://api.vk.com/method/";

    public final static String VK_ACCESS_URI = "https://oauth.vk.com/access_token";

    public static <T> T call(String targetUri, Map<String, Object> parameters, Class<T> entityClass) throws VkSideError {
        Client client = ClientBuilder.newBuilder().register(JacksonFeature.class).build();

        // Decrease timeout to avoid slowing vk queries
        client.property(ClientProperties.CONNECT_TIMEOUT, 5000);
        client.property(ClientProperties.READ_TIMEOUT, 3000);

        Response response;
        try {
            WebTarget target = client.target(targetUri);
            for(Map.Entry<String, Object> entry : parameters.entrySet())
                target = target.queryParam(entry.getKey(), entry.getValue());
            target = target.queryParam("v", "5.37");

            // TODO: Rewrite this part, add reconnecting
            response = target.request(MediaType.APPLICATION_JSON).get();
        } catch (RuntimeException ignore) {
            logger.warn(String.format("Can't processing with vk servers: %s", ignore.getLocalizedMessage()));
            throw new VkSideError(ignore.getLocalizedMessage());
        }

        if(response.getStatus() != Response.Status.OK.getStatusCode()) {
            try {
                VkErrorResponse error = response.readEntity(VkErrorResponse.class);
                logger.warn(String.format("Response from vk not equals 200, it is: %d. Error description from vk: %s",
                        response.getStatus(), error.getErrorDescription()));

                throw new VkSideError(error.getErrorDescription(), response.getStatus());
            } catch (RuntimeException ignored) {
                logger.warn(String.format("Vk return error without description with code: %d. Exception: %s",
                        response.getStatus(), ignored.getLocalizedMessage()));
                ignored.printStackTrace();
                throw new VkSideError("Vk return error without description", response.getStatus());
            }
        }

        try {
            return response.readEntity(entityClass);
        } catch (RuntimeException e) {
            logger.warn(String.format("Vk return http ok code, but data in response is incorrect. Exception: %s",
                    e.getLocalizedMessage()));
            throw new VkSideError("Vk return http ok code, but data in response is incorrect", e,
                    response.getStatus());
        }
    }
}
