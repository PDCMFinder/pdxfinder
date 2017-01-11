package org.pdxi.dao;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

/**
 * Tissue
 */
@NodeEntity
public class Tissue {

    @GraphId
    private Long id;

    @Property
    private String NODE_TYPE = "TISSUE";

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
