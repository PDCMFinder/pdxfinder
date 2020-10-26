package org.pdxfinder.graph.dao;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * Created by jmason on 06/06/2017.
 */
@NodeEntity
public class Image {

    @Id @GeneratedValue
    Long id;

    private String url;
    private String description;
    private String type;
    

    public Image(String url) {
        this.url = url;
    }

    public Image() {

    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
