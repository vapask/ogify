package net.ogify.database.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.ogify.database.entities.validation.TelephoneNumber;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by melges.morgen on 14.02.15.
 */
@Entity
@Table(name = "orders")
@NamedQueries({
        @NamedQuery(name = "Order.getNearestOrder", query = "select orders from Order orders " +
                "where orders.latitude > (:latitude - 0.07) and orders.latitude < (:latitude + 0.07) " +
                "and orders.longitude > (:longitude - 0.07) and orders.longitude < (:longitude + 0.07)" +
                "and (orders.expireIn > CURRENT_TIMESTAMP or orders.expireIn is null) " +
                "and orders.status = :enumOrderNew and orders.namespace = :enumOrderAll"),
        @NamedQuery(name = "Order.getUsersOrders", query = "select orders from Order orders where " +
                "orders.owner = :user or orders.executor = :user"),
        @NamedQuery(name = "Order.getUsersOrderById", query = "select orders from Order orders where " +
                "orders.id = :orderId and (orders.owner = :user or orders.executor = :user)"),
        @NamedQuery(name = "Order.getNearestOrdersFiltered", query = "select distinct orders from Order orders " +
                "where orders.id in (" +
                    "select orders.id from Order orders where " +
                    "orders.latitude > :swLatitude and orders.latitude < :neLatitude " +
                    "and orders.longitude > :swLongitude and orders.longitude < :neLongitude " +
                    "and (orders.expireIn > CURRENT_TIMESTAMP or orders.expireIn is null) " +
                    "and orders.status = :enumOrderNew) " +
                "and (" +
                    "orders.namespace = :enumOrderAll " +
                    "or (" +
                        "orders.namespace = :enumOrderFriendsOfFriends and " +
                        "(orders.owner.id in :userExtendedFriendsIds) or (orders.owner.id in :userFriendsIds))" +
                    "or (orders.namespace = :enumOrderFriends and orders.owner.id in :userFriendsIds)" +
                ") " +
                "and orders.owner != :user " +
                "and orders.executor is null " +
                "ORDER BY orders.expireIn"),
        @NamedQuery(name = "Order.getOrderByIdFiltered", query = "SELECT orders FROM Order orders WHERE " +
                    "orders.owner = :user AND orders.id = :orderId " +
                "UNION SELECT orders FROM Order orders, User owners WHERE " +
                    "orders.namespace = :enumOrderFriends and " +
                    "orders.owner in :friends " +
                    "and orders.id = :orderId " +
                "UNION SELECT orders FROM Order orders WHERE " +
                    "orders.namespace = :enumOrderFriendsOfFriends and orders.owner in :extendedFriends " +
                    "and orders.id = :orderId " +
                "UNION SELECT orders FROM Order orders WHERE orders.namespace = :enumOrderAll " +
                    "and orders.id = :orderId " +
                "UNION SELECT orders FROM Order orders WHERE orders.executor = :user"),
        @NamedQuery(name = "Order.getOrdersByIdsForLinkWithOwner", query =
                "SELECT orders FROM Order orders, User owners WHERE " +
                    "orders.owner.id in :friendsIds and " +
                    "orders.id in :ordersIds " +
                "UNION SELECT orders FROM Order orders WHERE " +
                    "orders.owner.id in :extendedFriendsIds and " +
                    "orders.id in :ordersIds " +
                "UNION SELECT orders FROM Order orders WHERE " +
                    "orders.id in :ordersIds and " +
                    "orders.owner.id not in :friendsIds and " +
                    "orders.owner.id not in :extendedFriendsIds"),
        @NamedQuery(name = "Order.getRunningByUser", query =
                "SELECT orders FROM Order orders WHERE " +
                    "orders.executor.id = :executorId and " +
                    "orders.namespace in :namespaces and " +
                    "orders.status = :runningStatus ORDER BY orders.expireIn DESC"),
        @NamedQuery(name = "Order.getCreatedByUser", query =
                "SELECT orders FROM Order orders WHERE " +
                    "orders.owner.id = :ownerId and " +
                    "orders.namespace in :namespaces ORDER BY orders.expireIn DESC")
})
@XmlRootElement
public class Order {
    @XmlType(name = "order_status")
    @XmlEnum
    public enum OrderStatus {
        @XmlEnumValue("new") New,
        @XmlEnumValue("running") Running,
        @XmlEnumValue("completed") Completed,
        @XmlEnumValue("canceled") Canceled
    }

    @XmlType(name = "order_namespace")
    @XmlEnum
    public enum OrderNamespace {
        @XmlEnumValue("private") Private,
        @XmlEnumValue("friends") Friends,
        @XmlEnumValue("friends-of-friends") FriendsOfFriends,
        @XmlEnumValue("all") All,
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    @XmlElement(nillable = true, required = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_owner", nullable = false)
    @XmlElement(nillable = false, required = true)
    private User owner;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_executor", nullable = true)
    @XmlElement(nillable = true, required = true)
    private User executor;

    @NotNull
    @Column(name = "order_status", nullable = false)
    @Enumerated(EnumType.STRING)
    @XmlElement(nillable = false, required = true)
    private OrderStatus status;

    @NotNull
    @Column(name = "order_namespace", nullable = false)
    @Enumerated(EnumType.STRING)
    @XmlElement(nillable = false, required = true)
    private OrderNamespace namespace;

    @NotNull
    @Column(name = "latitude", nullable = false)
    @XmlElement(required = true, nillable = false)
    private Double latitude;

    @NotNull
    @Column(name = "longitude", nullable = false)
    @XmlElement(required = true, nillable = false)
    private Double longitude;

    @Column(name = "address")
    @XmlElement(name = "address")
    private String address;

    @Column(name = "reward")
    @XmlElement(name = "reward")
    private String reward;

    @Column(name = "order_description")
    @XmlElement
    private String description;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false)
    @XmlElement
    private Date createdAt = new Date();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "done_at", nullable = true)
    @XmlElement
    private Date doneAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "expire_in")
    @XmlElement
    private Date expireIn;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "executor_get_in")
    @JsonIgnore
    private Date executorGetIn;

    @TelephoneNumber
    private String telephoneNumber;

    @NotNull
    @OneToMany(mappedBy = "order", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @XmlElement(name = "items")
    private List<OrderItem> items = new ArrayList<OrderItem>();

    @OneToMany
    @JsonIgnore
    private List<Feedback> relatedFeedbacks;

    public boolean isUserOwner(User user) {
        return user.equals(owner);
    }

    public boolean isUserExecutor(User user) {
        return user.equals(executor);
    }

    public boolean isInFinalState() {
        return status == OrderStatus.Completed || status == OrderStatus.Canceled;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setExecutor(User executor) {
        this.executor = executor;
    }

    public void makeCreatedNow() {
        createdAt = new Date();
    }

    public void setOwner(User user) {
        this.owner = user;
    }

    public User getOwner() {
        return owner;
    }

    public User getExecutor() {
        return executor;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    @JsonProperty("telephoneNumber")
    public String getTelephoneNumber() {
        if(executorGetIn == null)
            return null;

        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.add(Calendar.MINUTE, -1);
        Calendar executorGetInCalendar = Calendar.getInstance();
        executorGetInCalendar.setTime(executorGetIn);

        if(currentCalendar.before(executorGetInCalendar))
            return null;

        return telephoneNumber;
    }

    @JsonProperty("telephoneNumber")
    public void setTelephoneNumber(String telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }

    public OrderNamespace getNamespace() {
        return namespace;
    }

    public void setNamespace(OrderNamespace namespace) {
        this.namespace = namespace;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getReward() {
        return reward;
    }

    public void setReward(String reward) {
        this.reward = reward;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getDoneAt() {
        return doneAt;
    }

    public void setDoneAt(Date doneAt) {
        this.doneAt = doneAt;
    }

    public Date getExpireIn() {
        return expireIn;
    }

    public void setExpireIn(Date expireIn) {
        this.expireIn = expireIn;
    }

    public Date getExecutorGetIn() {
        return executorGetIn;
    }

    public void setExecutorGetIn(Date executorGetIn) {
        this.executorGetIn = executorGetIn;
    }

    public List<Feedback> getRelatedFeedbacks() {
        return relatedFeedbacks;
    }
}
