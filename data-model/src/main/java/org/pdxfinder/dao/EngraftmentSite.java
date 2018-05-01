package org.pdxfinder.dao;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * Represents the site or organ where implantation was performed
 */
@NodeEntity
public class EngraftmentSite {

    @GraphId
    private Long id;

    private String name;

    private EngraftmentSite() {
        // Empty constructor required as of Neo4j API 2.0.5
    }

    public EngraftmentSite(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
