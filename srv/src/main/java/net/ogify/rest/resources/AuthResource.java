package net.ogify.rest.resources;

import com.qmino.miredot.annotations.ReturnType;
import net.ogify.engine.secure.AuthController;
import net.ogify.engine.vkapi.VkAuth;
import net.ogify.engine.vkapi.exceptions.VkSideError;
import net.ogify.rest.elements.SNRequestUri;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Main authentication interface
 *
 * @author Morgen Matvey
 */
@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Component
public class AuthResource {
    @Autowired
    VkAuth vkAuth;

    @Autowired
    AuthController authController;

    /**
     * Field is storing the session id of the user if the user has no session it is null.
     *
     * Can be null only in permitted for all methods.
     */
    @CookieParam(value = AuthController.SESSION_COOKIE_NAME)
    private String sessionSecret;

    /**
     * Field is storing the vk id of the user if the user not authorized it is null.
     *
     * Can be null only in permitted for all methods.
     */
    @CookieParam(value = AuthController.USER_ID_COOKIE_NAME)
    private Long userId;

    /**
     * Method for simplify checking auth status from client side, return 200 OK if user is authenticated,
     * or 401 Unauthorized (Error will raised by AuthFilter).
     *
     * @summary Check authentication status of client
     *
     * @return always return empty OK Response.
     */
    @Path("/isAuthenticated")
    @GET
    @ReturnType("java.lang.Void")
    public Response isAuthenticated() {
        return Response.ok().build();
    }

    /**
     * Method return url which must be used for authentication.
     *
     * @summary Generate authenticate url
     *
     * @param uriInfo context parameter with uri.
     * @param betaKey special key provided for beta testing.
     * @return authentication url.
     */
    @GET
    @Path("/getRequestUri")
    @PermitAll
    public SNRequestUri getRequestUri(@Context UriInfo uriInfo,
                                      @QueryParam("betaKey") String betaKey) {
        // TODO: Rewrite using authController
        //authController.checkBetaKey(betaKey);

        URI authRequestUri = uriInfo.getBaseUriBuilder()
                .path(AuthResource.class) // Add class path;
                .build();

        return vkAuth.getClientAuthUri(authRequestUri, betaKey);
    }

    /**
     * Authenticate client redirected from social network. This is endpoint (redirect url should point to this method)
     * for OAuth2 protocol.
     *
     * @summary Authenticate client
     * @param code secret code returned by social network.
     * @param betaKey special secret key used for auth users subscribed on beta program.
     * @param request request from context.
     * @return response which sets cookie and redirect client to client application.
     * @throws VkSideError if there error returned from vk on authentication process.
     * @throws URISyntaxException on error in producing redirect URI
     */
    @GET
    @PermitAll
    @Consumes(MediaType.WILDCARD)
    @ReturnType("java.lang.Void")
    public Response auth(
            @QueryParam("code") String code,
            @QueryParam("state") String betaKey,
            @Context HttpServletRequest request) throws VkSideError, URISyntaxException {
        if(code == null || code.isEmpty())
            return Response.seeOther(new URI("/landing")).build();

        String uri = request.getRequestURL().toString();
        if(sessionSecret == null)
            sessionSecret = AuthController.generateSessionSecret();

        NewCookie snIdCookie = new NewCookie(AuthController.USER_ID_COOKIE_NAME,
                authController.auth(code, uri, sessionSecret, betaKey).toString(), "/", null,
                null, 2629744, false); // Valid for a month
        NewCookie sessionIdCookie = new NewCookie(AuthController.SESSION_COOKIE_NAME, sessionSecret, "/", null,
                null, 2629744, false); // Valid for a month

        return Response.seeOther(new URI("/client"))
                .cookie(snIdCookie)
                .cookie(sessionIdCookie)
                .build();
    }
}
