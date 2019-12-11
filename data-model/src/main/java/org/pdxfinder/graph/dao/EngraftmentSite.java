package org.pdxfinder.graph.dao;

import org.neo4j.ogm.annotation.NodeEntity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Represents the site or organ where implantation was performed
 */
@NodeEntity
public class EngraftmentSite {

    @Id
    @GeneratedValue
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
