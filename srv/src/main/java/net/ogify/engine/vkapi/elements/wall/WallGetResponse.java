package net.ogify.engine.vkapi.elements.wall;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.ImmutableSet;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Set;

/**
 * Class represents response on
 */
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class WallGetResponse {
    public static class ResponseBody {
        @XmlElement(name = "count", required = true)
        int count;

        @XmlElement(name = "items", required = true)
        Set<WallPost> postSet;
    }

    @NotNull
    @XmlElement(name = "response", required = true)
    private ResponseBody response;

    public int getCount() {
        return response.count;
    }

    public Set<WallPost> getPostSet() {
        if(response == null)
            return ImmutableSet.of();
        return response.postSet;
    }
}
