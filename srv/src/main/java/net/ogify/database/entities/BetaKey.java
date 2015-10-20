package net.ogify.database.entities;

import javax.persistence.*;

/**
 * Created by melge on 18.10.2015.
 */
@Entity
@Table(name = "beta_keys")
@NamedQueries(
        @NamedQuery(name = "BetaKey.getByKey", query = "select betaKey from BetaKey betaKey " +
                "where betaKey.betaKey = :betaKey")
)
public class BetaKey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "beta_key", nullable = false, unique = true, updatable = false)
    private String betaKey;

    @Column(name = "use_time", nullable = false, updatable = true)
    private Integer usedTime;

    public BetaKey() {
        betaKey = null;
        usedTime = 0;
    }

    public BetaKey(String betaKey) {
        this.betaKey = betaKey;
        usedTime = 0;
    }

    public Long getId() {
        return id;
    }

    public String getBetaKey() {
        return betaKey;
    }

    public Integer getUsedTime() {
        return usedTime;
    }

    public void incrementUsedTime() {
        usedTime++;
    }
}
