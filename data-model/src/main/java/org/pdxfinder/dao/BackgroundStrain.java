package org.pdxfinder.dao;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * Represents a background strain
 */
@NodeEntity
public class BackgroundStrain {

    @GraphId
    private Long id;

    private String name;

    private BackgroundStrain() {
        // Empty constructor required as of Neo4j API 2.0.5
    }

    public BackgroundStrain(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
