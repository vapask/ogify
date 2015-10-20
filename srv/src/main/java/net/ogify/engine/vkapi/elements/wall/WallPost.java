package net.ogify.engine.vkapi.elements.wall;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

/**
 * Class represents post on users wall.
 */
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class WallPost {
    /**
     * Post id
     */
    @XmlElement(name = "id")
    Long id;

    /**
     * Id of user who is owner of wall where post is placed.
     */
    @XmlElement(name = "owner_id")
    Long ownerId;

    /**
     * Id of user who write this post
     */
    @XmlElement(name = "from_id")
    Long fromId;

    /**
     * Date when post was posted
     */
    @XmlElement(name = "date")
    Date date;

    /**
     * Text which contain post
     */
    @XmlElement(name = "text")
    String text;

    public Long getId() {
        return id;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public Long getFromId() {
        return fromId;
    }

    public Date getDate() {
        return date;
    }

    public String getText() {
        return text;
    }
}
