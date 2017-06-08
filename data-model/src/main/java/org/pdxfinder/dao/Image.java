package org.pdxfinder.dao;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * Created by jmason on 06/06/2017.
 */
@NodeEntity
public class Image {

    @GraphId
    Long id;

    private String url;

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
}
