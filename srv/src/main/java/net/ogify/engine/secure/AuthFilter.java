package net.ogify.engine.secure;

import net.ogify.engine.secure.exceptions.ForbiddenException;
import net.ogify.engine.secure.exceptions.NotAuthenticatedException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Filter class, jersey invoke filter method of this class before invoke any
 * resource methods.
 *
 * Class must check is the client which is try to call resource method has a right for that.
 */
@Provider
@Component
public class AuthFilter implements ContainerRequestFilter {
    /**
     * Class logger.
     */
    private final static Logger logger = Logger.getLogger(AuthFilter.class);

    /**
     * Field is used for get information from context about invoked web resource.
     */
    @Context
    private ResourceInfo resourceInfo;

    @Autowired
    AuthController authController;

    /**
     * Check is client has right to invoke requested method. If client haven't right invoke will be denied and
     * exception will throw.
     *
     * @param requestContext context for get information about client and requested method and other.
     * @throws ForbiddenException on illegal access to resource.
     * @throws NotAuthenticatedException when unauthenticated user tries to get resource which wasn't annotated with
     * PermitAll.
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws ForbiddenException, NotAuthenticatedException {
        // Get method which will be invoked by jersey if we don't deny it
        Method resourceMethod = resourceInfo.getResourceMethod();

        if (resourceMethod.isAnnotationPresent(PermitAll.class))
            return; // Simply return and continue work if resource available for all

        if (resourceMethod.isAnnotationPresent(DenyAll.class))
            throw new ForbiddenException("Forbidden. Denied for all.");

        Map<String, Cookie> cookieMap = requestContext.getCookies();
        Cookie vkIdCookie = cookieMap.get(AuthController.USER_ID_COOKIE_NAME);
        if (vkIdCookie == null)
            throw new NotAuthenticatedException("No cookie: " + AuthController.USER_ID_COOKIE_NAME);
        Long vkId;
        try {
            vkId = Long.parseLong(vkIdCookie.getValue());
        } catch (NumberFormatException exception) {
            throw new NotAuthenticatedException("Invalid cookie: " + AuthController.USER_ID_COOKIE_NAME);
        }

        Cookie sessionCookie = cookieMap.get(AuthController.SESSION_COOKIE_NAME);
        if (sessionCookie == null)
            throw new NotAuthenticatedException("No session in cookie.");
        String sessionSecret = sessionCookie.getValue();

        if (resourceMethod.isAnnotationPresent(RolesAllowed.class)) {
            RolesAllowed rolesAllowed = resourceMethod.getAnnotation(RolesAllowed.class);
            String[] allowedRoles = rolesAllowed.value();

            // TODO: Add user group support

            return;
        }

        if (!authController.isSessionCorrect(vkId, sessionSecret))
            throw new NotAuthenticatedException("Session incorrect or expired");

    }
}