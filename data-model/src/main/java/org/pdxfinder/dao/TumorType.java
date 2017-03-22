package org.pdxfinder.dao;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * Sample type represents the various types of tumors
 */
@NodeEntity
public class TumorType {

    @GraphId
    private Long id;

    private String name;

    private TumorType() {
        // Empty constructor required as of Neo4j API 2.0.5
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
