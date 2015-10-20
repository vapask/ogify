package net.ogify.database.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.Date;

/**
 * Class which represents social_tokens table - table of auth tokens in social networks.
 */
@Entity
@Table(name = "social_tokens")
@NamedQueries({
        @NamedQuery(name = "SocialToken.getUsersTokens", query = "select token from SocialToken token where " +
                "token.owner = :owner and token.tokensSocialNetwork = :socialNetwork " +
                "and token.expireIn > CURRENT_TIMESTAMP"),
        @NamedQuery(name = "SocialToken.getNewestUsersToken", query = "select token from SocialToken token where " +
                "token.owner = :owner and token.tokensSocialNetwork = :socialNetwork " +
                "and token.expireIn > CURRENT_TIMESTAMP order by token.tokenId desc")
})
@XmlAccessorType(XmlAccessType.NONE)
@JsonIgnoreProperties
public class SocialToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    Long tokenId;

    @Column(name = "token_sn", nullable = false)
    SocialNetwork tokensSocialNetwork;

    @Column(name = "auth_token")
    String token;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "expireIn")
    Date expireIn;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "owner", nullable = false)
    User owner;

    public SocialToken() {
    }

    public SocialToken(String token, Long expireIn, SocialNetwork socialNetwork, User owner) {
        this.token = token;
        this.tokensSocialNetwork = socialNetwork;
        this.setExpireIn(expireIn);
        this.owner = owner;
    }

    public void setExpireIn(Long expireIn) {
        if (expireIn <= 0)
            expireIn = 2629743L * 1000L;
        this.expireIn = new Date(System.currentTimeMillis() + expireIn);
    }

    public Date getExpireIn() {
        return expireIn;
    }

    public String getToken() {
        return token;
    }

    public SocialNetwork getTokensSocialNetwork() {
        return tokensSocialNetwork;
    }

    public Long getTokenId() {
        return tokenId;
    }
}
