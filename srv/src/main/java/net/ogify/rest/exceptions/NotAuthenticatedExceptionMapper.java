package net.ogify.rest.exceptions;


import net.ogify.engine.secure.exceptions.NotAuthenticatedException;
import net.ogify.rest.elements.ErrorResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Created by melges on 18.12.14.
 */
@Provider
public class NotAuthenticatedExceptionMapper implements ExceptionMapper<NotAuthenticatedException> {
    @Override
    public Response toResponse(NotAuthenticatedException exception) {
        ErrorResponse response =
                new ErrorResponse(exception.getClass().getSimpleName(), exception.getLocalizedMessage());
        return Response.status(Response.Status.UNAUTHORIZED).entity(response).build();
    }
}
