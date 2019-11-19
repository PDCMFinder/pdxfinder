package org.pdxfinder.graph.dao;

import org.neo4j.ogm.annotation.NodeEntity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Represents the type of implantation. e.g. Orthotopic, Heterotopic
 */
@NodeEntity
public class EngraftmentType {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private EngraftmentType() {
        // Empty constructor required as of Neo4j API 2.0.5
    }

    public EngraftmentType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
