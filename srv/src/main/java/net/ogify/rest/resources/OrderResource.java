package net.ogify.rest.resources;

import net.ogify.database.OrderController;
import net.ogify.database.entities.Order;
import net.ogify.database.entities.OrderItem;
import net.ogify.engine.order.OrderProcessor;
import net.ogify.engine.secure.AuthController;
import net.ogify.rest.elements.RateRequest;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Class represents
 */
@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Component
public class OrderResource {
    /**
     * Field storing the vk id of the user if the user not authorized it is null.
     * Can be null only in permitted for all methods.
     */
    @CookieParam(value = AuthController.USER_ID_COOKIE_NAME)
    private Long userId;

    @Autowired
    OrderController orderController;

    @Autowired
    OrderProcessor orderProcessor;

    @GET
    @Path("/near")
    public List<Order> getOrdersNear(@NotNull @QueryParam("neLatitude") Double neLatitude,
                                     @NotNull @QueryParam("neLongitude") Double neLongitude,
                                     @NotNull @QueryParam("swLatitude") Double swLatitude,
                                     @NotNull @QueryParam("swLongitude") Double swLongitude)
            throws ExecutionException {
        return orderProcessor.getNearestOrders(neLatitude, neLongitude, swLatitude, swLongitude, userId);
    }

    /**
     * Return, not more then specified, users orders, where user is owner or executor.
     *
     * @param firstResult the position of the first result to retrieve.
     * @param maxResults the maximum number of results to retrieve.
     * @return found users orders.
     */
    @GET
    public List<Order> getMyOrders(@QueryParam("offset") int firstResult,
                                   @QueryParam("firstResult") int maxResults) {
        return orderProcessor.getUsersOrders(userId, firstResult, maxResults);
    }

    @GET
    @Path(("/{id}"))
    public Order getOrder(@NotNull @PathParam("id") Long orderId) {
        // TODO: Rewrite using processor
        return orderController.getOrderById(orderId);
    }

    @GET
    @Path("/{id}/items")
    public List<OrderItem> getOrderItems(@NotNull @PathParam("id") Long orderId) {
        // TODO: Rewrite using processor
        return orderController.getOrderById(orderId).getItems();
    }

    @PUT
    @Path("/{id}/getToExecution")
    public void getToExecution(@PathParam("id") Long orderId) {
        orderProcessor.changeOrderExecutor(userId, orderId);
        orderProcessor.changeOrderStatus(userId, orderId, Order.OrderStatus.Running);
    }

    @DELETE
    @Path("/{id}/executor")
    public void denyOrderExecution(@PathParam("id") Long orderId) {
        orderProcessor.denyOrderExecution(userId, orderId);
    }

    @GET
    @Path("/{id}/socialLink")
    public Order.OrderNamespace getSocialLink(@NotNull @PathParam("id") Long orderId) throws ExecutionException {
        return orderProcessor.getOrderConnectionWithUser(orderId, userId);
    }

    /**
     * Calculate social link with orders.
     *
     * @param ordersIds list of orders, for them would be provided information about social link.
     * @return map of orders and theirs link with user.
     * @throws ExecutionException in case of some errors on calculation.
     */
    @GET
    @Path("/socialLinks")
    public Map<Long, Order.OrderNamespace> getSocialLinks(@NotEmpty @QueryParam("ordersIds") Set<Long> ordersIds)
            throws ExecutionException {
        return orderProcessor.getOrdersConnectionsWithUser(ordersIds, userId);
    }

    /**
     * Create new order.
     *
     * @param order order body.
     * @return created order.
     */
    @POST
    public Order createNewOrder(Order order) {
        return orderProcessor.createOrder(userId, order);
    }

    /**
     * Change order status on specified in request.
     *
     * @param orderId id of order to change.
     * @param status status which should be set on order.
     */
    @PUT
    @Path("/{id}/status")
    public void completeOrder(@PathParam("id") Long orderId,
                              @NotNull Order.OrderStatus status) {
        orderProcessor.changeOrderStatus(userId, orderId, status);
    }

    /**
     * Rate other side of order.
     *
     * @param orderId id of order related to rate.
     * @param rateRequest object contains rate details.
     */
    @PUT
    @Path("/{id}/rate")
    public void rateOrder(@PathParam("id") Long orderId, @NotNull RateRequest rateRequest) {
        orderProcessor.rateOrderParty(userId, orderId, rateRequest.getRate(), rateRequest.getComment());
    }
}
