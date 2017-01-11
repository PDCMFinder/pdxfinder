package org.pdxi.dao;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

/**
 * Tumor type represents the various types of tumors
 */
@NodeEntity
public class TumorType {

    @GraphId
    private Long id;
    @Property(name = "TUMOR_TYPE")
    private String NODE_TYPE;

    private String name;

    private TumorType() {
    }

    public TumorType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
