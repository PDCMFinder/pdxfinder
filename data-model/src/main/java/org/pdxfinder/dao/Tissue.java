package org.pdxfinder.dao;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * Tissue represents a human tissue associated with a tumor
 */
@NodeEntity
public class Tissue {

    @GraphId
    private Long id;

    private String name;

    public Tissue() {
        // Empty constructor required as of Neo4j API 2.0.5
    }

    public Tissue(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
