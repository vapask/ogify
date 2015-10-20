package net.ogify.rest.elements;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Object of that class used for transmits rate request from users.
 *
 * @author Morgen Matvey
 */
@XmlRootElement
public class RateRequest {
    @NotNull
    @JsonProperty(required = true)
    private double rate;

    private String comment;

    public RateRequest() {
    }

    public RateRequest(double rate, String comment) {
        this.rate = rate;
        this.comment = comment;
    }

    public double getRate() {
        return rate;
    }

    public String getComment() {
        return comment;
    }
}
