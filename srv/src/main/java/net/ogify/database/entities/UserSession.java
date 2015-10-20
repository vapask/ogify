package net.ogify.database.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

/**
 * Created by melges.morgen on 14.02.15.
 */
@Entity
@Table(name = "users_sessions", uniqueConstraints = {
        @UniqueConstraint(name = "user_and_session", columnNames = {"owner", "session_secret"})})
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE) // Deny session transfer to client
@JsonIgnoreProperties
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    Long id;

    @Column(name = "session_secret", nullable = false, unique = true, length = 32, updatable = false)
    private String sessionSecret;

    @Temporal(TemporalType.TIMESTAMP)
    Date expireIn;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "owner", nullable = false, unique = false, updatable = false)
    User owner;

    public UserSession() {
    }

    public UserSession(String sessionSecret, Long expireIn, User owner) {
        this.sessionSecret = sessionSecret;
        this.setExpireIn(expireIn);
        this.owner = owner;
    }

    public void setExpireIn(Long expireIn) {
        if (expireIn <= 0)
            expireIn = 2629743L * 1000L;
        this.expireIn = new Date(System.currentTimeMillis() + expireIn);
    }

    public Long getId() {
        return id;
    }

    public String getSessionSecret() {
        return sessionSecret;
    }

    public Date getExpireIn() {
        return expireIn;
    }

    public User getOwner() {
        return owner;
    }
}
