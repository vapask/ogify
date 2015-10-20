package net.ogify.engine.vkapi.elements;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Set;

/**
 * Created by melges on 27.02.2015.
 */
@XmlRootElement
public class VkFriendsGetResponse {
    public static class VkFriendsResponse {
        private Integer count;

        private Set<Long> items;

        public VkFriendsResponse() {
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        public Set<Long> getItems() {
            return items;
        }

        public void setItems(Set<Long> items) {
            this.items = items;
        }
    }

    /**
     * Array of profiles
     */
    @XmlElement(name = "response", required = true, nillable = false)
    VkFriendsResponse response;

    public Integer getCount() {
        return response.getCount();
    }

    public Set<Long> getItems() {
        return response.getItems();
    }

}
